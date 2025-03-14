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

package com.spotify.github.v3.activity.events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.prs.PullRequest;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Triggered when a pull request is assigned, unassigned, labeled, unlabeled, opened, edited,
 * closed, reopened, or synchronized.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutablePullRequestEvent.class)
@JsonDeserialize(as = ImmutablePullRequestEvent.class)
public interface PullRequestEvent extends BaseEvent {

  /**
   * The action that was performed. Can be one of "assigned", "unassigned", "labeled", "unlabeled",
   * "opened", "edited", "closed", or "reopened", or "synchronize". If the action is "closed" and
   * the merged key is false, the pull request was closed with unmerged commits. If the action is
   * "closed" and the merged key is true, the pull request was merged.
   */
  @Nullable
  String action();

  /** The pull request number. */
  @Nullable
  Integer number();

  /** The pull request */
  @Nullable
  PullRequest pullRequest();
}
