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
import com.spotify.github.v3.issues.Issue;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Triggered when an issue is assigned, unassigned, labeled, unlabeled, opened, edited, closed, or
 * reopened.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableIssuesEvent.class)
@JsonDeserialize(as = ImmutableIssuesEvent.class)
public interface IssuesEvent extends BaseEvent {

  /**
   * The action that was performed. Can be one of "assigned", "unassigned", "labeled", "unlabeled",
   * "opened", "edited", "closed", or "reopened".
   */
  @Nullable
  String action();

  /** Issue */
  @Nullable
  Issue issue();
}
