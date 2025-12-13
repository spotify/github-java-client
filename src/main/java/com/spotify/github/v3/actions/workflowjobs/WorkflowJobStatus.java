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

/**
 * The possible status values of a WorkflowJob's status field.
 * &gt;p/&lt;
 * Value of the status property can be one of: "queued", "in_progress", or "completed". Only GitHub Actions can set a status of "waiting", "pending", or "requested".
 * When it’s “completed,” it makes sense to check if it finished successfully. We need a value of the conclusion property.
 * Conclusion Can be one of the “success”, “failure”, “neutral”, “cancelled”, “skipped”, “timed_out”, or “action_required”.
 * &gt;p/&lt;
 * &#064;See <a href="https://docs.github.com/en/rest/actions/workflow-runs?apiVersion=2022-11-28#list-workflow-runs-for-a-repository">The GitHub API docs</a>
 * &#064;See also <a href="https://github.com/github/rest-api-description/issues/1634#issuecomment-2230666873">GitHub rest api docs issue #1634</a>
 */
public enum WorkflowJobStatus {
  completed,
  in_progress,
  queued,
  requested,
  waiting,
  pending
}
