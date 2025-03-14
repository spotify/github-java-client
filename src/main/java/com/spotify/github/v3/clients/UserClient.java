/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2016 - 2024 Spotify AB
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

import com.spotify.github.v3.user.requests.SuspensionReason;
import java.util.concurrent.CompletableFuture;

public class UserClient {

  public static final int NO_CONTENT = 204;
  private final GitHubClient github;
  private final String owner;

  private static final String SUSPEND_USER_TEMPLATE = "/users/%s/suspended";

  UserClient(final GitHubClient github, final String owner) {
    this.github = github;
    this.owner = owner;
  }

  static UserClient create(final GitHubClient github, final String owner) {
    return new UserClient(github, owner);
  }

  public GithubAppClient createGithubAppClient() {
    return new GithubAppClient(this.github, this.owner);
  }

  /**
   * Suspend a user.
   *
   * @param username username of the user to suspend
   * @return a CompletableFuture that indicates success or failure
   */
  public CompletableFuture<Boolean> suspendUser(
      final String username, final SuspensionReason reason) {
    final String path = String.format(SUSPEND_USER_TEMPLATE, username);
    return github
        .put(path, github.json().toJsonUnchecked(reason))
        .thenApply(resp -> resp.code() == NO_CONTENT);
  }

  /**
   * Unsuspend a user.
   *
   * @param username username of the user to unsuspend
   * @return a CompletableFuture that indicates success or failure
   */
  public CompletableFuture<Boolean> unSuspendUser(
      final String username, final SuspensionReason reason) {
    final String path = String.format(SUSPEND_USER_TEMPLATE, username);
    return github
        .delete(path, github.json().toJsonUnchecked(reason))
        .thenApply(resp -> resp.code() == NO_CONTENT);
  }
}
