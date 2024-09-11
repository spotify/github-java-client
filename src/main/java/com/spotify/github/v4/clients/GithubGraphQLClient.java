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
package com.spotify.github.v4.clients;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static okhttp3.MediaType.parse;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.spotify.github.http.AbstractGitHubApiClient;
import com.spotify.github.http.GitHubClientConfig;
import com.spotify.github.v3.checks.AccessToken;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GithubGraphQLClient extends AbstractGitHubApiClient {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final GitHubClientConfig clientConfig;
  private final Map<Integer, AccessToken> installationTokens;

  private GithubGraphQLClient(final GitHubClientConfig config) {
    this.clientConfig = config;
    this.installationTokens = new HashMap<>();
  }

  public static GithubGraphQLClient create(final GitHubClientConfig config) {
    return new GithubGraphQLClient(config);
  }

  private Request.Builder graphqlRequestBuilder() {
    URI url =
        clientConfig
            .graphqlApiUrl()
            .orElseThrow(() -> new IllegalStateException("No graphql url set"));
    final Request.Builder builder =
        new Request.Builder()
            .url(url.toString())
            .addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    builder.addHeader(HttpHeaders.AUTHORIZATION, getAuthorizationHeader("/graphql"));
    return builder;
  }

  @Override
  protected Map<Integer, AccessToken> installationTokens() {
    return installationTokens;
  }

  @Override
  protected GitHubClientConfig clientConfig() {
    return this.clientConfig;
  }

  @Override
  protected OkHttpClient client() {
    return this.clientConfig.client();
  }

  @Override
  protected RequestNotOkException mapException(final Response res, final Request request)
      throws IOException {
    return null;
  }

  @Override
  protected CompletableFuture<Response> processPossibleRedirects(
      final Response response, final AtomicBoolean redirected) {
    return completedFuture(response);
  }

  /**
   * Make a POST request to the graphql endpoint of Github
   *
   * @param queryRequest GraphQLOperationRequest object with query or mutation request
   * @param responseProjection Select what fields are required in the response
   * @return response
   * @see
   *     "https://docs.github.com/en/enterprise-server@3.9/graphql/guides/forming-calls-with-graphql#communicating-with-graphql"
   */
  public CompletableFuture<Response> queryGraphQL(
      final GraphQLOperationRequest queryRequest,
      final GraphQLResponseProjection responseProjection) {
    GraphQLRequest graphqlRequest = new GraphQLRequest(queryRequest, responseProjection);
    String body = graphqlRequest.toQueryString();
    final Request request =
        graphqlRequestBuilder()
            .method("POST", RequestBody.create(parse(MediaType.APPLICATION_JSON), body))
            .build();
    log.info("Making GraphQL Query POST request to {}, with body {}", request.url(), body);
    return this.call(request);
  }
}
