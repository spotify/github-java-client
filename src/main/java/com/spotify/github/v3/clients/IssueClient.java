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
import com.spotify.github.v3.comment.Comment;
import com.spotify.github.v3.comment.CommentReaction;
import com.spotify.github.v3.comment.CommentReactionContent;
import com.spotify.github.v3.issues.Issue;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Issue API client */
public class IssueClient {

  static final String COMMENTS_URI_NUMBER_TEMPLATE = "/repos/%s/%s/issues/%s/comments";
  static final String COMMENTS_URI_TEMPLATE = "/repos/%s/%s/issues/comments";
  static final String COMMENTS_URI_ID_TEMPLATE = "/repos/%s/%s/issues/comments/%s";
  static final String COMMENTS_REACTION_TEMPLATE = "/repos/%s/%s/issues/comments/%s/reactions";
  static final String COMMENTS_REACTION_ID_TEMPLATE = "/repos/%s/%s/issues/%s/reactions/%s";
  static final String ISSUES_URI_ID_TEMPLATE = "/repos/%s/%s/issues/%s";
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final GitHubClient github;
  private final String owner;
  private final String repo;

  IssueClient(final GitHubClient github, final String owner, final String repo) {
    this.github = github;
    this.owner = owner;
    this.repo = repo;
  }

  static IssueClient create(final GitHubClient github, final String owner, final String repo) {
    return new IssueClient(github, owner, repo);
  }

  /**
   * List repository comments.
   *
   * @return comments
   */
  public Iterator<AsyncPage<Comment>> listComments() {
    return listComments(String.format(COMMENTS_URI_TEMPLATE, owner, repo));
  }

  /**
   * List given issue number comments.
   *
   * @param number issue number
   * @return comments
   */
  public Iterator<AsyncPage<Comment>> listComments(final int number) {
    return listComments(String.format(COMMENTS_URI_NUMBER_TEMPLATE, owner, repo, number));
  }

  /**
   * Get a specific comment.
   *
   * @param id comment id
   * @return a comment
   */
  public CompletableFuture<Comment> getComment(final int id) {
    final String path = String.format(COMMENTS_URI_ID_TEMPLATE, owner, repo, id);
    log.info("Fetching issue comments from " + path);
    return github.request(path, Comment.class);
  }

  /**
   * Create a comment for a given issue number.
   *
   * @param number issue number
   * @param body comment content
   * @return the Comment that was just created
   */
  public CompletableFuture<Comment> createComment(final int number, final String body) {
    final String path = String.format(COMMENTS_URI_NUMBER_TEMPLATE, owner, repo, number);
    final String requestBody = github.json().toJsonUnchecked(ImmutableMap.of("body", body));
    return github.post(path, requestBody, Comment.class);
  }

  /**
   * Edit a specific comment.
   *
   * @param id comment id
   * @param body new comment content
   */
  public CompletableFuture<Void> editComment(final int id, final String body) {
    final String path = String.format(COMMENTS_URI_ID_TEMPLATE, owner, repo, id);
    return github
        .patch(path, github.json().toJsonUnchecked(ImmutableMap.of("body", body)))
        .thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * Delete a comment.
   *
   * @param id comment id
   */
  public CompletableFuture<Void> deleteComment(final int id) {
    return github
        .delete(String.format(COMMENTS_URI_ID_TEMPLATE, owner, repo, id))
        .thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  private Iterator<AsyncPage<Comment>> listComments(final String path) {
    return new GithubPageIterator<>(new GithubPage<>(github, path, LIST_COMMENT_TYPE_REFERENCE));
  }

  /**
   * Get issue by id
   *
   * @param id issue id
   * @return the Issue for the given id if exists.
   */
  public CompletableFuture<Issue> getIssue(final int id) {
    return github.request(String.format(ISSUES_URI_ID_TEMPLATE, owner, repo, id), Issue.class);
  }

  /**
   * Create a reaction on a comment. See <a *
   * href="https://docs.github.com/en/rest/reactions/reactions?apiVersion=2022-11-28#create-reaction-for-an-issue-comment">Create
   * reaction for an issue comment</a>
   *
   * @param commentId comment id
   * @param reaction reaction content
   * @return the Comment that was just created
   */
  public CompletableFuture<CommentReaction> createCommentReaction(
      final long commentId, final CommentReactionContent reaction) {
    final String path = String.format(COMMENTS_REACTION_TEMPLATE, owner, repo, commentId);
    final String requestBody =
        github.json().toJsonUnchecked(ImmutableMap.of("content", reaction.toString()));
    return github.post(path, requestBody, CommentReaction.class);
  }

  /**
   * Delete a reaction on a comment. See <a
   * href="https://docs.github.com/en/rest/reactions/reactions?apiVersion=2022-11-28#delete-an-issue-comment-reaction">List
   * reactions for an issue comment</a>
   *
   * @param issueNumber issue number
   * @param reactionId reaction id
   */
  public CompletableFuture<Response> deleteCommentReaction(
      final long issueNumber, final long reactionId) {
    final String path =
        String.format(COMMENTS_REACTION_ID_TEMPLATE, owner, repo, issueNumber, reactionId);
    return github.delete(path);
  }

  /**
   * List reactions on a comment. See <a
   * href="https://docs.github.com/en/rest/reactions/reactions?apiVersion=2022-11-28#list-reactions-for-an-issue-comment">List
   * reactions for an issue comment</a>
   *
   * @param commentId comment id
   * @return reactions
   */
  public GithubPageIterator<CommentReaction> listCommentReaction(final long commentId) {
    final String path = String.format(COMMENTS_REACTION_TEMPLATE, owner, repo, commentId);
    return new GithubPageIterator<>(
        new GithubPage<>(github, path, LIST_COMMENT_REACTION_TYPE_REFERENCE));
  }
}
