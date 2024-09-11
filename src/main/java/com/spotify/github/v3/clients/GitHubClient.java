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
import com.spotify.github.http.AbstractGitHubApiClient;
import com.spotify.github.http.GitHubClientConfig;
import com.spotify.github.http.ImmutableGitHubClientConfig;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.Team;
import com.spotify.github.v3.User;
import com.spotify.github.v3.checks.AccessToken;
import com.spotify.github.v3.comment.Comment;
import com.spotify.github.v3.exceptions.ReadOnlyRepositoryException;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import com.spotify.github.v3.git.Reference;
import com.spotify.github.v3.orgs.TeamInvitation;
import com.spotify.github.v3.prs.PullRequestItem;
import com.spotify.github.v3.prs.Review;
import com.spotify.github.v3.prs.ReviewRequests;
import com.spotify.github.v3.repos.Branch;
import com.spotify.github.v3.repos.CommitItem;
import com.spotify.github.v3.repos.FolderContent;
import com.spotify.github.v3.repos.Repository;
import com.spotify.github.v3.repos.RepositoryInvitation;
import com.spotify.github.v3.repos.Status;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * GitHub client is a main communication entry point for the REST APIs. Provides lower level
 * communication functionality as well as acts as a factory for the higher level API clients.
 */
public class GitHubClient extends AbstractGitHubApiClient {

  private static final int EXPIRY_MARGIN_IN_MINUTES = 5;
  private final GitHubClientConfig clientConfig;

  static final Consumer<Response> IGNORE_RESPONSE_CONSUMER =
      (response) -> {
        if (response.body() != null) {
          response.body().close();
        }
      };
  static final TypeReference<List<Comment>> LIST_COMMENT_TYPE_REFERENCE = new TypeReference<>() {};
  static final TypeReference<List<Repository>> LIST_REPOSITORY = new TypeReference<>() {};
  static final TypeReference<List<CommitItem>> LIST_COMMIT_TYPE_REFERENCE =
      new TypeReference<>() {};
  static final TypeReference<List<Review>> LIST_REVIEW_TYPE_REFERENCE = new TypeReference<>() {};
  static final TypeReference<ReviewRequests> LIST_REVIEW_REQUEST_TYPE_REFERENCE =
      new TypeReference<>() {};
  static final TypeReference<List<Status>> LIST_STATUS_TYPE_REFERENCE = new TypeReference<>() {};
  static final TypeReference<List<FolderContent>> LIST_FOLDERCONTENT_TYPE_REFERENCE =
      new TypeReference<>() {};
  static final TypeReference<List<PullRequestItem>> LIST_PR_TYPE_REFERENCE =
      new TypeReference<>() {};
  static final TypeReference<List<Branch>> LIST_BRANCHES = new TypeReference<>() {};
  static final TypeReference<List<Reference>> LIST_REFERENCES = new TypeReference<>() {};
  static final TypeReference<List<RepositoryInvitation>> LIST_REPOSITORY_INVITATION =
      new TypeReference<>() {};

  static final TypeReference<List<Team>> LIST_TEAMS = new TypeReference<>() {};

  static final TypeReference<List<User>> LIST_TEAM_MEMBERS = new TypeReference<>() {};

  static final TypeReference<List<TeamInvitation>> LIST_PENDING_TEAM_INVITATIONS =
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

    this.clientConfig =
        ImmutableGitHubClientConfig.builder()
            .client(client)
            .baseUrl(Optional.ofNullable(baseUrl))
            .accessToken(Optional.ofNullable(accessToken))
            .privateKey(Optional.ofNullable(privateKey))
            .appId(Optional.ofNullable(appId))
            .installationId(Optional.ofNullable(installationId))
            .build();
    this.baseUrl = clientConfig.baseUrl().orElse(null);
    this.token = clientConfig.accessToken().orElse(null);
    this.client = clientConfig.client();
    this.privateKey = clientConfig.privateKey().orElse(null);
    this.appId = clientConfig.appId().orElse(null);
    this.installationId = clientConfig.installationId().orElse(null);
    this.installationTokens = new HashMap<>();
  }

  private GitHubClient(final GitHubClientConfig config) {
    this.baseUrl = config.baseUrl().orElse(null);
    this.token = config.accessToken().orElse(null);
    this.client = config.client();
    this.privateKey = config.privateKey().orElse(null);
    this.appId = config.appId().orElse(null);
    this.installationId = config.installationId().orElse(null);
    this.installationTokens = new HashMap<>();
    this.clientConfig = config;
  }

  /**
   * Create a github api client with a given base URL and authorization token.
   *
   * @param config GitHubClientConfig object
   * @return github api client
   */
  public static GitHubClient create(final GitHubClientConfig config) {
    return new GitHubClient(config);
  }

  /**
   * Create a github api client with a given base URL and authorization token.
   *
   * @deprecated use {@link #create(GitHubClientConfig)} instead
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
   * @deprecated use {@link #create(GitHubClientConfig)} instead
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
   * @deprecated use {@link #create(GitHubClientConfig)} instead
   * @param baseUrl base URL
   * @param privateKey the private key as byte array
   * @param appId the github app ID
   * @return github api client
   */
  public static GitHubClient create(
      final URI baseUrl, final byte[] privateKey, final Integer appId) {
    return new GitHubClient(new OkHttpClient(), baseUrl, null, privateKey, appId, null);
  }

  /**
   * Create a github api client with a given base URL and a path to a key.
   *
   * @deprecated use {@link #create(GitHubClientConfig)} instead
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
   * @deprecated use {@link #create(GitHubClientConfig)} instead
   * @param baseUrl base URL
   * @param privateKey the private key as byte array
   * @param appId the github app ID
   * @param installationId the installationID to be authenticated as
   * @return github api client
   */
  public static GitHubClient create(
      final URI baseUrl,
      final byte[] privateKey,
      final Integer appId,
      final Integer installationId) {
    return new GitHubClient(new OkHttpClient(), baseUrl, null, privateKey, appId, installationId);
  }

  /**
   * Create a github api client with a given base URL and a path to a key.
   *
   * @deprecated use {@link #create(GitHubClientConfig)} instead
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
   * @deprecated use {@link #create(GitHubClientConfig)} instead
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
   * @deprecated use {@link #create(GitHubClientConfig)} instead
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

  public GitHubClient withScopeForInstallationId(final int installationId) {
    if (Optional.ofNullable(privateKey).isEmpty()) {
      throw new RuntimeException("Installation ID scoped client needs a private key");
    }
    return new GitHubClient(client, baseUrl, null, privateKey, appId, installationId);
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

  /**
   * Create a checks API client
   *
   * @param owner repository owner
   * @param repo repository name
   * @return checks API client
   */
  public ChecksClient createChecksClient(final String owner, final String repo) {
    return ChecksClient.create(this, owner, repo);
  }

  /**
   * Create organisation API client
   *
   * @return organisation API client
   */
  public OrganisationClient createOrganisationClient(final String org) {
    return OrganisationClient.create(this, org);
  }

  public UserClient createUserClient(final String owner) {
    return UserClient.create(this, owner);
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
    log.debug("Making request to {}", request.url());
    return call(request);
  }

  /**
   * Make an http GET request for the given path on the server
   *
   * @param path relative to the Github base url
   * @param extraHeaders extra github headers to be added to the call
   * @return a reader of response body
   */
  CompletableFuture<Response> request(final String path, final Map<String, String> extraHeaders) {
    final Request.Builder builder = requestBuilder(path);
    extraHeaders.forEach(builder::addHeader);
    final Request request = builder.build();
    log.debug("Making request to {}", request.url());
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
    log.debug("Making request to {}", request.url());
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
    log.debug("Making request to {}", request.url());
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
    log.debug("Making request to {}", request.url());
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
    log.debug("Making request to {}", request.url());
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
    log.debug("Making POST request to {}", request.url());
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
    log.debug("Making POST request to {}", request.url());
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
    log.debug("Making POST request to {}", request.url());
    return call(request);
  }

  /**
   * Make a HTTP PUT request for the given path with provided JSON body.
   *
   * @param path relative to the Github base url
   * @param data request body as stringified JSON
   * @param clazz class to cast response as
   * @return response body deserialized as provided class
   */
  <T> CompletableFuture<T> put(final String path, final String data, final Class<T> clazz) {
    return put(path, data)
        .thenApply(
            response -> json().fromJsonUncheckedNotNull(responseBodyUnchecked(response), clazz));
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
    log.debug("Making PATCH request to {}", request.url());
    return call(request);
  }

  /**
   * Make an http PATCH request for the given path with provided JSON body.
   *
   * @param path relative to the Github base url
   * @param data request body as stringified JSON
   * @param clazz class to cast response as
   * @return response body deserialized as provided class
   */
  <T> CompletableFuture<T> patch(final String path, final String data, final Class<T> clazz) {
    return patch(path, data)
        .thenApply(
            response -> json().fromJsonUncheckedNotNull(responseBodyUnchecked(response), clazz));
  }

  /**
   * Make an http PATCH request for the given path with provided JSON body
   *
   * @param path relative to the Github base url
   * @param data request body as stringified JSON
   * @param clazz class to cast response as
   * @return response body deserialized as provided class
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
    log.debug("Making PATCH request to {}", request.url());
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
    log.debug("Making DELETE request to {}", request.url());
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
    log.debug("Making DELETE request to {}", request.url());
    return call(request);
  }

  private Request.Builder requestBuilder(final String path) {
    String url = urlFor(path).orElseThrow(() -> new IllegalStateException("No baseUrl defined"));
    final Request.Builder builder =
        new Request.Builder()
            .url(url)
            .addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    builder.addHeader(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(path));

    return builder;
  }

  @Override
  protected Map<Integer, AccessToken> installationTokens() {
    return this.installationTokens;
  }

  @Override
  protected GitHubClientConfig clientConfig() {
    return this.clientConfig;
  }

  @Override
  protected OkHttpClient client() {
    return client;
  }

  @Override
  protected RequestNotOkException mapException(final Response res, final Request request)
      throws IOException {
    String bodyString = res.body() != null ? res.body().string() : "";
    Map<String, List<String>> headersMap = res.headers().toMultimap();

    if (res.code() == FORBIDDEN) {
      if (bodyString.contains("Repository was archived so is read-only")) {
        return new ReadOnlyRepositoryException(
            request.method(), request.url().encodedPath(), res.code(), bodyString, headersMap);
      }
    }

    return new RequestNotOkException(
        request.method(), request.url().encodedPath(), res.code(), bodyString, headersMap);
  }

  @Override
  protected CompletableFuture<Response> processPossibleRedirects(
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
              .method(response.request().method(), response.request().body())
              .build();
      // Do the new call and complete the original future when the new call completes
      return call(request);
    }

    return completedFuture(response);
  }

  /** Wrapper to Constructors that expose File object for the privateKey argument */
  private static GitHubClient createOrThrow(
      final OkHttpClient httpClient,
      final URI baseUrl,
      final File privateKey,
      final Integer appId,
      final Integer installationId) {

    try {
      return new GitHubClient(
          ImmutableGitHubClientConfig.builder()
              .baseUrl(Optional.ofNullable(baseUrl))
              .privateKey(FileUtils.readFileToByteArray(privateKey))
              .appId(Optional.ofNullable(appId))
              .installationId(Optional.ofNullable(installationId))
              .client(httpClient)
              .build());
    } catch (IOException e) {
      throw new RuntimeException("There was an error generating JWT token", e);
    }
  }
}
