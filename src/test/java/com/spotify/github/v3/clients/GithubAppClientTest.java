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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.github.FixtureHelper;
import com.spotify.github.v3.apps.InstallationRepositoriesResponse;
import com.spotify.github.v3.checks.Installation;
import java.io.File;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class GithubAppClientTest {

  @Rule
  public final MockWebServer mockServer = new MockWebServer();

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final int appId = 42;
  private GithubAppClient client;

  @Before
  public void setUp() throws Exception {
    URI uri = mockServer.url("").uri();
    File key = FixtureHelper.loadFile("githubapp/key.pem");

    GitHubClient rootclient = GitHubClient.create(uri, key, appId);
    client = rootclient.createRepositoryClient("", "").createGithubAppClient();
  }

  @Test
  public void getInstallationsList() throws Exception {
    mockServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setBody(FixtureHelper.loadFixture("githubapp/installations-list.json")));

    List<Installation> installations = client.getInstallations().join();

    assertThat(installations.size(), is(2));
    assertThat(installations.get(0).account().login(), is("github"));
    assertThat(installations.get(0).id(), is(1));
    assertThat(installations.get(1).account().login(), is("octocat"));
    assertThat(installations.get(1).id(), is(3));

    RecordedRequest recordedRequest = mockServer.takeRequest(1, TimeUnit.MILLISECONDS);
    assertThat(recordedRequest.getRequestUrl().encodedPath(), is("/app/installations"));
    assertThat(recordedRequest.getRequestUrl().queryParameter("per_page"), is("100"));

    assertThat(
        recordedRequest.getHeaders().values("Accept"),
        containsInAnyOrder("application/json", "application/vnd.github.machine-man-preview+json"));
  }

  @Test
  public void listAccessibleRepositories() throws Exception {
    // response for POST /app/installations/:id/access_tokens
    final String installationAccessToken = "abc123-secret";
    mockServer.enqueue(
        new MockResponse()
            .setResponseCode(201)
            // this might not serialize 100% the same as the Json class's ObjectMapper but should be
            // fine for this test
            .setBody(
                objectMapper
                    .createObjectNode()
                    .put("token", installationAccessToken)
                    .put("expires_at", ZonedDateTime.now().plusHours(1).toString())
                    .toString()));

    // response for GET /installation/repositories
    mockServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setBody(FixtureHelper.loadFixture("githubapp/accessible-repositories.json")));

    InstallationRepositoriesResponse response =
        client.listAccessibleRepositories(1234).join();

    assertThat(response.totalCount(), is(2));
    assertThat(response.repositories().size(), is(2));
    assertThat(response.repositories().get(0).id(), is(1));
    assertThat(response.repositories().get(1).id(), is(2));

    RecordedRequest accessTokenRequest = mockServer.takeRequest(1, TimeUnit.MILLISECONDS);
    assertThat(accessTokenRequest.getMethod(), is("POST"));
    assertThat(
        accessTokenRequest.getRequestUrl().encodedPath(),
        is("/app/installations/1234/access_tokens"));

    RecordedRequest listReposRequest = mockServer.takeRequest(1, TimeUnit.MILLISECONDS);
    assertThat(listReposRequest.getMethod(), is("GET"));
    assertThat(listReposRequest.getRequestUrl().encodedPath(), is("/installation/repositories"));
  }
}
