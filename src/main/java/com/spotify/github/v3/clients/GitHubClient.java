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

package com.spotify.github.v3.clients;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static okhttp3.MediaType.parse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.spotify.github.Tracer;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.checks.AccessToken;
import com.spotify.github.v3.comment.Comment;
import com.spotify.github.v3.exceptions.ReadOnlyRepositoryException;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import com.spotify.github.v3.git.Reference;
import com.spotify.github.v3.prs.PullRequestItem;
import com.spotify.github.v3.prs.Review;
import com.spotify.github.v3.prs.ReviewRequests;
import com.spotify.github.v3.repos.Branch;
import com.spotify.github.v3.repos.CommitItem;
import com.spotify.github.v3.repos.FolderContent;
import com.spotify.github.v3.repos.Repository;
import com.spotify.github.v3.repos.Status;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Github client is a main communication entry point. Provides lower level communication
 * functionality as well as acts as a factory for the higher level API clients.
 */
public class GitHubClient {

  private Tracer tracer = NoopTracer.INSTANCE;

  static final Consumer<Response> IGNORE_RESPONSE_CONSUMER = (response) -> {
    if (response.body() != null) {
      response.body().close();
    }
  };
  static final TypeReference<List<Comment>> LIST_COMMENT_TYPE_REFERENCE =
      new TypeReference<>() {};
  static final TypeReference<List<Repository>> LIST_REPOSITORY =
      new TypeReference<>() {};
  static final TypeReference<List<CommitItem>> LIST_COMMIT_TYPE_REFERENCE =
      new TypeReference<>() {};
  static final TypeReference<List<Review>> LIST_REVIEW_TYPE_REFERENCE = new TypeReference<>() {};
  static final TypeReference<ReviewRequests> LIST_REVIEW_REQUEST_TYPE_REFERENCE =
      new TypeReference<>() {};
  static final TypeReference<List<Status>> LIST_STATUS_TYPE_REFERENCE =
      new TypeReference<>() {};
  static final TypeReference<List<FolderContent>> LIST_FOLDERCONTENT_TYPE_REFERENCE =
      new TypeReference<>() {};
  static final TypeReference<List<PullRequestItem>> LIST_PR_TYPE_REFERENCE =
      new TypeReference<>() {};
  static final TypeReference<List<Branch>> LIST_BRANCHES =
      new TypeReference<>() {};
  static final TypeReference<List<Reference>> LIST_REFERENCES =
      new TypeReference<>() {};

  private static final String GET_ACCESS_TOKEN_URL = "app/installations/%s/access_tokens";

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final int PERMANENT_REDIRECT = 301;
  private static final int TEMPORARY_REDIRECT = 307;
  private static final int FORBIDDEN = 403;

  private final URI baseUrl;
  private final Json json = Json.create();
  private final OkHttpClient client;
  private final String token;

  private final byte[] privateKey;
  private final Integer appId;
  private final Integer installationId;

  private final Map<Integer, AccessToken> installationTokens;

  private GitHubClient(
      final OkHttpClient client,
      final URI baseUrl,
      final String accessToken,
      final byte[] privateKey,
      final Integer appId,
      final Integer installationId) {
    this.baseUrl = baseUrl;
    this.token = accessToken;
    this.client = client;
    this.privateKey = privateKey;
    this.appId = appId;
    this.installationId = installationId;
    this.installationTokens = new HashMap<>();
  }

  /**
   * Create a github api client with a given base URL and authorization token.
   *
   * @param baseUrl base URL
   * @param token authorization token
   * @return github api client
   */
  public static GitHubClient create(final URI baseUrl, final String token) {
    return new GitHubClient(new OkHttpClient(), baseUrl, token, null, null, null);
  }

  /**
   * Create a github api client with a given base URL and a path to a key.
   *
   * @param baseUrl base URL
   * @param privateKey the private key PEM file
   * @param appId the github app ID
   * @return github api client
   */
  public static GitHubClient create(final URI baseUrl, final File privateKey, final Integer appId) {
    return createOrThrow(new OkHttpClient(), baseUrl, privateKey, appId, null);
  }

  /**
   * Create a github api client with a given base URL and a path to a key.
   *
   * @param baseUrl base URL
   * @param privateKey the private key as byte array
   * @param appId the github app ID
   * @return github api client
   */
  public static GitHubClient create(final URI baseUrl, final byte[] privateKey, final Integer appId) {
    return new GitHubClient(new OkHttpClient(), baseUrl, null, privateKey, appId, null);
  }

  /**
   * Create a github api client with a given base URL and a path to a key.
   *
   * @param baseUrl base URL
   * @param privateKey the private key PEM file
   * @param appId the github app ID
   * @param installationId the installationID to be authenticated as
   * @return github api client
   */
  public static GitHubClient create(
      final URI baseUrl, final File privateKey, final Integer appId, final Integer installationId) {
    return createOrThrow(new OkHttpClient(), baseUrl, privateKey, appId, installationId);
  }

  /**
   * Create a github api client with a given base URL and a path to a key.
   *
   * @param baseUrl base URL
   * @param privateKey the private key as byte array
   * @param appId the github app ID
   * @param installationId the installationID to be authenticated as
   * @return github api client
   */
  public static GitHubClient create(
          final URI baseUrl, final byte[] privateKey, final Integer appId, final Integer installationId) {
    return new GitHubClient(new OkHttpClient(), baseUrl, null, privateKey, appId, installationId);
  }

  /**
   * Create a github api client with a given base URL and a path to a key.
   *
   * @param httpClient an instance of OkHttpClient
   * @param baseUrl base URL
   * @param privateKey the private key PEM file
   * @param appId the github app ID
   * @return github api client
   */
  public static GitHubClient create(
      final OkHttpClient httpClient,
      final URI baseUrl,
      final File privateKey,
      final Integer appId) {
    return createOrThrow(httpClient, baseUrl, privateKey, appId, null);
  }

  /**
   * Create a github api client with a given base URL and a path to a key.
   *
   * @param httpClient an instance of OkHttpClient
   * @param baseUrl base URL
   * @param privateKey the private key as byte array
   * @param appId the github app ID
   * @return github api client
   */
  public static GitHubClient create(
          final OkHttpClient httpClient,
          final URI baseUrl,
          final byte[] privateKey,
          final Integer appId) {
    return new GitHubClient(httpClient, baseUrl, null, privateKey, appId, null);
  }

  /**
   * Create a github api client with a given base URL and a path to a key.
   *
   * @param httpClient an instance of OkHttpClient
   * @param baseUrl base URL
   * @param privateKey the private key PEM file
   * @param appId the github app ID
   * @return github api client
   */
  public static GitHubClient create(
      final OkHttpClient httpClient,
      final URI baseUrl,
      final File privateKey,
      final Integer appId,
      final Integer installationId) {
    return createOrThrow(httpClient, baseUrl, privateKey, appId, installationId);
  }

  /**
   * Create a github api client with a given base URL and a path to a key.
   *
   * @param httpClient an instance of OkHttpClient
   * @param baseUrl base URL
   * @param privateKey the private key as byte array
   * @param appId the github app ID
   * @return github api client
   */
  public static GitHubClient create(
          final OkHttpClient httpClient,
          final URI baseUrl,
          final byte[] privateKey,
          final Integer appId,
          final Integer installationId) {
    return new GitHubClient(httpClient, baseUrl, null, privateKey, appId, installationId);
  }

  /**
   * Create a github api client with a given base URL and authorization token.
   *
   * @param httpClient an instance of OkHttpClient
   * @param baseUrl base URL
   * @param token authorization token
   * @return github api client
   */
  public static GitHubClient create(
      final OkHttpClient httpClient, final URI baseUrl, final String token) {
    return new GitHubClient(httpClient, baseUrl, token, null, null, null);
  }

  /**
   * Receives a github client and scopes it to a certain installation ID.
   *
   * @param client the github client with a valid private key
   * @param installationId the installation ID to be scoped
   * @return github api client
   */
  public static GitHubClient scopeForInstallationId(
      final GitHubClient client, final int installationId) {
    if (client.getPrivateKey().isEmpty()) {
      throw new RuntimeException("Installation ID scoped client needs a private key");
    }
    return new GitHubClient(
        client.client,
        client.baseUrl,
        null,
        client.getPrivateKey().get(),
        client.appId,
        installationId);
  }

  static String responseBodyUnchecked(final Response response) {
    try (ResponseBody body = response.body()) {
      return body.string();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed getting response body for: " + response, e);
    }
  }

  public GitHubClient withTracer(final Tracer tracer) {
    this.tracer = tracer;
    return this;
  }

  public Optional<byte[]> getPrivateKey() {
    return Optional.ofNullable(privateKey);
  }

  public Optional<String> getAccessToken() {
    return Optional.ofNullable(token);
  }

  /**
   * Create a repository API client
   *
   * @param owner repository owner
   * @param repo repository name
   * @return repository API client
   */
  public RepositoryClient createRepositoryClient(final String owner, final String repo) {
    return RepositoryClient.create(this, owner, repo);
  }

  /**
   * Create a GitData API client
   *
   * @param owner repository owner
   * @param repo repository name
   * @return GitData API client
   */
  public GitDataClient createGitDataClient(final String owner, final String repo) {
    return GitDataClient.create(this, owner, repo);
  }

  /**
   * Create search API client
   *
   * @return search API client
   */
  public SearchClient createSearchClient() {
    return SearchClient.create(this);
  }

  Json json() {
    return json;
  }

  /**
   * Make an http GET request for the given path on the server
   *
   * @param path relative to the Github base url
   * @return response body as a String
   */
  CompletableFuture<Response> request(final String path) {
    final Request request = requestBuilder(path).build();
    log.debug("Making request to {}", request.url().toString());
    return call(request);
  }

  /**
   * Make an http GET request for the given path on the server
   *
   * @param path relative to the Github base url
   * @return body deserialized as provided type
   */
  <T> CompletableFuture<T> request(final String path, final Class<T> clazz) {
    final Request request = requestBuilder(path).build();
    log.debug("Making request to {}", request.url().toString());
    return call(request)
        .thenApply(body -> json().fromJsonUncheckedNotNull(responseBodyUnchecked(body), clazz));
  }

  /**
   * Make an http GET request for the given path on the server
   *
   * @param path relative to the Github base url
   * @param extraHeaders extra github headers to be added to the call
   * @return body deserialized as provided type
   */
  <T> CompletableFuture<T> request(
      final String path, final Class<T> clazz, final Map<String, String> extraHeaders) {
    final Request.Builder builder = requestBuilder(path);
    extraHeaders.forEach(builder::addHeader);
    final Request request = builder.build();
    log.debug("Making request to {}", request.url().toString());
    return call(request)
        .thenApply(body -> json().fromJsonUncheckedNotNull(responseBodyUnchecked(body), clazz));
  }

  /**
   * Make an http request for the given path on the Github server.
   *
   * @param path relative to the Github base url
   * @param extraHeaders extra github headers to be added to the call
   * @return body deserialized as provided type
   */
  <T> CompletableFuture<T> request(
      final String path,
      final TypeReference<T> typeReference,
      final Map<String, String> extraHeaders) {
    final Request.Builder builder = requestBuilder(path);
    extraHeaders.forEach(builder::addHeader);
    final Request request = builder.build();
    log.debug("Making request to {}", request.url().toString());
    return call(request)
        .thenApply(
            response ->
                json().fromJsonUncheckedNotNull(responseBodyUnchecked(response), typeReference));
  }

  /**
   * Make an http request for the given path on the Github server.
   *
   * @param path relative to the Github base url
   * @return body deserialized as provided type
   */
  <T> CompletableFuture<T> request(final String path, final TypeReference<T> typeReference) {
    final Request request = requestBuilder(path).build();
    log.debug("Making request to {}", request.url().toString());
    return call(request)
        .thenApply(
            response ->
                json().fromJsonUncheckedNotNull(responseBodyUnchecked(response), typeReference));
  }

  /**
   * Make an http POST request for the given path with provided JSON body.
   *
   * @param path relative to the Github base url
   * @param data request body as stringified JSON
   * @return response body as String
   */
  CompletableFuture<Response> post(final String path, final String data) {
    final Request request =
        requestBuilder(path)
            .method("POST", RequestBody.create(parse(MediaType.APPLICATION_JSON), data))
            .build();
    log.debug("Making POST request to {}", request.url().toString());
    return call(request);
  }

  /**
   * Make an http POST request for the given path with provided JSON body.
   *
   * @param path relative to the Github base url
   * @param data request body as stringified JSON
   * @param extraHeaders
   * @return response body as String
   */
  CompletableFuture<Response> post(
      final String path, final String data, final Map<String, String> extraHeaders) {
    final Request.Builder builder =
        requestBuilder(path)
            .method("POST", RequestBody.create(parse(MediaType.APPLICATION_JSON), data));
    extraHeaders.forEach(builder::addHeader);
    final Request request = builder.build();
    log.debug("Making POST request to {}", request.url().toString());
    return call(request);
  }

  /**
   * Make an http POST request for the given path with provided JSON body.
   *
   * @param path relative to the Github base url
   * @param data request body as stringified JSON
   * @param clazz class to cast response as
   * @param extraHeaders
   * @return response body deserialized as provided class
   */
  <T> CompletableFuture<T> post(
      final String path,
      final String data,
      final Class<T> clazz,
      final Map<String, String> extraHeaders) {
    return post(path, data, extraHeaders)
        .thenApply(
            response -> json().fromJsonUncheckedNotNull(responseBodyUnchecked(response), clazz));
  }

  /**
   * Make an http POST request for the given path with provided JSON body.
   *
   * @param path relative to the Github base url
   * @param data request body as stringified JSON
   * @param clazz class to cast response as
   * @return response body deserialized as provided class
   */
  <T> CompletableFuture<T> post(final String path, final String data, final Class<T> clazz) {
    return post(path, data)
        .thenApply(
            response -> json().fromJsonUncheckedNotNull(responseBodyUnchecked(response), clazz));
  }

  /**
   * Make an http PUT request for the given path with provided JSON body.
   *
   * @param path relative to the Github base url
   * @param data request body as stringified JSON
   * @return response body as String
   */
  CompletableFuture<Response> put(final String path, final String data) {
    final Request request =
        requestBuilder(path)
            .method("PUT", RequestBody.create(parse(MediaType.APPLICATION_JSON), data))
            .build();
    log.debug("Making POST request to {}", request.url().toString());
    return call(request);
  }

  /**
   * Make an http PATCH request for the given path with provided JSON body.
   *
   * @param path relative to the Github base url
   * @param data request body as stringified JSON
   * @return response body as String
   */
  CompletableFuture<Response> patch(final String path, final String data) {
    final Request request =
        requestBuilder(path)
            .method("PATCH", RequestBody.create(parse(MediaType.APPLICATION_JSON), data))
            .build();
    log.debug("Making PATCH request to {}", request.url().toString());
    return call(request);
  }

  /**
   * Make an http PATCH request for the given path with provided JSON body
   *
   * @param path relative to the Github base url
   * @param data request body as stringified JSON
   * @param clazz class to cast response as
   * @return response body as String
   */
  <T> CompletableFuture<T> patch(
      final String path,
      final String data,
      final Class<T> clazz,
      final Map<String, String> extraHeaders) {
    final Request.Builder builder =
        requestBuilder(path)
            .method("PATCH", RequestBody.create(parse(MediaType.APPLICATION_JSON), data));
    extraHeaders.forEach(builder::addHeader);
    final Request request = builder.build();
    log.debug("Making PATCH request to {}", request.url().toString());
    return call(request)
        .thenApply(
            response -> json().fromJsonUncheckedNotNull(responseBodyUnchecked(response), clazz));
  }

  /**
   * Make an http DELETE request for the given path.
   *
   * @param path relative to the Github base url
   * @return response body as String
   */
  CompletableFuture<Response> delete(final String path) {
    final Request request = requestBuilder(path).delete().build();
    log.debug("Making DELETE request to {}", request.url().toString());
    return call(request);
  }

  /**
   * Make an http DELETE request for the given path.
   *
   * @param path relative to the Github base url
   * @param data request body as stringified JSON
   * @return response body as String
   */
  CompletableFuture<Response> delete(final String path, final String data) {
    final Request request =
        requestBuilder(path)
            .method("DELETE", RequestBody.create(parse(MediaType.APPLICATION_JSON), data))
            .build();
    log.debug("Making DELETE request to {}", request.url().toString());
    return call(request);
  }

  /**
   * Create a URL for a given path to this Github server.
   *
   * @param path relative URI
   * @return URL to path on this server
   */
  String urlFor(final String path) {
    return baseUrl.toString().replaceAll("/+$", "") + "/" + path.replaceAll("^/+", "");
  }

  private Request.Builder requestBuilder(final String path) {
    final Request.Builder builder =
        new Request.Builder()
            .url(urlFor(path))
            .addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    builder.addHeader(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(path));

    return builder;
  }

  /*
   Generates the Authentication header, given the API endpoint and the credentials provided.

   <p>Github Requests can be authenticated in 3 different ways.
   (1) Regular, static access token;
   (2) JWT Token, generated from a private key. Used in Github Apps;
   (3) Installation Token, generated from the JWT token. Also used in Github Apps.
  */
  private String getAuthorizationHeader(final String path) {
    if (isJwtRequest(path) && getPrivateKey().isEmpty()) {
      throw new IllegalStateException("This endpoint needs a client with a private key for an App");
    }
    if (getAccessToken().isPresent()) {
      return String.format("token %s", token);
    } else if (getPrivateKey().isPresent()) {
      final String jwtToken;
      try {
        jwtToken = JwtTokenIssuer.fromPrivateKey(privateKey).getToken(appId);
      } catch (Exception e) {
        throw new RuntimeException("There was an error generating JWT token", e);
      }
      if (isJwtRequest(path)) {
        return String.format("Bearer %s", jwtToken);
      }
      if (installationId == null) {
        throw new RuntimeException("This endpoint needs a client with an installation ID");
      }
      try {
        return String.format("token %s", getInstallationToken(jwtToken, installationId));
      } catch (Exception e) {
        throw new RuntimeException("Could not generate access token for github app", e);
      }
    }
    throw new RuntimeException("Not possible to authenticate. ");
  }

  private boolean isJwtRequest(final String path) {
    return path.startsWith("/app/installation") || path.endsWith("installation");
  }

  private String getInstallationToken(final String jwtToken, final int installationId)
      throws Exception {

    AccessToken installationToken = installationTokens.get(installationId);

    if (installationToken == null || isExpired(installationToken)) {
      log.info(
          "Github token for installation {} is either expired or null. Trying to get a new one.",
          installationId);
      installationToken = generateInstallationToken(jwtToken, installationId);
      installationTokens.put(installationId, installationToken);
    }
    return installationToken.token();
  }

  private boolean isExpired(final AccessToken token) {
    return token.expiresAt().isBefore(ZonedDateTime.now().plusMinutes(-1));
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

    final Response response = client.newCall(request).execute();

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

  private CompletableFuture<Response> call(final Request request) {
    final Call call = client.newCall(request);

    final CompletableFuture<Response> future = new CompletableFuture<>();

    // avoid multiple redirects
    final AtomicBoolean redirected = new AtomicBoolean(false);

    call.enqueue(
        new Callback() {
          @Override
          public void onFailure(final Call call, final IOException e) {
            future.completeExceptionally(e);
          }

          @Override
          public void onResponse(final Call call, final Response response) {
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

  private RequestNotOkException mapException(final Response res, final Request request)
      throws IOException {
    String bodyString = res.body() != null ? res.body().string() : "";
    if (res.code() == FORBIDDEN) {
      if (bodyString.contains("Repository was archived so is read-only")) {
        return new ReadOnlyRepositoryException(request.url().encodedPath(), res.code(), bodyString);
      }
    }
    return new RequestNotOkException(request.url().encodedPath(), res.code(), bodyString);
  }

  CompletableFuture<Response> processPossibleRedirects(
      final Response response, final AtomicBoolean redirected) {
    if (response.code() >= PERMANENT_REDIRECT
        && response.code() <= TEMPORARY_REDIRECT
        && !redirected.get()) {
      redirected.set(true);
      // redo the same request with a new URL
      final String newLocation = response.header("Location");
      final Request request =
          requestBuilder(newLocation)
              .url(newLocation)
              .method("POST", response.request().body())
              .build();
      // Do the new call and complete the original future when the new call completes
      return call(request);
    }

    return completedFuture(response);
  }

  /**
   * Wrapper to Constructors that expose File object for the privateKey argument
   * */
  private static GitHubClient createOrThrow(final OkHttpClient httpClient, final URI baseUrl, final File privateKey, final Integer appId, final Integer installationId) {
    try {
      return new GitHubClient(httpClient, baseUrl, null, FileUtils.readFileToByteArray(privateKey), appId, installationId);
    } catch (IOException e) {
      throw new RuntimeException("There was an error generating JWT token", e);
    }
  }
}
