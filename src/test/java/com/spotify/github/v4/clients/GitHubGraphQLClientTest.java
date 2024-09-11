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

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.spotify.github.Tracer;
import com.spotify.github.graphql.models.*;
import com.spotify.github.http.ImmutableGitHubClientConfig;
import com.spotify.github.v3.clients.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class GitHubGraphQLClientTest {

  private GithubGraphQLClient github;
  private OkHttpClient client;

  private static String getFixture(String resource) throws IOException {
    return Resources.toString(
        getResource(GitHubGraphQLClientTest.class, resource), defaultCharset());
  }

  @BeforeEach
  public void setUp() {
    client = mock(OkHttpClient.class);
    github =
        GithubGraphQLClient.create(
            ImmutableGitHubClientConfig.builder()
                .client(client)
                .graphqlApiUrl(URI.create("http://bogus/graphql"))
                .accessToken("token")
                .build());
  }

  @Test
  public void testMutationGraphQL() throws ExecutionException, InterruptedException, IOException {
    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> callbackCapture = ArgumentCaptor.forClass(Callback.class);
    final ArgumentCaptor<Request> requestCapture = ArgumentCaptor.forClass(Request.class);
    doNothing().when(call).enqueue(callbackCapture.capture());

    EnablePullRequestAutoMergeMutationRequest request =
        EnablePullRequestAutoMergeMutationRequest.builder()
            .setInput(
                EnablePullRequestAutoMergeInput.builder()
                    .setPullRequestId("TEST_PR_ID")
                    .setMergeMethod(PullRequestMergeMethod.SQUASH)
                    .build())
            .build();

    EnablePullRequestAutoMergePayloadResponseProjection projection =
        new EnablePullRequestAutoMergePayloadResponseProjection()
            .clientMutationId()
            .pullRequest(new PullRequestResponseProjection().id().title());

    when(client.newCall(requestCapture.capture())).thenReturn(call);

    String fixture = getFixture("../clients/graphql_enable_pr_auto_merge_response.json");

    final Response response =
        new Response.Builder()
            .code(200)
            .body(ResponseBody.create(MediaType.get("application/json"), fixture))
            .message("")
            .protocol(Protocol.HTTP_1_1)
            .request(new Request.Builder().url("http://localhost/").build())
            .build();
    CompletableFuture<Response> future = github.queryGraphQL(request, projection);
    callbackCapture.getValue().onResponse(call, response);
    var result = future.get();

    assertThat(result.code(), is(200));
    ObjectMapper mapper = new ObjectMapper();

    JsonNode expectedTree = mapper.readTree(result.body().string());
    JsonNode actualTree = mapper.readTree(fixture);

    assertThat(actualTree, is(expectedTree));

    Request capturedRequest = requestCapture.getValue();
    final Buffer buffer = new Buffer();
    capturedRequest.body().writeTo(buffer);
    String requestBody = buffer.readUtf8();

    assertThat(capturedRequest.url().toString(), is("http://bogus/graphql"));
    assertThat(capturedRequest.method(), is("POST"));
    assertThat(
        requestBody,
        is(
            "mutation enablePullRequestAutoMerge { enablePullRequestAutoMerge: enablePullRequestAutoMerge(input: { mergeMethod: SQUASH, pullRequestId: \"TEST_PR_ID\" }){ clientMutationId pullRequest { id title } } }"));
  }

  @Test
  public void testQueryGraphQL() throws ExecutionException, InterruptedException, IOException {
    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> callbackCapture = ArgumentCaptor.forClass(Callback.class);
    final ArgumentCaptor<Request> requestCapture = ArgumentCaptor.forClass(Request.class);
    doNothing().when(call).enqueue(callbackCapture.capture());

    RepositoryResponseProjection projection =
        new RepositoryResponseProjection().id().nameWithOwner();

    RepositoryQueryRequest request =
        RepositoryQueryRequest.builder().setOwner("test-org").setName("test-repo").build();

    when(client.newCall(requestCapture.capture())).thenReturn(call);
    String fixture = getFixture("../clients/graphql_enable_pr_auto_merge_response.json");
    final Response response =
        new Response.Builder()
            .code(200)
            .body(ResponseBody.create(MediaType.get("application/json"), fixture))
            .message("")
            .protocol(Protocol.HTTP_1_1)
            .request(new Request.Builder().url("http://localhost/").build())
            .build();
    CompletableFuture<Response> future = github.queryGraphQL(request, projection);
    callbackCapture.getValue().onResponse(call, response);
    var result = future.get();

    assertThat(result.code(), is(200));
    ObjectMapper mapper = new ObjectMapper();

    JsonNode expectedResponseTree = mapper.readTree(result.body().string());
    JsonNode actualResponseTree = mapper.readTree(fixture);

    assertThat(expectedResponseTree, is(actualResponseTree));

    Request capturedRequest = requestCapture.getValue();
    final Buffer buffer = new Buffer();
    capturedRequest.body().writeTo(buffer);
    String requestBody = buffer.readUtf8();

    assertThat(capturedRequest.url().toString(), is("http://bogus/graphql"));
    assertThat(capturedRequest.method(), is("POST"));
    assertThat(
        requestBody,
        is(
            "query repository { repository: repository(followRenames: true, name: \"test-repo\", owner: \"test-org\"){ id nameWithOwner } }"));
  }
}
