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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.spotify.github.v3.actions.workflowruns.GetWorkflowRunQueryParams;
import com.spotify.github.v3.actions.workflowruns.ListWorkflowRunsQueryParams;
import com.spotify.github.v3.actions.workflowruns.WorkflowRunResponse;
import com.spotify.github.v3.actions.workflowruns.WorkflowRunsResponseList;

import javax.annotation.Nullable;
import javax.ws.rs.core.HttpHeaders;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Workflow Runs API client
 */
public class WorkflowRunsClient {
  private static final String LIST_REPOSITORY_WORKFLOW_RUNS_URI = "/repos/%s/%s/actions/runs";
  private static final String LIST_WORKFLOW_RUNS_URI = "/repos/%s/%s/actions/workflows/%s/runs";
  private static final String GET_WORKFLOW_RUN_URI = "/repos/%s/%s/actions/runs/%s";

  private final GitHubClient github;
  private final String owner;
  private final String repo;

  private final Map<String, String> extraHeaders =
      ImmutableMap.of(HttpHeaders.ACCEPT, "application/vnd.github+json");

  public WorkflowRunsClient(final GitHubClient github, final String owner, final String repo) {
    this.github = github;
    this.owner = owner;
    this.repo = repo;
  }

  static WorkflowRunsClient create(final GitHubClient github, final String owner, final String repo) {
    return new WorkflowRunsClient(github, owner, repo);
  }

  /**
   * List all workflow runs for a repository.
   *
   * @param queryParams optional parameters to add to the query. Can be null.
   * @return a list of workflow runs for the repository
   */
  public CompletableFuture<WorkflowRunsResponseList> listAllWorkflowRuns(@Nullable final ListWorkflowRunsQueryParams queryParams) {
    final String serial = (queryParams == null ? "" : queryParams.serialize());
    final String path = String.format(LIST_REPOSITORY_WORKFLOW_RUNS_URI, owner, repo) + (Strings.isNullOrEmpty(serial) ? "" : "?" + serial);
    return github.request(path, WorkflowRunsResponseList.class, extraHeaders);
  }

  /**
   * List workflow runs for the given workflow.
   *
   * @param workflowId  the workflow id to get the workflow runs for
   * @param queryParams optional parameters to add to the query. Can be null.
   * @return a list of workflow runs for the given workflow
   */
  public CompletableFuture<WorkflowRunsResponseList> listWorkflowRuns(final int workflowId, @Nullable final ListWorkflowRunsQueryParams queryParams) {
    final String serial = (queryParams == null ? "" : queryParams.serialize());
    final String path = String.format(LIST_WORKFLOW_RUNS_URI, owner, repo, workflowId) + (Strings.isNullOrEmpty(serial) ? "" : "?" + serial);

    return github.request(path, WorkflowRunsResponseList.class, extraHeaders);
  }

  /**
   * Gets a workflow by id.
   *
   * @param runId the workflow run id to be retrieved
   * @return a WorkflowRunResponse
   */
  public CompletableFuture<WorkflowRunResponse> getWorkflowRun(final long runId, @Nullable final GetWorkflowRunQueryParams queryParams) {
    final String serial = (queryParams == null ? "" : queryParams.serialize());
    final String path = String.format(GET_WORKFLOW_RUN_URI, owner, repo, runId) + (Strings.isNullOrEmpty(serial) ? "" : "?" + serial);
    return github.request(path, WorkflowRunResponse.class, extraHeaders);
  }
}
