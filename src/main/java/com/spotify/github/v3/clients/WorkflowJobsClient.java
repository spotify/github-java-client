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
import com.spotify.github.v3.actions.workflowjobs.ListWorkflowJobsQueryParams;
import com.spotify.github.v3.actions.workflowjobs.WorkflowJobResponse;
import com.spotify.github.v3.actions.workflowjobs.WorkflowJobsResponseList;

import javax.annotation.Nullable;
import javax.ws.rs.core.HttpHeaders;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Workflow Runs API client
 */
public class WorkflowJobsClient {
  private static final String LIST_WORKFLOW_RUN_JOBS_URI = "/repos/%s/%s/actions/runs/%s/jobs";
  private static final String GET_WORKFLOW_JOB_URI = "/repos/%s/%s/actions/jobs/%s";

  private final GitHubClient github;
  private final String owner;
  private final String repo;

  private final Map<String, String> extraHeaders =
      ImmutableMap.of(HttpHeaders.ACCEPT, "application/vnd.github+json");

  public WorkflowJobsClient(final GitHubClient github, final String owner, final String repo) {
    this.github = github;
    this.owner = owner;
    this.repo = repo;
  }

  static WorkflowJobsClient create(final GitHubClient github, final String owner, final String repo) {
    return new WorkflowJobsClient(github, owner, repo);
  }

  /**
   * List all workflow run jobs for a repository.
   *
   * @param queryParams optional parameters to add to the query. Can be null.
   * @return a list of workflow run jobs for the repository
   */
  public CompletableFuture<WorkflowJobsResponseList> listWorkflowJobs(final long runId, @Nullable final ListWorkflowJobsQueryParams queryParams) {
    final String serial = (queryParams == null ? "" : queryParams.serialize());
    final String path = String.format(LIST_WORKFLOW_RUN_JOBS_URI, owner, repo, runId) + (Strings.isNullOrEmpty(serial) ? "" : "?" + serial);
    return github.request(path, WorkflowJobsResponseList.class, extraHeaders);
  }

  /**
   * Gets a workflow job by id.
   *
   * @param jobId the workflow job id to be retrieved
   * @return a WorkflowRunResponse
   */
  public CompletableFuture<WorkflowJobResponse> getWorkflowJob(final long jobId, @Nullable final ListWorkflowJobsQueryParams queryParams) {
    final String serial = (queryParams == null ? "" : queryParams.serialize());
    final String path = String.format(GET_WORKFLOW_JOB_URI, owner, repo, jobId) + (Strings.isNullOrEmpty(serial) ? "" : "?" + serial);
    return github.request(path, WorkflowJobResponse.class, extraHeaders);
  }
}
