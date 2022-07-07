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

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import com.spotify.github.v3.git.Reference;
import com.spotify.github.v3.prs.ImmutableRequestReviewParameters;
import com.spotify.github.v3.prs.PullRequest;
import com.spotify.github.v3.prs.ReviewRequests;
import com.spotify.github.v3.prs.requests.ImmutablePullRequestCreate;
import com.spotify.github.v3.prs.requests.ImmutablePullRequestUpdate;
import com.spotify.github.v3.prs.requests.PullRequestCreate;
import com.spotify.github.v3.prs.requests.PullRequestUpdate;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PullRequestClientTest {

  private GitHubClient github;
  private OkHttpClient client;

  private static String getFixture(String resource) throws IOException {
    return Resources.toString(getResource(PullRequestClientTest.class, resource), defaultCharset());
  }

  @Before
  public void setUp() {
    client = mock(OkHttpClient.class);
    github = GitHubClient.create(client, URI.create("http://bogus"), "token");
  }

  @Test
  public void createPullRequest() throws Exception {
    final String title = "Amazing new feature";
    final String body = "Please pull these awesome changes in!";
    final String head = "octocat:new-topic";
    final String base = "master";

    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> capture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(capture.capture());

    final Response response =
        new Response.Builder()
            .code(201)
            .protocol(Protocol.HTTP_1_1)
            .message("Created")
            .body(
                ResponseBody.create(
                    MediaType.get("application/json"),
                    getFixture("pull_request.json")))
            .request(new Request.Builder().url("http://localhost/").build())
            .build();

    when(client.newCall(any())).thenReturn(call);

    final PullRequestClient pullRequestClient =
        PullRequestClient.create(github, "owner", "repo");

    final PullRequestCreate request = ImmutablePullRequestCreate.builder().title(title).body(body)
        .head(head).base(base).build();

    final CompletableFuture<PullRequest> result = pullRequestClient.create(request);

    capture.getValue().onResponse(call, response);

    PullRequest pullRequest = result.get();

    assertThat(pullRequest.title(), is(title));
    assertThat(pullRequest.body().get(), is(body));
    assertThat(pullRequest.head().label().get(), is(head));
    assertThat(pullRequest.base().ref(), is(base));
  }

  @Test
  public void updatePullRequest() throws Exception {
    final String title = "Amazing new feature";
    final String body = "Please pull these awesome changes in!";

    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> capture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(capture.capture());

    final Response response =
        new Response.Builder()
            .code(200)
            .protocol(Protocol.HTTP_1_1)
            .message("OK")
            .body(
                ResponseBody.create(
                    MediaType.get("application/json"),
                    getFixture("pull_request.json")))
            .request(new Request.Builder().url("http://localhost/").build())
            .build();

    when(client.newCall(any())).thenReturn(call);

    final PullRequestClient pullRequestClient =
        PullRequestClient.create(github, "owner", "repo");

    final PullRequestUpdate request = ImmutablePullRequestUpdate.builder().title(title).body(body)
        .build();

    final CompletableFuture<PullRequest> result = pullRequestClient.update(1, request);

    capture.getValue().onResponse(call, response);

    PullRequest pullRequest = result.get();

    assertThat(pullRequest.title(), is(title));
    assertThat(pullRequest.body().get(), is(body));
  }

  @Test
  public void testListReviewRequests() throws Throwable {
    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> capture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(capture.capture());

    final Response response =
        new Response.Builder()
            .code(200)
            .protocol(Protocol.HTTP_1_1)
            .message("OK")
            .body(
                ResponseBody.create(
                    MediaType.get("application/json"),
                    getFixture("requestedReviews.json")))
            .request(new Request.Builder().url("http://localhost/").build())
            .build();

    when(client.newCall(any())).thenReturn(call);

    final PullRequestClient pullRequestClient =
        PullRequestClient.create(github, "owner", "repo");

    final CompletableFuture<ReviewRequests> result =
        pullRequestClient.listReviewRequests(1);

    capture.getValue().onResponse(call, response);

    ReviewRequests reviewRequests = result.get();

    assertEquals(1, reviewRequests.users().size());
    assertEquals("octocat", reviewRequests.users().get(0).login());
    assertEquals(1, reviewRequests.teams().size());
    assertEquals("justice-league", reviewRequests.teams().get(0).slug());
  }

  @Test
  public void testRemoveRequestedReview() throws Throwable {

    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> capture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(capture.capture());

    final Response response =
        new Response.Builder()
            .code(200)
            .protocol(Protocol.HTTP_1_1)
            .message("OK")
            .request(new Request.Builder().url("http://localhost/").build())
            .build();

    when(client.newCall(any())).thenReturn(call);

    PullRequestClient pullRequestClient =
        PullRequestClient.create(github, "owner", "repo");

    CompletableFuture<Void> result =
        pullRequestClient.removeRequestedReview(1, ImmutableRequestReviewParameters.builder()
            .reviewers(ImmutableList.of("user1", "user2"))
            .build());

    capture.getValue().onResponse(call, response);

    result.get();
    // Passes without throwing
  }

  @Test(expected = RequestNotOkException.class)
  public void testRemoveRequestedReview_failure() throws Throwable {

    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> capture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(capture.capture());

    final Response response =
        new Response.Builder()
            .code(400)
            .protocol(Protocol.HTTP_1_1)
            .message("Failed")
            .request(new Request.Builder().url("http://localhost/").build())
            .build();

    when(client.newCall(any())).thenReturn(call);

    PullRequestClient pullRequestClient =
        PullRequestClient.create(github, "owner", "repo");

    CompletableFuture<Void> result =
        pullRequestClient.removeRequestedReview(1, ImmutableRequestReviewParameters.builder()
            .reviewers(ImmutableList.of("user1", "user2"))
            .build());

    capture.getValue().onResponse(call, response);

    try {
      result.get();
    } catch (ExecutionException e) {
      throw e.getCause();
      // expecting RequestNotOkException
    }
  }
}
