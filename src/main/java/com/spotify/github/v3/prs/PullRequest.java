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

package com.spotify.github.v3.prs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.User;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Pull request resource represents data returned by a single PR get operation. It contains all the
 * fields from {@link PullRequestItem} entity.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutablePullRequest.class)
@JsonDeserialize(as = ImmutablePullRequest.class)
public interface PullRequest extends PullRequestItem {

  /** Is it merged. */
  @Nullable
  Boolean merged();

  /** Is it mergeable. */
  Optional<Boolean> mergeable();

  /** Merged by user. */
  Optional<User> mergedBy();

  /** Number of comments. */
  @Nullable
  Integer comments();

  /** Number of review (commit) comments. */
  @Nullable
  Integer reviewComments();

  /** Number of commits. */
  @Nullable
  Integer commits();

  /** Number of additions. */
  @Nullable
  Integer additions();

  /** Number of deletions. */
  @Nullable
  Integer deletions();

  /** Number of changed files. */
  @Nullable
  Integer changedFiles();

  /** The mergeable state of this PR. */
  @Nullable
  String mergeableState();

  /** Is it a draft PR? */
  Optional<Boolean> draft();
}
