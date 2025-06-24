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

import static com.spotify.github.v3.clients.GitHubClient.IGNORE_RESPONSE_CONSUMER;
import static com.spotify.github.v3.clients.GitHubClient.LIST_COMMIT_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_FILE_ITEMS;
import static com.spotify.github.v3.clients.GitHubClient.LIST_PR_COMMENT_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_PR_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_REVIEW_REQUEST_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_REVIEW_TYPE_REFERENCE;
import static java.util.Objects.isNull;
import static java.lang.Math.toIntExact;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.spotify.github.async.AsyncPage;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.git.FileItem;
import com.spotify.github.v3.prs.Comment;
import com.spotify.github.v3.prs.MergeParameters;
import com.spotify.github.v3.prs.PullRequest;
import com.spotify.github.v3.prs.PullRequestItem;
import com.spotify.github.v3.prs.RequestReviewParameters;
import com.spotify.github.v3.prs.Review;
import com.spotify.github.v3.prs.ReviewParameters;
import com.spotify.github.v3.prs.ReviewRequests;
import com.spotify.github.v3.prs.requests.PullRequestCreate;
import com.spotify.github.v3.prs.requests.PullRequestParameters;
import com.spotify.github.v3.prs.requests.PullRequestUpdate;
import com.spotify.github.v3.repos.CommitItem;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.core.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Pull call API client */
public class PullRequestClient {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String PR_TEMPLATE = "/repos/%s/%s/pulls";
  private static final String PR_NUMBER_TEMPLATE = "/repos/%s/%s/pulls/%s";
  private static final String PR_COMMITS_TEMPLATE = "/repos/%s/%s/pulls/%s/commits";
  private static final String PR_REVIEWS_TEMPLATE = "/repos/%s/%s/pulls/%s/reviews";
  private static final String PR_COMMENTS_TEMPLATE = "/repos/%s/%s/pulls/%s/comments";
  private static final String PR_CHANGED_FILES_TEMPLATE = "/repos/%s/%s/pulls/%s/files";
  private static final String PR_REVIEW_REQUESTS_TEMPLATE =
      "/repos/%s/%s/pulls/%s/requested_reviewers";
  private static final String PR_COMMENT_REPLIES_TEMPLATE =
      "/repos/%s/%s/pulls/%s/comments/%s/replies";

  private final GitHubClient github;
  private final String owner;
  private final String repo;

  PullRequestClient(final GitHubClient github, final String owner, final String repo) {
    this.github = github;
    this.owner = owner;
    this.repo = repo;
  }

  static PullRequestClient create(
      final GitHubClient github, final String owner, final String repo) {
    return new PullRequestClient(github, owner, repo);
  }

  /**
   * List repository pull request.
   *
   * @return pull requests
   */
  public CompletableFuture<List<PullRequestItem>> list() {
    return list("");
  }

  /**
   * List repository pull requests using given parameters.
   *
   * @param parameters request parameters
   * @return pull requests
   */
  public CompletableFuture<List<PullRequestItem>> list(final PullRequestParameters parameters) {
    final String serial = parameters.serialize();
    final String path = Strings.isNullOrEmpty(serial) ? "" : "?" + serial;
    return list(path);
  }

  /**
   * Get a specific pull request.
   *
   * @deprecated Use {@link #get(long)} instead
   * @param prNumber pull request number
   * @return pull request
   */
  @Deprecated
  public CompletableFuture<PullRequest> get(final int prNumber) {
    return get((long) prNumber);
  }

  /**
   * Get a specific pull request.
   *
   * @param prNumber pull request number
   * @return pull request
   */
  public CompletableFuture<PullRequest> get(final long prNumber) {
    final String path = String.format(PR_NUMBER_TEMPLATE, owner, repo, prNumber);
    log.debug("Fetching pull request from " + path);
    return github.request(path, PullRequest.class);
  }

  /**
   * Create a pull request.
   *
   * @param request create request
   * @return pull request
   */
  public CompletableFuture<PullRequest> create(final PullRequestCreate request) {
    final String path = String.format(PR_TEMPLATE, owner, repo);
    return github.post(path, github.json().toJsonUnchecked(request), PullRequest.class);
  }

  /**
   * Update given pull request.
   *
   * @deprecated Use {@link #update(long, PullRequestUpdate)} instead
   * @param prNumber pull request number
   * @param request update request
   * @return pull request
   */
  @Deprecated
  public CompletableFuture<PullRequest> update(
      final int prNumber, final PullRequestUpdate request) {
    return update((long) prNumber, request);
  }

  /**
   * Update given pull request.
   *
   * @param prNumber pull request number
   * @param request update request
   * @return pull request
   */
  public CompletableFuture<PullRequest> update(
      final long prNumber, final PullRequestUpdate request) {
    final String path = String.format(PR_NUMBER_TEMPLATE, owner, repo, prNumber);
    return github.patch(path, github.json().toJsonUnchecked(request), PullRequest.class);
  }

  /**
   * List pull request commits.
   *
   * @deprecated Use {@link #listCommits(long)} instead
   * @param prNumber pull request number
   * @return commits
   */
  @Deprecated
  public CompletableFuture<List<CommitItem>> listCommits(final int prNumber) {
    return listCommits((long) prNumber);
  }

  /**
   * List pull request commits.
   *
   * @param prNumber pull request number
   * @return commits
   */
  public CompletableFuture<List<CommitItem>> listCommits(final long prNumber) {
    final String path = String.format(PR_COMMITS_TEMPLATE, owner, repo, prNumber);
    log.debug("Fetching pull request commits from " + path);
    return github.request(path).thenApply(
        response -> Json.create().fromJsonUncheckedNotNull(response.bodyString(), LIST_COMMIT_TYPE_REFERENCE));
  }

  public Iterator<AsyncPage<CommitItem>> listCommits(final long prNumber, final int itemsPerPage) {
    final String path = "";

    return new GithubPageIterator<>(new GithubPage<>(github, path, LIST_COMMIT_TYPE_REFERENCE, itemsPerPage));
  }

  /**
   * List pull request reviews. Reviews are returned in chronological order.
   *
   * @deprecated Use {@link #listReviews(long)} instead
   * @param prNumber pull request number
   * @return list of reviews
   */
  @Deprecated
  public CompletableFuture<List<Review>> listReviews(final int prNumber) {
    return listReviews((long) prNumber);
  }

  /**
   * List pull request reviews. Reviews are returned in chronological order.
   *
   * @param prNumber pull request number
   * @return list of reviews
   */
  public CompletableFuture<List<Review>> listReviews(final long prNumber) {
    final String path = String.format(PR_REVIEWS_TEMPLATE, owner, repo, prNumber);
    log.debug("Fetching pull request reviews from " + path);
    return github.request(path, LIST_REVIEW_TYPE_REFERENCE);
  }

  /**
   * List pull request reviews paginated. Reviews are returned in chronological order.
   *
   * @deprecated Use {@link #listReviews(long,long)} instead
   * @param prNumber pull request number
   * @param itemsPerPage prNumber of items per page
   * @return iterator of reviews
   */
  @Deprecated
  public Iterator<AsyncPage<Review>> listReviews(final int prNumber, final int itemsPerPage) {
    return listReviews((long) prNumber, itemsPerPage);
  }

  /**
   * List pull request reviews paginated. Reviews are returned in chronological order.
   *
   * @param prNumber pull request number
   * @param itemsPerPage prNumber of items per page
   * @return iterator of reviews
   */
  public Iterator<AsyncPage<Review>> listReviews(final long prNumber, final long itemsPerPage) {
    final String path = String.format(PR_REVIEWS_TEMPLATE, owner, repo, prNumber);
    log.debug("Fetching pull request reviews from " + path);
    return new GithubPageIterator<>(new GithubPage<>(github, path, LIST_REVIEW_TYPE_REFERENCE, toIntExact(itemsPerPage)));
  }

  /**
   * Creates a review for a pull request.
   *
   * @deprecated Use {@link #createReview(long,ReviewParameters)} instead
   * @param prNumber pull request number
   * @param properties properties for reviewing the PR, such as sha, body and event
   * @see "https://developer.github.com/v3/pulls/reviews/#create-a-review-for-a-pull-request"
   */
  @Deprecated
  public CompletableFuture<Review> createReview(
      final int prNumber, final ReviewParameters properties) {
    return createReview((long) prNumber, properties);
  }

  /**
   * Creates a review for a pull request.
   *
   * @param prNumber pull request number
   * @param properties properties for reviewing the PR, such as sha, body and event
   * @see "https://developer.github.com/v3/pulls/reviews/#create-a-review-for-a-pull-request"
   */
  public CompletableFuture<Review> createReview(
      final long prNumber, final ReviewParameters properties) {
    final String path = String.format(PR_REVIEWS_TEMPLATE, owner, repo, prNumber);
    final String jsonPayload = github.json().toJsonUnchecked(properties);
    log.debug("Creating review for PR: " + path);
    return github.post(path, jsonPayload, Review.class);
  }

  /**
   * List pull request requested reviews.
   *
   * @deprecated Use {@link #listReviewRequests(long)} instead
   * @param prNumber pull request number
   * @return list of reviews
   */
  @Deprecated
  public CompletableFuture<ReviewRequests> listReviewRequests(final int prNumber) {
    return listReviewRequests((long) prNumber);
  }

  /**
   * List pull request requested reviews.
   *
   * @param prNumber pull request number
   * @return list of reviews
   */
  public CompletableFuture<ReviewRequests> listReviewRequests(final long prNumber) {
    final String path = String.format(PR_REVIEW_REQUESTS_TEMPLATE, owner, repo, prNumber);
    log.debug("Fetching pull request requested reviews from " + path);
    return github.request(path, LIST_REVIEW_REQUEST_TYPE_REFERENCE);
  }

  /**
   * Requests a review for a pull request.
   *
   * @deprecated Use {@link #requestReview(long,RequestReviewParameters)} instead
   * @param prNumber pull request number
   * @param properties properties for reviewing the PR, such as reviewers and team_reviewers.
   * @see "https://docs.github.com/en/rest/reference/pulls#request-reviewers-for-a-pull-request"
   */
  @Deprecated
  public CompletableFuture<PullRequest> requestReview(
      final int prNumber, final RequestReviewParameters properties) {
    return requestReview((long) prNumber, properties);
  }

  /**
   * Requests a review for a pull request.
   *
   * @param prNumber pull request number
   * @param properties properties for reviewing the PR, such as reviewers and team_reviewers.
   * @see "https://docs.github.com/en/rest/reference/pulls#request-reviewers-for-a-pull-request"
   */
  public CompletableFuture<PullRequest> requestReview(
      final long prNumber, final RequestReviewParameters properties) {
    final String path = String.format(PR_REVIEW_REQUESTS_TEMPLATE, owner, repo, prNumber);
    final String jsonPayload = github.json().toJsonUnchecked(properties);
    log.debug("Requesting reviews for PR: " + path);
    return github.post(path, jsonPayload, PullRequest.class);
  }

  /**
   * Remove a request for review for a pull request.
   *
   * @deprecated Use {@link #removeRequestedReview(long,RequestReviewParameters)} instead
   * @param prNumber pull request number
   * @param properties properties for reviewing the PR, such as reviewers and team_reviewers.
   * @see "https://docs.github.com/en/rest/reference/pulls#request-reviewers-for-a-pull-request"
   */
  @Deprecated
  public CompletableFuture<Void> removeRequestedReview(
      final int prNumber, final RequestReviewParameters properties) {
    return removeRequestedReview((long) prNumber, properties);
  }

  /**
   * Remove a request for review for a pull request.
   *
   * @param prNumber pull request number
   * @param properties properties for reviewing the PR, such as reviewers and team_reviewers.
   * @see "https://docs.github.com/en/rest/reference/pulls#request-reviewers-for-a-pull-request"
   */
  public CompletableFuture<Void> removeRequestedReview(
      final long prNumber, final RequestReviewParameters properties) {
    final String path = String.format(PR_REVIEW_REQUESTS_TEMPLATE, owner, repo, prNumber);
    final String jsonPayload = github.json().toJsonUnchecked(properties);
    log.debug("Removing requested reviews for PR: " + path);
    return github.delete(path, jsonPayload).thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * Merges a pull request.
   *
   * @deprecated Use {@link #merge(long,MergeParameters)} instead
   * @param prNumber pull request number
   * @param properties the properties on merging the PR, such as title, message and sha
   * @see "https://developer.github.com/v3/pulls/#merge-a-pull-request-merge-button"
   */
  @Deprecated
  public CompletableFuture<Void> merge(final int prNumber, final MergeParameters properties) {
    return merge((long) prNumber, properties);
  }

  /**
   * Merges a pull request.
   *
   * @param prNumber pull request number
   * @param properties the properties on merging the PR, such as title, message and sha
   * @see "https://developer.github.com/v3/pulls/#merge-a-pull-request-merge-button"
   */
  public CompletableFuture<Void> merge(final long prNumber, final MergeParameters properties) {
    final String path = String.format(PR_NUMBER_TEMPLATE + "/merge", owner, repo, prNumber);
    final String jsonPayload = github.json().toJsonUnchecked(properties);
    log.debug("Merging pr, running: {}", path);
    return github.put(path, jsonPayload).thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * Fetches a pull request patch.
   *
   * @deprecated Use {@link #patch(long)} instead
   * @param prNumber pull request number
   * @return reader for the patch
   */
  @Deprecated
  public CompletableFuture<Reader> patch(final int prNumber) {
    return patch((long) prNumber);
  }

  /**
   * Fetches a pull request patch.
   *
   * @param prNumber pull request number
   * @return reader for the patch
   */
  public CompletableFuture<Reader> patch(final long prNumber) {
    final String path = String.format(PR_NUMBER_TEMPLATE, owner, repo, prNumber);
    final Map<String, String> extraHeaders =
        ImmutableMap.of(HttpHeaders.ACCEPT, "application/vnd.github.patch");
    log.debug("Fetching pull request patch from " + path);
    return github
        .request(path, extraHeaders)
        .thenApply(
            response -> {
              final var body = response.body();
              if (isNull(body)) {
                return Reader.nullReader();
              }
              return new InputStreamReader(body);
            });
  }

  /**
   * Fetches a pull request diff.
   *
   * @deprecated Use {@link #diff(long)} instead
   * @param prNumber pull request number
   * @return reader for the diff
   */
  @Deprecated
  public CompletableFuture<Reader> diff(final int prNumber) {
    return diff((long) prNumber);
  }

  /**
   * Fetches a pull request diff.
   *
   * @param prNumber pull request number
   * @return reader for the diff
   */
  public CompletableFuture<Reader> diff(final long prNumber) {
    final String path = String.format(PR_NUMBER_TEMPLATE, owner, repo, prNumber);
    final Map<String, String> extraHeaders =
        ImmutableMap.of(HttpHeaders.ACCEPT, "application/vnd.github.diff");
    log.debug("Fetching pull diff from " + path);
    return github
        .request(path, extraHeaders)
        .thenApply(
            response -> {
              final var body = response.body();
              if (isNull(body)) {
                return Reader.nullReader();
              }
              return new InputStreamReader(body);
            });
  }

  public Iterator<AsyncPage<FileItem>> changedFiles(final long prNumber) {
    final String path = String.format(PR_CHANGED_FILES_TEMPLATE, owner, repo, prNumber);
    return new GithubPageIterator<>(new GithubPage<>(github, path, LIST_FILE_ITEMS));
  }

  /**
   * List pull requests using given parameters.
   *
   * @param parameterPath request parameters
   * @return pull requests
   */
  private CompletableFuture<List<PullRequestItem>> list(final String parameterPath) {
    final String path = String.format(PR_TEMPLATE + parameterPath, owner, repo);
    log.debug("Fetching pull requests from " + path);
    return github.request(path, LIST_PR_TYPE_REFERENCE);
  }

  /**
   * List pull request review comments.
   *
   * @param prNumber pull request number
   * @return iterator of comments
   */
  public Iterator<AsyncPage<Comment>> listComments(final long prNumber) {
    final String path = String.format(PR_COMMENTS_TEMPLATE, owner, repo, prNumber);
    return new GithubPageIterator<>(new GithubPage<>(github, path, LIST_PR_COMMENT_TYPE_REFERENCE));
  }

  /**
   * Creates a reply to a pull request review comment.
   *
   * @param prNumber pull request number
   * @param commentId the ID of the comment to reply to
   * @param body the reply message
   * @return the created comment
   * @see "https://docs.github.com/en/rest/pulls/comments#create-a-reply-for-a-review-comment"
   */
  public CompletableFuture<Comment> createCommentReply(
      final long prNumber, final long commentId, final String body) {
    final String path =
        String.format(PR_COMMENT_REPLIES_TEMPLATE, owner, repo, prNumber, commentId);
    final Map<String, String> payload = ImmutableMap.of("body", body);
    final String jsonPayload = github.json().toJsonUnchecked(payload);
    log.debug("Creating reply to PR comment: " + path);
    return github.post(path, jsonPayload, Comment.class);
  }
}
