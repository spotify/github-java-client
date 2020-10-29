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

import static com.spotify.github.v3.clients.ChecksClientTest.loadResource;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.checks.AccessToken;
import com.spotify.github.v3.checks.ImmutableAccessToken;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GitHubAuthTest {

  private final File key =
      new File(
          Objects.requireNonNull(
                  getClass().getClassLoader().getResource(
                      "com/spotify/github/v3/github-private-key"))
              .getFile());
  private URI url;
  private final MockWebServer mockServer = new MockWebServer();
  private OkHttpClient client;
  private ChecksClient checksClient;

  private final MockResponse validTokenResponse =
      new MockResponse().setBody(Json.create().toJson(getTestInstallationToken()));
  private final MockResponse expiredTokenResponse =
      new MockResponse()
          .setBody(
              Json.create()
                  .toJson(
                      ImmutableAccessToken.copyOf(getTestInstallationToken())
                          .withExpiresAt(ZonedDateTime.now().minusHours(2))));
  private final MockResponse checkRunResponse =
      new MockResponse().setBody(loadResource("com/spotify/github/v3/checks/checks-run-completed-response.json"));

  public GitHubAuthTest() throws JsonProcessingException {}

  @Before
  public void setUp() throws IOException {
    client =
        new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(1))
            .readTimeout(Duration.ofSeconds(1))
            .build();

    mockServer.start();
    url = mockServer.url("/").uri();
    checksClient =
        GitHubClient.create(client, url, key, 123, 1)
            .createRepositoryClient("foo", "bar")
            .createChecksApiClient();
  }

  @After
  public void tearDown() throws IOException {
    mockServer.shutdown();
  }

  @Test
  public void usesProvidedJwtTokenToFetchInstallationToken() throws Exception {
    mockServer.enqueue(validTokenResponse);
    mockServer.enqueue(checkRunResponse);

    checksClient.getCheckRun(123).join();

    assertThat(mockServer.getRequestCount(), is(2));

    final RecordedRequest r1 = mockServer.takeRequest();
    assertThat(r1.getHeader("Accept"), is("application/vnd.github.machine-man-preview+json"));
    assertThat(r1.getHeader("Authorization"), startsWith("Bearer eyJh"));
    assertThat(r1.getPath(), is("/app/installations/1/access_tokens"));

    assertThat(mockServer.takeRequest().getPath(), is("/repos/foo/bar/check-runs/123"));
  }

  @Test
  public void usesCachedInstallationTokenIfNotExpired() throws Exception {
    mockServer.enqueue(validTokenResponse);
    mockServer.enqueue(checkRunResponse);
    mockServer.enqueue(checkRunResponse);

    checksClient.getCheckRun(123).join();
    checksClient.getCheckRun(123).join();

    // One to get the token, 2 checks
    assertThat(mockServer.getRequestCount(), is(3));

    assertThat(mockServer.takeRequest().getPath(), is("/app/installations/1/access_tokens"));
    assertThat(mockServer.takeRequest().getPath(), is("/repos/foo/bar/check-runs/123"));
    assertThat(mockServer.takeRequest().getPath(), is("/repos/foo/bar/check-runs/123"));
  }

  @Test
  public void fetchesANewInstallationTokenIfExpired() throws Exception {
    mockServer.enqueue(expiredTokenResponse);
    mockServer.enqueue(checkRunResponse);
    mockServer.enqueue(validTokenResponse);
    mockServer.enqueue(checkRunResponse);

    checksClient.getCheckRun(123).join();
    checksClient.getCheckRun(123).join();

    // 2 to get the token, 2 checks
    assertThat(mockServer.getRequestCount(), is(4));

    assertThat(mockServer.takeRequest().getPath(), is("/app/installations/1/access_tokens"));
    assertThat(mockServer.takeRequest().getPath(), is("/repos/foo/bar/check-runs/123"));
    assertThat(mockServer.takeRequest().getPath(), is("/app/installations/1/access_tokens"));
    assertThat(mockServer.takeRequest().getPath(), is("/repos/foo/bar/check-runs/123"));
  }

  @Test
  public void throwsIfFetchingInstallationTokenRequestIsUnsuccessful() throws Exception {
    mockServer.enqueue(new MockResponse().setResponseCode(500));
    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> checksClient.getCheckRun(123).join());

    assertThat(ex.getMessage(), is("Could not generate access token for github app"));

    assertThat(ex.getCause(), is(notNullValue()));
    assertThat(ex.getCause().getMessage(), startsWith("Got non-2xx status 500 when getting an access token from GitHub"));

    RecordedRequest recordedRequest = mockServer.takeRequest(1, TimeUnit.MILLISECONDS);
    // make sure it was the expected request that threw
    assertThat(recordedRequest.getRequestUrl().encodedPath(), is("/app/installations/1/access_tokens"));
  }

  @Test
  public void assertJwtEndpointOnlyUsesJwt() throws Exception {
    mockServer.enqueue(new MockResponse().setBody("[]"));
    final GitHubClient github = GitHubClient.create(client, url, key, 123);
    github.createRepositoryClient("foo", "bar").createGithubAppClient().getInstallations().join();

    // Only one call to the API, using the JWT Token
    assertThat(mockServer.getRequestCount(), is(1));

    final RecordedRequest request = mockServer.takeRequest();

    assertThat(request.getPath(), startsWith("/app/installations"));
    assertThat(request.getHeaders().values("Accept"), hasItem("application/vnd.github.machine-man-preview+json"));
    assertThat(request.getMethod(), is("GET"));
  }

  @Test
  public void assertChecksApiContainsCorrectHeader() throws Exception {
    mockServer.enqueue(validTokenResponse);
    mockServer.enqueue(checkRunResponse);

    checksClient.updateCheckRun(12, null).join();
    assertThat(mockServer.getRequestCount(), is(2));

    assertThat(mockServer.takeRequest().getPath(), is("/app/installations/1/access_tokens"));

    final RecordedRequest request2 = mockServer.takeRequest();

    assertThat(request2.getPath(), is("/repos/foo/bar/check-runs/12"));
    assertThat(request2.getHeaders().values("Accept"), hasItem("application/vnd.github.antiope-preview+json"));
    assertThat(request2.getMethod(), is("PATCH"));
  }

  @Test
  public void assertInstallationEndpointWithoutInstallationThrows() {
    final GitHubClient github = GitHubClient.create(client, url, key, 123);
    final RuntimeException ex = assertThrows(RuntimeException.class,
        () -> github.createRepositoryClient("foo", "bar").createChecksApiClient().getCheckRun(123)
            .join());
    assertThat(ex.getMessage(), is("This endpoint needs a client with an installation ID"));
  }

  @Test
  public void assertJwtEndpointWithNoKeyThrows() {
    final GitHubClient github = GitHubClient.create(client, url, "a-token");

    final IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> github.createRepositoryClient("foo", "bar").createGithubAppClient().getInstallations()
            .join());

    assertThat(ex.getMessage(), is("This endpoint needs a client with a private key for an App"));
  }

  @Test
  public void assertNoPrivateKeyProvidedUsesAccessToken() throws Exception {
    mockServer.enqueue(new MockResponse().setBody(loadResource("com/spotify/github/v3/repos/commit.json")));
    final GitHubClient github = GitHubClient.create(client, url, "some-token");

    github.createRepositoryClient("org", "repo").getCommit("some-sha").join();

    assertThat(mockServer.getRequestCount(), is(1));
    final RecordedRequest request = mockServer.takeRequest();

    assertThat(
        request.getPath(), is("/repos/org/repo/commits/some-sha"));
    assertThat(request.getHeader("Accept"), is("application/json"));
    assertThat(request.getHeader("Authorization"), is("token some-token"));
    assertThat(request.getMethod(), is("GET"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void assertNoTokenThrowsException() {
    final GitHubClient apiWithNoKey = GitHubClient.create(URI.create("someurl"), "a-token");
    apiWithNoKey.createRepositoryClient("foo", "bar").createChecksApiClient();
  }

  private AccessToken getTestInstallationToken() {
    return ImmutableAccessToken.builder()
        .token("installation-token")
        .expiresAt(ZonedDateTime.now().plusHours(1))
        .build();
  }
}
