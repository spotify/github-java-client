/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2016 - 2020 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */
package com.spotify.github.http;

import static okhttp3.MediaType.parse;

import com.spotify.github.Tracer;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.checks.AccessToken;
import com.spotify.github.v3.clients.JwtTokenIssuer;
import com.spotify.github.v3.clients.NoopTracer;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.core.MediaType;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractGitHubApiClient {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final int EXPIRY_MARGIN_IN_MINUTES = 5;

  private static final String GET_ACCESS_TOKEN_URL = "app/installations/%s/access_tokens";

  protected Tracer tracer = NoopTracer.INSTANCE;

  protected abstract Map<Integer, AccessToken> installationTokens();

  protected abstract GitHubClientConfig clientConfig();

  protected abstract OkHttpClient client();

  private static boolean isJwtRequest(final String path) {
    return path.startsWith("/app/installation") || path.endsWith("installation");
  }

  /*
   Generates the Authentication header, given the API endpoint and the credentials provided.

   <p>GitHub Requests can be authenticated in 3 different ways.
   (1) Regular, static access token;
   (2) JWT Token, generated from a private key. Used in Github Apps;
   (3) Installation Token, generated from the JWT token. Also used in Github Apps.
  */
  public String getAuthorizationHeader(final String path) {
    var config = clientConfig();
    if (isJwtRequest(path) && config.privateKey().isEmpty()) {
      throw new IllegalStateException("This endpoint needs a client with a private key for an App");
    }
    if (config.accessToken().isPresent()) {
      return String.format("token %s", config.accessToken().get());
    } else if (config.privateKey().isPresent()) {
      final String jwtToken;
      try {
        jwtToken =
            JwtTokenIssuer.fromPrivateKey(config.privateKey().get()).getToken(config.appId().get());
      } catch (Exception e) {
        throw new RuntimeException("There was an error generating JWT token", e);
      }
      if (isJwtRequest(path)) {
        return String.format("Bearer %s", jwtToken);
      }
      if (config.installationId().isEmpty()) {
        throw new RuntimeException("This endpoint needs a client with an installation ID");
      }
      try {
        return String.format(
            "token %s", getInstallationToken(jwtToken, config.installationId().get()));
      } catch (Exception e) {
        throw new RuntimeException("Could not generate access token for github app", e);
      }
    }
    throw new RuntimeException("Not possible to authenticate. ");
  }

  private boolean isExpired(final AccessToken token) {
    // Adds a few minutes to avoid making calls with an expired token due to clock differences
    return token.expiresAt().isBefore(ZonedDateTime.now().plusMinutes(EXPIRY_MARGIN_IN_MINUTES));
  }

  private String getInstallationToken(final String jwtToken, final int installationId)
      throws Exception {

    AccessToken installationToken = installationTokens().get(installationId);

    if (installationToken == null || isExpired(installationToken)) {
      log.info(
          "Github token for installation {} is either expired or null. Trying to get a new one.",
          installationId);
      installationToken = generateInstallationToken(jwtToken, installationId);
      installationTokens().put(installationId, installationToken);
    }
    return installationToken.token();
  }

  /**
   * Create a URL for a given path to this Github server.
   *
   * @param path relative URI
   * @return URL to path on this server
   */
  String urlFor(final String path) {
    return clientConfig().baseUrl().toString().replaceAll("/+$", "")
        + "/"
        + path.replaceAll("^/+", "");
  }

  private AccessToken generateInstallationToken(final String jwtToken, final int installationId)
      throws Exception {
    log.info("Got JWT Token. Now getting Github access_token for installation {}", installationId);
    final String url = String.format(urlFor(GET_ACCESS_TOKEN_URL), installationId);
    final Request request =
        new Request.Builder()
            .addHeader("Accept", "application/vnd.github.machine-man-preview+json")
            .addHeader("Authorization", "Bearer " + jwtToken)
            .url(url)
            .method("POST", RequestBody.create(parse(MediaType.APPLICATION_JSON), ""))
            .build();

    final Response response = client().newCall(request).execute();

    if (!response.isSuccessful()) {
      throw new Exception(
          String.format(
              "Got non-2xx status %s when getting an access token from GitHub: %s",
              response.code(), response.message()));
    }

    if (response.body() == null) {
      throw new Exception(
          String.format(
              "Got empty response body when getting an access token from GitHub, HTTP status was: %s",
              response.message()));
    }
    final String text = response.body().string();
    response.body().close();
    return Json.create().fromJson(text, AccessToken.class);
  }

  protected abstract RequestNotOkException mapException(Response res, Request request)
      throws IOException;

  protected abstract CompletableFuture<Response> processPossibleRedirects(
      Response response, AtomicBoolean redirected);

  protected CompletableFuture<Response> call(final Request request) {
    final Call call = client().newCall(request);

    final CompletableFuture<Response> future = new CompletableFuture<>();

    // avoid multiple redirects
    final AtomicBoolean redirected = new AtomicBoolean(false);

    call.enqueue(
        new Callback() {
          @Override
          public void onFailure(@NotNull final Call call, final IOException e) {
            future.completeExceptionally(e);
          }

          @Override
          public void onResponse(@NotNull final Call call, final Response response) {
            processPossibleRedirects(response, redirected)
                .handle(
                    (res, ex) -> {
                      if (Objects.nonNull(ex)) {
                        future.completeExceptionally(ex);
                      } else if (!res.isSuccessful()) {
                        try {
                          future.completeExceptionally(mapException(res, request));
                        } catch (final Throwable e) {
                          future.completeExceptionally(e);
                        } finally {
                          if (res.body() != null) {
                            res.body().close();
                          }
                        }
                      } else {
                        future.complete(res);
                      }
                      return res;
                    });
          }
        });
    tracer.span(request.url().toString(), request.method(), future);
    return future;
  }
}
