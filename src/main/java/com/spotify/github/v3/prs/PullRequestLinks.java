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
import com.spotify.github.Links;
import com.spotify.github.v3.comment.Comment;
import java.net.URI;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Reference links for the PullRequest entity */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutablePullRequestLinks.class)
@JsonDeserialize(as = ImmutablePullRequestLinks.class)
public interface PullRequestLinks extends Links {

  /** API link to the pull request. */
  @Nullable
  Href<URI> issue();

  /** API link to the comments on this pull request. {@link Comment} */
  @Nullable
  Href<URI> comments();

  /** API link template to a specific review comments on this pull request. */
  @Nullable
  Href<String> reviewComment();

  /** API link to the review comments on this pull request. {@link Comment} */
  @Nullable
  Href<URI> reviewComments();

  /**
   * API link to the commits on this pull request. {@link com.spotify.github.v3.repos.CommitItem}
   */
  @Nullable
  Href<URI> commits();

  /** API link to the statuses on this pull request. {@link com.spotify.github.v3.repos.Status} */
  @Nullable
  Href<URI> statuses();
}
