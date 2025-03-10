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
import static com.spotify.github.FixtureHelper.loadFixture;
import static com.spotify.github.v3.clients.IssueClient.*;
import static com.spotify.github.v3.clients.MockHelper.createMockResponse;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.spotify.github.async.Async;
import com.spotify.github.async.AsyncPage;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.ImmutableUser;
import com.spotify.github.v3.comment.Comment;
import com.spotify.github.v3.comment.CommentReaction;
import com.spotify.github.v3.comment.CommentReactionContent;
import com.spotify.github.v3.comment.ImmutableCommentReaction;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import com.spotify.github.v3.issues.Issue;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class IssueClientTest {

  private GitHubClient github;
  private IssueClient issueClient;
  private Json json;

  @BeforeEach
  public void setUp() {
    json = Json.create();
    github = mock(GitHubClient.class);
    when(github.json()).thenReturn(json);
    when(github.urlFor("")).thenReturn("https://github.com/api/v3");
    issueClient = new IssueClient(github, "someowner", "somerepo");
  }

  @Test
  public void testCommentPaginationSpliterator() throws IOException {
    final String firstPageLink =
        "<https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments?page=2>; rel=\"next\", <https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments?page=2>; rel=\"last\"";
    final String firstPageBody =
        Resources.toString(getResource(this.getClass(), "comments_page1.json"), defaultCharset());
    final Response firstPageResponse = createMockResponse(firstPageLink, firstPageBody);

    final String lastPageLink =
        "<https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments>; rel=\"first\", <https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments>; rel=\"prev\"";
    final String lastPageBody =
        Resources.toString(getResource(this.getClass(), "comments_page2.json"), defaultCharset());
    final Response lastPageResponse = createMockResponse(lastPageLink, lastPageBody);

    when(github.request(format(COMMENTS_URI_NUMBER_TEMPLATE, "someowner", "somerepo", "123")))
        .thenReturn(completedFuture(firstPageResponse));
    when(github.request(
            format(COMMENTS_URI_NUMBER_TEMPLATE + "?page=2", "someowner", "somerepo", "123")))
        .thenReturn(completedFuture(lastPageResponse));

    final Iterable<AsyncPage<Comment>> pageIterator = () -> issueClient.listComments(123);
    final List<Comment> listComments =
        Async.streamFromPaginatingIterable(pageIterator).collect(toList());

    assertThat(listComments.size(), is(30));
    assertThat(listComments.get(0).id(), is(1345268L));
    assertThat(listComments.get(listComments.size() - 1).id(), is(1356168L));
  }

  @Test
  public void testCommentPaginationForeach() throws IOException {
    final String firstPageLink =
        "<https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments?page=2>; rel=\"next\", <https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments?page=2>; rel=\"last\"";
    final String firstPageBody =
        Resources.toString(getResource(this.getClass(), "comments_page1.json"), defaultCharset());
    final Response firstPageResponse = createMockResponse(firstPageLink, firstPageBody);

    final String lastPageLink =
        "<https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments>; rel=\"first\", <https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments>; rel=\"prev\"";
    final String lastPageBody =
        Resources.toString(getResource(this.getClass(), "comments_page2.json"), defaultCharset());
    final Response lastPageResponse = createMockResponse(lastPageLink, lastPageBody);

    when(github.request(format(COMMENTS_URI_NUMBER_TEMPLATE, "someowner", "somerepo", "123")))
        .thenReturn(completedFuture(firstPageResponse));
    when(github.request(
            format(COMMENTS_URI_NUMBER_TEMPLATE + "?page=2", "someowner", "somerepo", "123")))
        .thenReturn(completedFuture(lastPageResponse));

    final List<Comment> listComments = Lists.newArrayList();
    issueClient
        .listComments(123)
        .forEachRemaining(
            page -> {
              page.iterator().forEachRemaining(listComments::add);
            });

    assertThat(listComments.size(), is(30));
    assertThat(listComments.get(0).id(), is(1345268L));
    assertThat(listComments.get(listComments.size() - 1).id(), is(1356168L));
  }

  @Test
  public void testCommentCreated() throws IOException {
    final String fixture = loadFixture("clients/comment_created.json");
    final Response response = createMockResponse("", fixture);
    final String path = format(COMMENTS_URI_NUMBER_TEMPLATE, "someowner", "somerepo", 10);
    when(github.post(anyString(), anyString(), eq(Comment.class))).thenCallRealMethod();
    when(github.post(eq(path), anyString())).thenReturn(completedFuture(response));
    final Comment comment = issueClient.createComment(10, "Me too").join();

    assertThat(comment.id(), is(114L));
  }

  @Test
  public void testCommentCreatedWithLargeId() throws IOException {
    final String fixture = loadFixture("clients/comment_created_long_id.json");
    final Response response = createMockResponse("", fixture);
    final String path = format(COMMENTS_URI_NUMBER_TEMPLATE, "someowner", "somerepo", 10);
    when(github.post(anyString(), anyString(), eq(Comment.class))).thenCallRealMethod();
    when(github.post(eq(path), anyString())).thenReturn(completedFuture(response));
    final Comment comment = issueClient.createComment(10, "Me too").join();

    assertThat(comment.id(), is(2459198527L));
  }

  @Test
  public void testGetIssue() throws IOException {
    final String fixture = loadFixture("issues/issue.json");
    final CompletableFuture<Issue> response = completedFuture(json.fromJson(fixture, Issue.class));
    final String path = format(ISSUES_URI_ID_TEMPLATE, "someowner", "somerepo", 2);
    when(github.request(eq(path), eq(Issue.class))).thenReturn(response);

    final var issue = issueClient.getIssue(2).join();

    assertThat(issue.id(), is(2));
    assertNotNull(issue.labels());
    assertFalse(issue.labels().isEmpty());
    assertThat(issue.labels().get(0).name(), is("bug"));
  }

  @ParameterizedTest
  @EnumSource(CommentReactionContent.class)
  public void testCreateIssueCommentReaction(CommentReactionContent reaction) {
    long commentId = 22369886;
    final CompletableFuture<CommentReaction> reactionResponse =
        completedFuture(
            ImmutableCommentReaction.builder()
                .id(42L)
                .content(reaction)
                .user(ImmutableUser.builder().login("octocat").build())
                .build());
    final String path = format(COMMENTS_REACTION_TEMPLATE, "someowner", "somerepo", commentId);
    final String requestBody =
        github.json().toJsonUnchecked(ImmutableMap.of("content", reaction.toString()));
    when(github.post(eq(path), eq(requestBody), eq(CommentReaction.class)))
        .thenReturn(reactionResponse);

    final var commentReaction = issueClient.createCommentReaction(commentId, reaction).join();

    assertThat(commentReaction.id(), is(42L));
    assertNotNull(commentReaction.user());
    assertThat(commentReaction.user().login(), is("octocat"));
    assertThat(commentReaction.content().toString(), is(reaction.toString()));
    verify(github, times(1)).post(eq(path), eq(requestBody), eq(CommentReaction.class));
  }

  @Test
  public void testDeleteIssueCommentReaction() {
    long issueNumber = 42;
    long reactionId = 385825;
    final String path =
        format(COMMENTS_REACTION_ID_TEMPLATE, "someowner", "somerepo", issueNumber, reactionId);
    Response mockResponse = mock(Response.class);
    when(mockResponse.code()).thenReturn(204);
    when(github.delete(eq(path))).thenReturn(completedFuture(mockResponse));

    final var response = issueClient.deleteCommentReaction(issueNumber, reactionId).join();

    assertThat(response.code(), is(204));
    assertThat(response, is(mockResponse));
    verify(github, times(1)).delete(eq(path));
  }

  @Test
  public void testListIssueCommentReaction() throws IOException {
    long commentId = 22369886;
    final CompletableFuture<List<CommentReaction>> listResponse =
        completedFuture(
            List.of(
                (ImmutableCommentReaction.builder()
                    .id(42L)
                    .content(CommentReactionContent.HEART)
                    .user(ImmutableUser.builder().login("octocat").build())
                    .build())));
    final String path = format(COMMENTS_REACTION_TEMPLATE, "someowner", "somerepo", commentId);

    final String firstPageLink =
        format(
            "<https://github.com/api/v3/repos/someowner/somerepo/comments/%s/reactions?page=1>; rel=\"last\"",
            commentId);
    final String firstPageBody = github.json().toJsonUnchecked(listResponse.join().toArray());
    final Response firstPageResponse = createMockResponse(firstPageLink, firstPageBody);

    when(github.request(eq(path))).thenReturn(completedFuture(firstPageResponse));
    final List<CommentReaction> listCommentReactions = Lists.newArrayList();
    issueClient
        .listCommentReaction(commentId)
        .forEachRemaining(
            page -> {
              page.iterator().forEachRemaining(listCommentReactions::add);
            });

    assertThat(listCommentReactions.size(), is(1));
    assertNotNull(listCommentReactions.get(0));
    assertThat(listCommentReactions.get(0).user().login(), is("octocat"));
    assertThat(
        listCommentReactions.get(0).content().toString(),
        is(CommentReactionContent.HEART.toString()));
    verify(github, atLeastOnce()).request(eq(path));
  }

  @Test
  public void testGetIssueNoIssue() {
    final String path = format(ISSUES_URI_ID_TEMPLATE, "someowner", "somerepo", 2);
    when(github.request(eq(path), eq(Issue.class)))
        .thenReturn(failedFuture(new RequestNotOkException("", "", 404, "", new HashMap<>())));

    assertThrows(CompletionException.class, () -> issueClient.getIssue(2).join());
  }
}
