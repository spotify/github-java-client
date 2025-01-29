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

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.common.io.Resources;
import com.spotify.github.Tracer;
import com.spotify.github.v3.checks.CheckSuiteResponseList;
import com.spotify.github.v3.checks.Installation;
import com.spotify.github.v3.exceptions.ReadOnlyRepositoryException;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import com.spotify.github.v3.repos.CommitItem;
import com.spotify.github.v3.repos.RepositoryInvitation;
import com.spotify.github.v3.workflows.WorkflowsResponse;
import com.spotify.github.v3.workflows.WorkflowsState;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class GitHubClientTest {

  private GitHubClient github;
  private OkHttpClient client;
  private Tracer tracer = mock(Tracer.class);

  private static String getFixture(String resource) throws IOException {
    return Resources.toString(getResource(GitHubClientTest.class, resource), defaultCharset());
  }

  @BeforeEach
  public void setUp() {
    client = mock(OkHttpClient.class);
    github = GitHubClient.create(client, URI.create("http://bogus"), "token");
  }

  @Test
  public void withScopedInstallationIdShouldFailWhenMissingPrivateKey() {
    assertThrows(RuntimeException.class, () -> github.withScopeForInstallationId(1));
  }

  @Test
  public void testWithScopedInstallationId() throws URISyntaxException {
    GitHubClient org = GitHubClient.create(new URI("http://apa.bepa.cepa"), "some_key_content".getBytes(), null, null);
    GitHubClient scoped = org.withScopeForInstallationId(1);
    Assertions.assertTrue(scoped.getPrivateKey().isPresent());
    Assertions.assertEquals(org.getPrivateKey().get(), scoped.getPrivateKey().get());
  }

  @Test
  public void testSearchIssue() throws Throwable {

    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> capture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(capture.capture());

    final Response response =
        new okhttp3.Response.Builder()
            .code(403)
            .body(
                ResponseBody.create(
                    MediaType.get("application/json"),
                    "{\"message\":\"Repository "
                        + "was archived so is "
                        + "read-only.\","
                        + "\"documentation_url"
                        + "\":\"https://developer"
                        + ".github.com/enterprise/2"
                        + ".12/v3/repos/comments"
                        + "/#update-a-commit-comment"
                        + "\"}"))
            .message("foo")
            .protocol(Protocol.HTTP_1_1)
            .request(new Request.Builder().url("http://localhost/").build())
            .build();

    when(client.newCall(any())).thenReturn(call);
    IssueClient issueClient =
        github.withTracer(tracer).createRepositoryClient("testorg", "testrepo").createIssueClient();

    CompletableFuture<Void> maybeSucceeded = issueClient.editComment(1, "some comment");
    capture.getValue().onResponse(call, response);
    verify(tracer,times(1)).span(anyString(), anyString(),any());

    Exception exception = assertThrows(ExecutionException.class,
        maybeSucceeded::get);
    Assertions.assertEquals(ReadOnlyRepositoryException.class, exception.getCause().getClass());
  }

  @Test
  public void testRequestNotOkException() throws Throwable {
    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> capture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(capture.capture());

    final Response response = new okhttp3.Response.Builder()
        .code(409) // Conflict
        .headers(Headers.of("x-ratelimit-remaining", "0"))
        .body(
            ResponseBody.create(
                MediaType.get("application/json"),
                "{\n  \"message\": \"Merge Conflict\"\n}"
            ))
        .message("")
        .protocol(Protocol.HTTP_1_1)
        .request(new Request.Builder().url("http://localhost/").build())
        .build();

    when(client.newCall(any())).thenReturn(call);
    RepositoryClient repoApi = github.createRepositoryClient("testorg", "testrepo");

    CompletableFuture<Optional<CommitItem>> future = repoApi.merge("basebranch", "headbranch");
    capture.getValue().onResponse(call, response);
    try {
      future.get();
      Assertions.fail("Did not throw");
    } catch (ExecutionException e) {
      assertThat(e.getCause() instanceof RequestNotOkException, is(true));
      RequestNotOkException e1 = (RequestNotOkException) e.getCause();
      assertThat(e1.statusCode(), is(409));
      assertThat(e1.method(), is("POST"));
      assertThat(e1.path(), is("/repos/testorg/testrepo/merges"));
      assertThat(e1.headers(), hasEntry("x-ratelimit-remaining", List.of("0")));
      assertThat(e1.getMessage(), containsString("POST"));
      assertThat(e1.getMessage(), containsString("/repos/testorg/testrepo/merges"));
      assertThat(e1.getMessage(), containsString("Merge Conflict"));
      assertThat(e1.getRawMessage(), containsString("Merge Conflict"));
    }
  }

  @Test
  public void testPutConvertsToClass() throws Throwable {
    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> callbackCapture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(callbackCapture.capture());

    final ArgumentCaptor<Request> requestCapture = ArgumentCaptor.forClass(Request.class);
    when(client.newCall(requestCapture.capture())).thenReturn(call);

    final Response response =
        new okhttp3.Response.Builder()
            .code(200)
            .body(
                ResponseBody.create(
                    MediaType.get("application/json"), getFixture("repository_invitation.json")))
            .message("")
            .protocol(Protocol.HTTP_1_1)
            .request(new Request.Builder().url("http://localhost/").build())
            .build();

    CompletableFuture<RepositoryInvitation> future = github.put("collaborators/", "",
        RepositoryInvitation.class);
    callbackCapture.getValue().onResponse(call, response);

    RepositoryInvitation invitation = future.get();
    assertThat(requestCapture.getValue().method(), is("PUT"));
    assertThat(requestCapture.getValue().url().toString(), is("http://bogus/collaborators/"));
    assertThat(invitation.id(), is(1));
  }

  @Test
  public void testGetCheckSuites() throws Throwable {

    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> callbackCapture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(callbackCapture.capture());

    final Response response = new okhttp3.Response.Builder()
        .code(200)
        .body(
            ResponseBody.create(
                MediaType.get("application/json"), getFixture("../checks/check-suites-response.json")))
        .message("")
        .protocol(Protocol.HTTP_1_1)
        .request(new Request.Builder().url("http://localhost/").build())
        .build();

    when(client.newCall(any())).thenReturn(call);
    ChecksClient client = github.createChecksClient("testorg", "testrepo");

    CompletableFuture<CheckSuiteResponseList> future = client.getCheckSuites("sha");
    callbackCapture.getValue().onResponse(call, response);
    var result = future.get();

    assertThat(result.totalCount(), is(1));
    assertThat(result.checkSuites().get(0).app().get().slug().get(), is("octoapp"));

  }

  @Test
  public void testGetWorkflow() throws Throwable {
    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> callbackCapture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(callbackCapture.capture());

    final Response response = new okhttp3.Response.Builder()
        .code(200)
        .body(
            ResponseBody.create(
                MediaType.get("application/json"),
                getFixture("../workflows/workflows-get-workflow-response.json")))
        .message("")
        .protocol(Protocol.HTTP_1_1)
        .request(new Request.Builder().url("http://localhost/").build())
        .build();

    when(client.newCall(any())).thenReturn(call);
    WorkflowsClient client = github.withTracer(tracer).createRepositoryClient("testorg", "testrepo")
        .createActionsClient().createWorkflowsClient();

    CompletableFuture<WorkflowsResponse> future = client.getWorkflow(161335);
    callbackCapture.getValue().onResponse(call, response);
    var result = future.get();

    assertThat(result.id(), is(161335));
    assertThat(result.state(), is(WorkflowsState.active));
  }

  @Test
  void asAppScopedClientGetsUserClientIfOrgClientNotFound() {
    var appGithub = GitHubClient.create(client, URI.create("http://bogus"), new byte[] {}, 1);
    var githubSpy = spy(appGithub);

    var orgClientMock = mock(OrganisationClient.class);
    when(githubSpy.createOrganisationClient("owner")).thenReturn(orgClientMock);

    var appClientMock = mock(GithubAppClient.class);
    when(orgClientMock.createGithubAppClient()).thenReturn(appClientMock);
    when(appClientMock.getInstallation()).thenReturn(failedFuture(new RequestNotOkException("", "", 404, "", new HashMap<>())));

    var userClientMock = mock(UserClient.class);
    when(githubSpy.createUserClient("owner")).thenReturn(userClientMock);

    var appClientMock2 = mock(GithubAppClient.class);
    when(userClientMock.createGithubAppClient()).thenReturn(appClientMock2);

    var installationMock = mock(Installation.class);
    when(appClientMock2.getUserInstallation()).thenReturn(completedFuture(installationMock));
    when(installationMock.id()).thenReturn(1);

    var maybeScopedClient = githubSpy.asAppScopedClient("owner").toCompletableFuture().join();

    Assertions.assertTrue(maybeScopedClient.isPresent());
    verify(githubSpy, times(1)).createOrganisationClient("owner");
    verify(githubSpy, times(1)).createUserClient("owner");
  }

  @Test
  void asAppScopedClientReturnsEmptyIfNoInstallation() {
    var appGithub = GitHubClient.create(client, URI.create("http://bogus"), new byte[] {}, 1);
    var githubSpy = spy(appGithub);

    var orgClientMock = mock(OrganisationClient.class);
    when(githubSpy.createOrganisationClient("owner")).thenReturn(orgClientMock);

    var appClientMock = mock(GithubAppClient.class);
    when(orgClientMock.createGithubAppClient()).thenReturn(appClientMock);
    when(appClientMock.getInstallation()).thenReturn(failedFuture(new RequestNotOkException("", "", 404, "", new HashMap<>())));

    var userClientMock = mock(UserClient.class);
    when(githubSpy.createUserClient("owner")).thenReturn(userClientMock);

    var appClientMock2 = mock(GithubAppClient.class);
    when(userClientMock.createGithubAppClient()).thenReturn(appClientMock2);

    var installationMock = mock(Installation.class);
    when(appClientMock2.getUserInstallation()).thenReturn(failedFuture(new RequestNotOkException("", "", 404, "", new HashMap<>())));
    when(installationMock.id()).thenReturn(1);

    var maybeScopedClient = githubSpy.asAppScopedClient("owner").toCompletableFuture().join();
    Assertions.assertTrue(maybeScopedClient.isEmpty());
  }
}
