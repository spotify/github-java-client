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

package com.spotify.github.v3.actions.workflowruns;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.User;
import com.spotify.github.v3.prs.PullRequestItem;
import com.spotify.github.v3.repos.PushCommit;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.List;

@Value.Immutable
@GithubStyle
@JsonDeserialize(as = ImmutableWorkflowRunResponse.class)
public interface WorkflowRunResponse {

  /**
   * The ID of the Workflow Run.
   * (Required)
   */
  long id();

  /**
   * The name of the Workflow Run.
   * Not required as per schema.
   */
  @Nullable
  String name();

  /**
   * GitHub api node id. See <a href="https://docs.github.com/en/graphql/guides/using-global-node-ids">Using Global Node ids</a>
   * (Required)
   */
  String nodeId();

  /**
   * The ID of the associated check suite.
   * Not required as per schema.
   */
  @Nullable
  Long checkSuiteId();

  /**
   * The node ID of the associated check suite.
   * Not required as per schema.
   */
  @Nullable
  String checkSuiteNodeId();

  /**
   * The branch of the head commit that points to the version of the workflow being run.
   * (Required)
   */
  String headBranch();

  /**
   * The SHA of the head commit that points to the version of the workflow being run.
   * (Required)
   */
  String headSha();

  /**
   * The full path of the workflow
   * (Required)
   */
  String path();

  /**
   * The auto incrementing run number for the Workflow Run.
   * (Required)
   */
  Integer runNumber();

  /**
   * Attempt number of the run, 1 for first attempt and higher if the workflow was re-run.
   * Not required as per schema.
   */
  @Nullable
  Integer runAttempt();

  /**
   * The event that lead to the trigger of this Workflow Run
   * (Required)
   */
  String event();

  /**
   * The status of this Workflow Run.
   * (Required)
   */
  WorkflowRunStatus status();

  /**
   * The result of the run.
   */
  @Nullable
  String conclusion();

  /**
   * The ID of the parent workflow.
   * (Required)
   */
  Integer workflowId();

  /**
   * The URL to the Workflow Run.
   * (Required)
   */
  String url();

  /**
   * URL for viewing the Workflow run on a browser
   * (Required)
   */
  String htmlUrl();

  /**
   * When the Workflow Run was created
   * (Required)
   */
  ZonedDateTime createdAt();

  /**
   * When the Workflow Run was last updated
   * (Required)
   */
  ZonedDateTime updatedAt();

  /**
   * The start time of the latest run. Resets on re-run.
   * Not required as per schema.
   */
  @Nullable
  ZonedDateTime runStartedAt();

  /**
   * The URL to the jobs for the Workflow Run.
   * (Required)
   */
  String jobsUrl();

  /**
   * The URL to download the logs for the Workflow Run.
   * (Required)
   */
  String logsUrl();

  /**
   * The URL to the associated check suite.
   * (Required)
   */
  String checkSuiteUrl();

  /**
   * The URL to the artifacts for the Workflow Run.
   * (Required)
   */
  String artifactsUrl();

  /**
   * The URL to cancel the Workflow Run.
   * (Required)
   */
  String cancelUrl();

  /**
   * The URL to rerun the Workflow Run.
   * (Required)
   */
  String rerunUrl();

  /**
   * The URL to the previous attempted run of this workflow, if one exists.
   * Not required as per schema.
   */
  @Nullable
  String previousAttemptUrl();

  /**
   * The URL to the workflow.
   * (Required)
   */
  String workflowUrl();

  /**
   * The event-specific title associated with the run or the run-name if set, or the value of `run-name` if it is set in the workflow.
   * (Required)
   */
  String displayTitle();

  /**
   * Pull requests that are open with a `head_sha` or `head_branch` that matches the Workflow Run. The returned pull requests do not necessarily indicate pull requests that triggered the run.
   * (Required)
   */
  List<PullRequestItem> pullRequests();

  /**
   * The GitHub user that triggered the initial Workflow Run. If the Workflow Run is a re-run, this value may differ from triggeringActor. Any workflow re-runs will use the privileges of actor, even if the actor initiating the re-run (triggeringActor) has different privileges.
   * Not required as per schema.
   */
  @Nullable
  User actor();

  /**
   * The GitHub user that initiated the Workflow Run. If the Workflow Run is a re-run, this value may differ from actor. Any workflow re-runs will use the privileges of actor, even if the actor initiating the re-run (triggeringActor) has different privileges.
   * Not required as per schema.
   */
  @Nullable
  User triggeringActor();

  /**
   * The head commit that points to the version of code the workflow being run on.
   * <p>
   * (Required)
   */
  PushCommit headCommit();
}

