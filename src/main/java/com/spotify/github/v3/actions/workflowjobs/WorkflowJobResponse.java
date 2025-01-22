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

package com.spotify.github.v3.actions.workflowjobs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.spotify.github.GithubStyle;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.List;

@Value.Immutable
@GithubStyle
@JsonDeserialize(as = ImmutableWorkflowJobResponse.class)
public interface WorkflowJobResponse {
  /**
   * The id of the job.
   * (Required)
   */
  long id();

  /**
   * The id of the associated workflow run.
   * (Required)
   */
  long runId();

  /**
   * (Required)
   */
  String runUrl();

  /**
   * Attempt number of the associated workflow run, 1 for first attempt and higher if the workflow was re-run.
   */
  @Nullable
  Integer runAttempt();

  /**
   * (Required)
   */
  String nodeId();

  /**
   * The SHA of the commit that is being run.
   * (Required)
   */
  String headSha();

  /**
   * (Required)
   */
  String url();

  /**
   * (Required)
   */
  String htmlUrl();

  /**
   * The phase of the lifecycle that the job is currently in.
   * (Required)
   */
  WorkflowJobStatus status();

  /**
   * The outcome of the job.
   * (Required)
   */
  WorkflowJobConclusion conclusion();

  /**
   * The time that the job created, in ISO 8601 format.
   */
  @Nullable
  ZonedDateTime createdAt();

  /**
   * The time that the job started, in ISO 8601 format.
   * (Required)
   */
  ZonedDateTime startedAt();

  /**
   * The time that the job finished, in ISO 8601 format.
   * (Required)
   */
  ZonedDateTime completedAt();

  /**
   * The name of the job.
   * (Required)
   */
  String name();

  /**
   * Steps in this job.
   */
  @Nullable
  List<WorkflowJobStep> steps();

  /**
   * (Required)
   */
  String checkRunUrl();

  /**
   * Labels for the workflow job. Specified by the "runs_on" attribute in the action's workflow file.
   * (Required)
   */
  List<String> labels();

  /**
   * The ID of the runner to which this job has been assigned. (If a runner hasn't yet been assigned, this will be null.)
   */
  @Nullable
  Integer runnerId();

  /**
   * The name of the runner to which this job has been assigned. (If a runner hasn't yet been assigned, this will be null.)
   * (Required)
   */
  String runnerName();

  /**
   * The ID of the runner group to which this job has been assigned. (If a runner hasn't yet been assigned, this will be null.)
   * (Required)
   */
  int runnerGroupId();

  /**
   * The name of the runner group to which this job has been assigned. (If a runner hasn't yet been assigned, this will be null.)
   * (Required)
   */
  String runnerGroupName();

  /**
   * The name of the workflow.
   * (Required)
   */
  String workflowName();

  /**
   * The name of the current branch.
   * (Required)
   */
  String headBranch();
}

