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
import com.spotify.github.v3.workflows.WorkflowsRepositoryResponseList;
import com.spotify.github.v3.workflows.WorkflowsResponse;

import javax.ws.rs.core.HttpHeaders;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Workflows API client */
public class WorkflowsClient {
  private static final String LIST_REPOSITORY_WORKFLOWS_URI = "/repos/%s/%s/actions/workflows";
  private static final String GET_WORKFLOW_URI = "/repos/%s/%s/actions/workflows/%s";

  private final GitHubClient github;
  private final String owner;
  private final String repo;

  private final Map<String, String> extraHeaders =
      ImmutableMap.of(HttpHeaders.ACCEPT, "application/vnd.github+json");

  public WorkflowsClient(final GitHubClient github, final String owner, final String repo) {
    this.github = github;
    this.owner = owner;
    this.repo = repo;
  }

  static WorkflowsClient create(final GitHubClient github, final String owner, final String repo) {
    return new WorkflowsClient(github, owner, repo);
  }

  /**
   * List workflows for a repository.
   *
   * @return a list of workflows for the repository
   */
  public CompletableFuture<WorkflowsRepositoryResponseList> listWorkflows() {
    final String path = String.format(LIST_REPOSITORY_WORKFLOWS_URI, owner, repo);
    return github.request(path, WorkflowsRepositoryResponseList.class, extraHeaders);
  }

  /**
   * Gets a workflow by id.
   *
   * @param id the workflow id
   * @return a WorkflowsResponse
   */
  public CompletableFuture<WorkflowsResponse> getWorkflow(final int id) {
    final String path = String.format(GET_WORKFLOW_URI, owner, repo, id);
    return github.request(path, WorkflowsResponse.class, extraHeaders);
  }
}
