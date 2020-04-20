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

import com.google.common.collect.ImmutableMap;
import com.spotify.github.v3.checks.CheckRunRequest;
import com.spotify.github.v3.checks.CheckRunResponse;
import com.spotify.github.v3.checks.CheckRunResponseList;
import com.spotify.github.v3.checks.CheckSuite;
import com.spotify.github.v3.checks.CheckSuiteResponseList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.core.HttpHeaders;

/** Checks API client */
public class ChecksClient {

  private static final String GET_CHECK_SUITE_URI = "/repos/%s/%s/check-suites/%s";
  private static final String LIST_CHECK_SUITES_REF_URI = "/repos/%s/%s/commits/%s/check-suites";

  private static final String CHECK_RUNS_URI = "/repos/%s/%s/check-runs";
  private static final String LIST_CHECK_RUNS_URI = "/repos/%s/%s/commits/%s/check-runs";
  private static final String GET_CHECK_RUN_URI = "/repos/%s/%s/check-runs/%s";

  private final GitHubClient github;
  private final String owner;
  private final String repo;

  private final Map<String, String> extraHeaders =
      ImmutableMap.of(HttpHeaders.ACCEPT, "application/vnd.github.antiope-preview+json");

  /**
   * Instantiates a new Checks client.
   *
   * @param github the github
   * @param owner the org
   * @param repo the repo
   */
  ChecksClient(final GitHubClient github, final String owner, final String repo) {
    this.github = github;
    this.owner = owner;
    this.repo = repo;
  }

  /**
   * Create a checkRun.
   *
   * @param checkRun the checkRunRequest payload to be created
   * @return the CheckRunResponse
   */
  public CompletableFuture<CheckRunResponse> createCheckRun(final CheckRunRequest checkRun) {
    final String path = String.format(CHECK_RUNS_URI, owner, repo);
    return github.post(
        path, github.json().toJsonUnchecked(checkRun), CheckRunResponse.class, extraHeaders);
  }

  /**
   * Updates a checkRun.
   *
   * @param id the checkRun id
   * @param checkRun the checkRun payload
   * @return the completable future
   */
  public CompletableFuture<CheckRunResponse> updateCheckRun(
      final int id, final CheckRunRequest checkRun) {
    final String path = String.format(GET_CHECK_RUN_URI, owner, repo, id);
    return github.patch(
        path, github.json().toJsonUnchecked(checkRun), CheckRunResponse.class, extraHeaders);
  }

  /**
   * Gets a checkRun by id.
   *
   * @param id the checkRun id
   * @return a CheckRunResponse
   */
  public CompletableFuture<CheckRunResponse> getCheckRun(final int id) {
    final String path = String.format(GET_CHECK_RUN_URI, owner, repo, id);
    return github.request(path, CheckRunResponse.class, extraHeaders);
  }

  /**
   * List checkRuns for a given ref.
   *
   * @param ref the ref
   * @return a list of CheckRun
   */
  public CompletableFuture<CheckRunResponseList> getCheckRuns(final String ref) {
    final String path = String.format(LIST_CHECK_RUNS_URI, owner, repo, ref);
    return github.request(path, CheckRunResponseList.class, extraHeaders);
  }

  /**
   * Gets a checkRun by id.
   *
   * @param id the checkSuite id
   * @return a CheckSuite
   */
  public CompletableFuture<CheckSuite> getCheckSuite(final String id) {
    final String path = String.format(GET_CHECK_SUITE_URI, owner, repo, id);
    return github.request(path, CheckSuite.class, extraHeaders);
  }

  /**
   * List checkSuites for a given ref.
   *
   * @param sha the sha
   * @return a list of CheckSuite
   */
  public CompletableFuture<CheckSuiteResponseList> getCheckSuites(final String sha) {
    final String path = String.format(LIST_CHECK_SUITES_REF_URI, owner, repo, sha);
    return github.request(path, CheckSuiteResponseList.class, extraHeaders);
  }
}
