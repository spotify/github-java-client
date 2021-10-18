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
import static com.spotify.github.v3.clients.GitHubClient.LIST_PR_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_REVIEW_REQUEST_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_REVIEW_TYPE_REFERENCE;

import com.google.common.base.Strings;
import com.spotify.github.async.AsyncPage;
import com.spotify.github.v3.prs.*;
import com.spotify.github.v3.prs.requests.PullRequestCreate;
import com.spotify.github.v3.prs.requests.PullRequestParameters;
import com.spotify.github.v3.prs.requests.PullRequestUpdate;
import com.spotify.github.v3.repos.CommitItem;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Pull call API client */
public class PullRequestClient {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String PR_TEMPLATE = "/repos/%s/%s/pulls";
  private static final String PR_NUMBER_TEMPLATE = "/repos/%s/%s/pulls/%s";
  private static final String PR_COMMITS_TEMPLATE = "/repos/%s/%s/pulls/%s/commits";
  private static final String PR_REVIEWS_TEMPLATE = "/repos/%s/%s/pulls/%s/reviews";
  private static final String PR_REVIEW_REQUESTS_TEMPLATE = "/repos/%s/%s/pulls/%s/requested_reviewers";

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
   * @param number pull request number
   * @return pull request
   */
  public CompletableFuture<PullRequest> get(final int number) {
    final String path = String.format(PR_NUMBER_TEMPLATE, owner, repo, number);
    log.debug("Fetching pull request from " + path);
    return github.request(path, PullRequest.class);
  }

  /**
   * Create a pull request.
   *
   * @param request create request
   */
  public CompletableFuture<Void> create(final PullRequestCreate request) {
    final String path = String.format(PR_TEMPLATE, owner, repo);
    return github
        .post(path, github.json().toJsonUnchecked(request))
        .thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * Update given pull request.
   *
   * @param number pull request number
   * @param request update request
   */
  public CompletableFuture<Void> update(final int number, final PullRequestUpdate request) {
    final String path = String.format(PR_NUMBER_TEMPLATE, owner, repo, number);
    return github
        .patch(path, github.json().toJsonUnchecked(request))
        .thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * List pull request commits.
   *
   * @param number pull request number
   * @return commits
   */
  public CompletableFuture<List<CommitItem>> listCommits(final int number) {
    final String path = String.format(PR_COMMITS_TEMPLATE, owner, repo, number);
    log.debug("Fetching pull request commits from " + path);
    return github.request(path, LIST_COMMIT_TYPE_REFERENCE);
  }

  /**
   * List pull request reviews. Reviews are returned in chronological order.
   *
   * @param number pull request number
   * @return list of reviews
   */
   public CompletableFuture<List<Review>> listReviews(final int number) {
   final String path = String.format(PR_REVIEWS_TEMPLATE, owner, repo, number);
   log.debug("Fetching pull request reviews from " + path);
   return github.request(path, LIST_REVIEW_TYPE_REFERENCE);
  }

  /**
   * List pull request reviews paginated. Reviews are returned in chronological order.
   *
   * @param number pull request number
   * @param itemsPerPage number of items per page
   * @return iterator of reviews
   */
  public Iterator<AsyncPage<Review>> listReviews(final int number, final int itemsPerPage) {
    // FIXME Use itemsPerPage property
    final String path = String.format(PR_REVIEWS_TEMPLATE, owner, repo, number);
    log.debug("Fetching pull request reviews from " + path);
    return new GithubPageIterator<>(new GithubPage<>(github, path, LIST_REVIEW_TYPE_REFERENCE));
  }

  /**
   * Creates a review for a pull request.
   *
   * @param number pull request number
   * @param properties properties for reviewing the PR, such as sha, body and event
   * @see "https://developer.github.com/v3/pulls/reviews/#create-a-review-for-a-pull-request"
   */
  public CompletableFuture<Review> createReview(final int number, final ReviewParameters properties) {
    final String path = String.format(PR_REVIEWS_TEMPLATE, owner, repo, number);
    final String jsonPayload = github.json().toJsonUnchecked(properties);
    log.debug("Creating review for PR: " + path);
    return github.post(path, jsonPayload, Review.class);
  }

  /**
   * List pull request requested reviews.
   *
   * @param number pull request number
   * @return list of reviews
   */
  public CompletableFuture<ReviewRequests> listReviewRequests(final int number) {
    final String path = String.format(PR_REVIEW_REQUESTS_TEMPLATE, owner, repo, number);
    log.debug("Fetching pull request requested reviews from " + path);
    return github.request(path, LIST_REVIEW_REQUEST_TYPE_REFERENCE);
  }

  /**
   * Requests a review for a pull request.
   *
   * @param number pull request number
   * @param properties properties for reviewing the PR, such as reviewers and team_reviewers.
   * @see "https://docs.github.com/en/rest/reference/pulls#request-reviewers-for-a-pull-request"
   */
  public CompletableFuture<PullRequest> requestReview(final int number, final RequestReviewParameters properties) {
    final String path = String.format(PR_REVIEW_REQUESTS_TEMPLATE, owner, repo, number);
    final String jsonPayload = github.json().toJsonUnchecked(properties);
    log.debug("Requesting reviews for PR: " + path);
    return github.post(path, jsonPayload, PullRequest.class);
  }

  /**
   * Remove a request for review for a pull request.
   *
   * @param number pull request number
   * @param properties properties for reviewing the PR, such as reviewers and team_reviewers.
   * @see "https://docs.github.com/en/rest/reference/pulls#request-reviewers-for-a-pull-request"
   */
  public CompletableFuture<Void> removeRequestedReview(final int number, final RequestReviewParameters properties) {
    final String path = String.format(PR_REVIEW_REQUESTS_TEMPLATE, owner, repo, number);
    final String jsonPayload = github.json().toJsonUnchecked(properties);
    log.debug("Removing requested reviews for PR: " + path);
    return github.delete(path, jsonPayload).thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * Merges a pull request.
   *
   * @param number pull request number
   * @param properties the properties on merging the PR, such as title, message and sha
   * @see "https://developer.github.com/v3/pulls/#merge-a-pull-request-merge-button"
   */
  public CompletableFuture<Void> merge(final int number, final MergeParameters properties) {
    final String path = String.format(PR_NUMBER_TEMPLATE + "/merge", owner, repo, number);
    final String jsonPayload = github.json().toJsonUnchecked(properties);
    log.debug("Merging pr, running: {}", path);
    return github.put(path, jsonPayload).thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  private CompletableFuture<List<PullRequestItem>> list(final String parameterPath) {
    final String path = String.format(PR_TEMPLATE + parameterPath, owner, repo);
    log.debug("Fetching pull requests from " + path);
    return github.request(path, LIST_PR_TYPE_REFERENCE);
  }

}
