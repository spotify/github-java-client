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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.CloseTracking;
import com.spotify.github.GitHubInstant;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.Milestone;
import com.spotify.github.v3.User;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Pull request item resource represents data returned during pull request list operation */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutablePullRequestItem.class)
@JsonDeserialize(as = ImmutablePullRequestItem.class)
public interface PullRequestItem extends CloseTracking {

  /** ID. */
  @Nullable
  Integer id();

  /** URL. */
  @Nullable
  URI url();

  /** HTML URL. */
  @Nullable
  URI htmlUrl();

  /** Diff URL. */
  @Nullable
  URI diffUrl();

  /** Patch URL. */
  @Nullable
  URI patchUrl();

  /** Issue URL. */
  @Nullable
  URI issueUrl();

  /** Commits URL. */
  @Nullable
  URI commitsUrl();

  /** Number. */
  @Nullable
  Integer number();

  /** Either open, closed, or all to filter by state. Default: open. */
  @Nullable
  String state();

  /** The title of the pull request. */
  @Nullable
  String title();

  /** The contents of the pull request. */
  Optional<String> body();

  /** Assignee. */
  Optional<User> assignee();

  /** Assignees. */
  Optional<List<User>> assignees();

  /** Milestone. */
  Optional<Milestone> milestone();

  /** Is it locked. */
  @Nullable
  Boolean locked();

  /** Merged date. */
  Optional<GitHubInstant> mergedAt();

  /** Head reference. */
  @Nullable
  PullRequestRef head();

  /** Base reference. */
  @Nullable
  PullRequestRef base();

  /** User. */
  @Nullable
  User user();

  /** Statuses API URL. */
  @Nullable
  URI statusesUrl();

  /** Review comments API URL. */
  @Nullable
  URI reviewCommentsUrl();

  /** Review comment API URL template. */
  @Nullable
  String reviewCommentUrl();

  /** Comments API URL. */
  @Nullable
  URI commentsUrl();

  /** Link references. */
  @Nullable
  @JsonProperty("_links")
  PullRequestLinks links();

  /** Requested reviewers (users) */
  @Nullable
  @JsonProperty("requested_reviewers")
  List<User> requestedReviewers();

  /** Requested reviewers (teams) */
  @Nullable
  @JsonProperty("requested_teams")
  List<User> requestedTeams();

  /** @Deprecated the merge commit sha. */
  Optional<String> mergeCommitSha();
}
