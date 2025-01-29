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

public class ActionsClient {
  private final String owner;
  private final String repo;
  private final GitHubClient github;

  ActionsClient(final GitHubClient github, final String owner, final String repo) {
    this.github = github;
    this.owner = owner;
    this.repo = repo;
  }

  static ActionsClient create(final GitHubClient github, final String owner, final String repo) {
    return new ActionsClient(github, owner, repo);
  }

  /**
   * Workflows API client
   *
   * @return Workflows API client
   */
  public WorkflowsClient createWorkflowsClient() {
    return WorkflowsClient.create(github, owner, repo);
  }
}
