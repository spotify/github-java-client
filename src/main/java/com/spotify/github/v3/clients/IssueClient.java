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

import static com.spotify.github.v3.clients.GitHubClient.*;

import com.google.common.collect.ImmutableMap;
import com.spotify.github.async.AsyncPage;
import com.spotify.github.http.HttpResponse;
import com.spotify.github.v3.comment.Comment;
import com.spotify.github.v3.comment.CommentReaction;
import com.spotify.github.v3.comment.CommentReactionContent;
import com.spotify.github.v3.issues.Issue;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Issue API client */
public class IssueClient {

  // URI templates for various API endpoints
  static final String COMMENTS_URI_NUMBER_TEMPLATE = "/repos/%s/%s/issues/%s/comments";
  static final String COMMENTS_URI_TEMPLATE = "/repos/%s/%s/issues/comments";
  static final String COMMENTS_URI_ID_TEMPLATE = "/repos/%s/%s/issues/comments/%s";
  static final String COMMENTS_REACTION_TEMPLATE = "/repos/%s/%s/issues/comments/%s/reactions";
  static final String COMMENTS_REACTION_ID_TEMPLATE = "/repos/%s/%s/issues/comments/%s/reactions/%s";
  static final String ISSUES_REACTION_TEMPLATE = "/repos/%s/%s/issues/%s/reactions";
  static final String ISSUES_REACTION_ID_TEMPLATE = "/repos/%s/%s/issues/%s/reactions/%s";
  static final String ISSUES_URI_ID_TEMPLATE = "/repos/%s/%s/issues/%s";
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final GitHubClient github;
  private final String owner;
  private final String repo;

  /**
   * Constructs an IssueClient.
   * @param github the GitHub client
   * @param owner the repository owner
   * @param repo the repository name
   */
  IssueClient(final GitHubClient github, final String owner, final String repo) {
    this.github = github;
    this.owner = owner;
    this.repo = repo;
  }

  /**
   * Creates an IssueClient.
   *
   * @param github the GitHub client
   * @param owner the repository owner
   * @param repo the repository name
   * @return a new IssueClient instance
   */
  static IssueClient create(final GitHubClient github, final String owner, final String repo) {
    return new IssueClient(github, owner, repo);
  }

  /**
   * Lists repository comments.
   *
   * @return an iterator of asynchronous pages of comments
   */
  public Iterator<AsyncPage<Comment>> listComments() {
    return listComments(String.format(COMMENTS_URI_TEMPLATE, owner, repo));
  }

  /**
   * Lists comments for a given issue number.
   *
   * @param issueNumber the issue number
   * @return an iterator of asynchronous pages of comments
   */
  public Iterator<AsyncPage<Comment>> listComments(final long issueNumber) {
    return listComments(String.format(COMMENTS_URI_NUMBER_TEMPLATE, owner, repo, issueNumber));
  }

  /**
   * Lists comments for a given issue number.
   *
   * @deprecated Use {@link #listComments(long)} instead
   * @param issueNumber the issue number
   * @return an iterator of asynchronous pages of comments
   */
  @Deprecated
  public Iterator<AsyncPage<Comment>> listComments(final int issueNumber) {
    return listComments((long) issueNumber);
  }

  /**
   * Gets a specific comment.
   *
   * @param commentId the comment id
   * @return a CompletableFuture containing the comment
   */
  public CompletableFuture<Comment> getComment(final long commentId) {
    final String path = String.format(COMMENTS_URI_ID_TEMPLATE, owner, repo, commentId);
    log.info("Fetching issue comments from " + path);
    return github.request(path, Comment.class);
  }

  /**
   * Gets a specific comment.
   *
   * @deprecated Use {@link #getComment(long)} instead
   * @param commentId the comment id
   * @return a CompletableFuture containing the comment
   */
  @Deprecated
  public CompletableFuture<Comment> getComment(final int commentId) {
    return getComment((long) commentId);
  }

  /**
   * Creates a comment for a given issue number.
   *
   * @param issueNumber the issue number
   * @param body the comment content
   * @return a CompletableFuture containing the created comment
   */
  public CompletableFuture<Comment> createComment(final long issueNumber, final String body) {
    final String path = String.format(COMMENTS_URI_NUMBER_TEMPLATE, owner, repo, issueNumber);
    final String requestBody = github.json().toJsonUnchecked(ImmutableMap.of("body", body));
    return github.post(path, requestBody, Comment.class);
  }

  /**
   * Creates a comment for a given issue number.
   *
   * @deprecated Use {@link #createComment(long, String)} instead
   * @param issueNumber the issue number
   * @param body the comment content
   * @return a CompletableFuture containing the created comment
   */
  @Deprecated
  public CompletableFuture<Comment> createComment(final int issueNumber, final String body) {
    return createComment((long) issueNumber, body);
  }

  /**
   * Edits a specific comment.
   *
   * @param commentId the comment id
   * @param body the new comment content
   * @return a CompletableFuture representing the completion of the operation
   */
  public CompletableFuture<Void> editComment(final long commentId, final String body) {
    final String path = String.format(COMMENTS_URI_ID_TEMPLATE, owner, repo, commentId);
    return github
        .patch(path, github.json().toJsonUnchecked(ImmutableMap.of("body", body)))
        .thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * Edits a specific comment.
   *
   * @deprecated Use {@link #editComment(long, String)} instead
   * @param commentId the comment id
   * @param body the new comment content
   * @return a CompletableFuture representing the completion of the operation
   */
  @Deprecated
  public CompletableFuture<Void> editComment(final int commentId, final String body) {
    return editComment((long) commentId, body);
  }

  /**
   * Deletes a comment.
   *
   * @param commentId the comment id
   * @return a CompletableFuture representing the completion of the operation
   */
  public CompletableFuture<Void> deleteComment(final long commentId) {
    return github
        .delete(String.format(COMMENTS_URI_ID_TEMPLATE, owner, repo, commentId))
        .thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * Deletes a comment.
   *
   * @deprecated Use {@link #deleteComment(long)} instead
   * @param commentId the comment id
   * @return a CompletableFuture representing the completion of the operation
   */
  @Deprecated
  public CompletableFuture<Void> deleteComment(final int commentId) {
    return deleteComment((long) commentId);
  }

  /**
   * Lists comments for a given path.
   *
   * @param path the API endpoint path
   * @return an iterator of asynchronous pages of comments
   */
  private Iterator<AsyncPage<Comment>> listComments(final String path) {
    return new GithubPageIterator<>(new GithubPage<>(github, path, LIST_COMMENT_TYPE_REFERENCE));
  }

  /**
   * Gets an issue by id.
   *
   * @param issueId the issue id
   * @return a CompletableFuture containing the issue
   */
  public CompletableFuture<Issue> getIssue(final long issueId) {
    return github.request(String.format(ISSUES_URI_ID_TEMPLATE, owner, repo, issueId), Issue.class);
  }

  /**
   * Gets an issue by id.
   *
   * @deprecated Use {@link #getIssue(long)} instead
   * @param issueId the issue id
   * @return a CompletableFuture containing the issue
   */
  @Deprecated
  public CompletableFuture<Issue> getIssue(final int issueId) {
    return getIssue((long) issueId);
  }

  /**
   * Creates a reaction on a comment.
   *
   * @param commentId the comment id
   * @param reaction the reaction content
   * @return a CompletableFuture containing the created reaction
   */
  public CompletableFuture<CommentReaction> createCommentReaction(
      final long commentId, final CommentReactionContent reaction) {
    final String path = String.format(COMMENTS_REACTION_TEMPLATE, owner, repo, commentId);
    final String requestBody =
        github.json().toJsonUnchecked(ImmutableMap.of("content", reaction.toString()));
    return github.post(path, requestBody, CommentReaction.class);
  }

  /**
   * Deletes a reaction on a comment. See <a
   * href="https://docs.github.com/en/rest/reactions/reactions?apiVersion=2022-11-28#delete-an-issue-comment-reaction">List
   * reactions for an issue comment</a>
   *
   * @param commentId the comment id
   * @param reactionId the reaction id
   * @return a CompletableFuture containing the HTTP response
   */
  public CompletableFuture<HttpResponse> deleteCommentReaction(
      final long commentId, final long reactionId) {
    final String path =
        String.format(COMMENTS_REACTION_ID_TEMPLATE, owner, repo, commentId, reactionId);
    return github.delete(path);
  }

  /**
   * Lists reactions on a comment. See <a
   * href="https://docs.github.com/en/rest/reactions/reactions?apiVersion=2022-11-28#list-reactions-for-an-issue-comment">List
   * reactions for an issue comment</a>
   *
   * @param commentId the comment id
   * @return an iterator of asynchronous pages of comment reactions
   */
  public GithubPageIterator<CommentReaction> listCommentReaction(final long commentId) {
    final String path = String.format(COMMENTS_REACTION_TEMPLATE, owner, repo, commentId);
    return new GithubPageIterator<>(
        new GithubPage<>(github, path, LIST_COMMENT_REACTION_TYPE_REFERENCE));
  }

  /**
   * Creates a reaction on an issue.
   *
   * @param issueNumber the issue number
   * @param reaction the reaction content
   * @return a CompletableFuture containing the created reaction
   */
  public CompletableFuture<CommentReaction> createIssueReaction(
      final long issueNumber, final CommentReactionContent reaction) {
    final String path = String.format(ISSUES_REACTION_TEMPLATE, owner, repo, issueNumber);
    final String requestBody =
        github.json().toJsonUnchecked(ImmutableMap.of("content", reaction.toString()));
    return github.post(path, requestBody, CommentReaction.class);
  }

  /**
   * Deletes a reaction on an issue. See <a
   * href="https://docs.github.com/en/rest/reactions/reactions?apiVersion=2022-11-28#delete-an-issue-reaction">Delete
   * an issue reaction</a>
   *
   * @param issueNumber the issue number
   * @param reactionId the reaction id
   * @return a CompletableFuture containing the HTTP response
   */
  public CompletableFuture<HttpResponse> deleteIssueReaction(
      final long issueNumber, final long reactionId) {
    final String path =
        String.format(ISSUES_REACTION_ID_TEMPLATE, owner, repo, issueNumber, reactionId);
    return github.delete(path);
  }
}
