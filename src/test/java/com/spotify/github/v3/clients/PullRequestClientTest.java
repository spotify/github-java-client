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

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import static java.nio.charset.Charset.defaultCharset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import static com.google.common.io.Resources.getResource;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import com.spotify.github.v3.prs.Comment;
import com.spotify.github.v3.prs.ImmutableRequestReviewParameters;
import com.spotify.github.v3.prs.PullRequest;
import com.spotify.github.v3.prs.ReviewRequests;
import com.spotify.github.v3.prs.requests.ImmutablePullRequestCreate;
import com.spotify.github.v3.prs.requests.ImmutablePullRequestUpdate;
import com.spotify.github.v3.prs.requests.PullRequestCreate;
import com.spotify.github.v3.prs.requests.PullRequestUpdate;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PullRequestClientTest {

  private GitHubClient github;
  private OkHttpClient client;

  private static String getFixture(String resource) throws IOException {
    return Resources.toString(getResource(PullRequestClientTest.class, resource), defaultCharset());
  }

  @BeforeEach
  public void setUp() {
    client = mock(OkHttpClient.class);
    github = GitHubClient.create(client, URI.create("http://bogus"), URI.create("https://bogus/graphql"), "token");
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

    final CompletableFuture<PullRequest> result = pullRequestClient.update(1L, request);

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
        pullRequestClient.listReviewRequests(1L);

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
        pullRequestClient.removeRequestedReview(1L, ImmutableRequestReviewParameters.builder()
            .reviewers(ImmutableList.of("user1", "user2"))
            .build());

    capture.getValue().onResponse(call, response);

    result.get();
    // Passes without throwing
  }

  @Test
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
        pullRequestClient.removeRequestedReview(1L, ImmutableRequestReviewParameters.builder()
            .reviewers(ImmutableList.of("user1", "user2"))
            .build());

    capture.getValue().onResponse(call, response);

    Exception exception = assertThrows(ExecutionException.class, result::get);
    assertEquals(RequestNotOkException.class, exception.getCause().getClass());
  }

  @Test
  public void testGetPatch() throws Throwable {
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
                    MediaType.get("application/vnd.github.patch"),
                    getFixture("patch.txt")))
            .request(new Request.Builder().url("http://localhost/").build())
            .build();

    when(client.newCall(any())).thenReturn(call);

    final PullRequestClient pullRequestClient =
        PullRequestClient.create(github, "owner", "repo");

    final CompletableFuture<Reader> result =
        pullRequestClient.patch(1L);

    capture.getValue().onResponse(call, response);

    Reader patchReader = result.get();

    assertEquals(getFixture("patch.txt"), IOUtils.toString(patchReader));
  }

  @Test
  public void testGetDiff() throws Throwable {
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
                    MediaType.get("application/vnd.github.diff"),
                    getFixture("diff.txt")))
            .request(new Request.Builder().url("http://localhost/").build())
            .build();

    when(client.newCall(any())).thenReturn(call);

    final PullRequestClient pullRequestClient =
        PullRequestClient.create(github, "owner", "repo");

    final CompletableFuture<Reader> result =
        pullRequestClient.diff(1L);

    capture.getValue().onResponse(call, response);

    Reader diffReader = result.get();

    assertEquals(getFixture("diff.txt"), IOUtils.toString(diffReader));
  }

  @Test
  public void testCreateCommentReply() throws Throwable {
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
                    getFixture("pull_request_review_comment_reply.json")))
            .request(new Request.Builder().url("http://localhost/").build())
            .build();

    when(client.newCall(any())).thenReturn(call);

    final PullRequestClient pullRequestClient =
        PullRequestClient.create(github, "owner", "repo");

    final String replyBody = "Thanks for the feedback!";
    final CompletableFuture<Comment> result =
        pullRequestClient.createCommentReply(1L, 123L, replyBody);

    capture.getValue().onResponse(call, response);

    Comment comment = result.get();

    assertThat(comment.body(), is("Great stuff!"));
    assertThat(comment.id(), is(10L));
    assertThat(comment.diffHunk(), is("@@ -16,33 +16,40 @@ public class Connection : IConnection..."));
    assertThat(comment.path(), is("file1.txt"));
    assertThat(comment.position(), is(1));
    assertThat(comment.originalPosition(), is(4));
    assertThat(comment.commitId(), is("6dcb09b5b57875f334f61aebed695e2e4193db5e"));
    assertThat(comment.originalCommitId(), is("9c48853fa3dc5c1c3d6f1f1cd1f2743e72652840"));
    assertThat(comment.inReplyToId(), is(426899381L));
    assertThat(comment.authorAssociation(), is("NONE"));
    assertThat(comment.user().login(), is("octocat"));
    assertThat(comment.startLine(), is(1));
    assertThat(comment.originalStartLine(), is(1));
    assertThat(comment.startSide(), is("RIGHT"));
    assertThat(comment.line(), is(2));
    assertThat(comment.originalLine(), is(2));
    assertThat(comment.side(), is("RIGHT"));
    assertThat(comment.pullRequestReviewId(), is(42L));
  }
}
