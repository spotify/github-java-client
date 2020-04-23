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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.spotify.github.v3.prs.MergeParameters;
import com.spotify.github.v3.prs.PullRequest;
import com.spotify.github.v3.prs.PullRequestItem;
import com.spotify.github.v3.prs.requests.PullRequestCreate;
import com.spotify.github.v3.prs.requests.PullRequestParameters;
import com.spotify.github.v3.prs.requests.PullRequestUpdate;
import com.spotify.github.v3.repos.CommitItem;
import java.lang.invoke.MethodHandles;
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
   * Merges this pull request.
   *
   * @param number pull request number
   * @param sha the sha that should match this PR for the merge to happen
   */
  public CompletableFuture<Void> merge(final int number, final String sha) {
    final String path = String.format(PR_NUMBER_TEMPLATE + "/merge", owner, repo, number);
    log.debug("Merging pr, running: {}", path);
    return github
        .put(path, github.json().toJsonUnchecked(ImmutableMap.of("sha", sha)))
        .thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * Merges a pull request with a specific commit message.
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
