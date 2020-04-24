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
import com.spotify.github.GitHubInstant;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.User;
import com.spotify.github.v3.activity.events.ReviewLinks;
import java.net.URI;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Pull request review resource represents data returned by a single PR review get operation. It
 * contains all the fields from {@link Review} entity.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableReview.class)
@JsonDeserialize(as = ImmutableReview.class)
public interface Review {

  /** Id. */
  @Nullable
  Integer id();

  /** User. */
  @Nullable
  User user();

  /** Body. */
  @Nullable
  Optional<String> body();

  /** Submitted at. */
  Optional<GitHubInstant> submittedAt();

  /** State. */
  @Nullable
  String state();

  /** Html URL. */
  @Nullable
  URI htmlUrl();

  /** Pull Request URL. */
  @Nullable
  URI pullRequestUrl();

  /** Hypermedia links. */
  @Nullable
  @JsonProperty("_links")
  ReviewLinks links();

  /** Commit sha the review refers to */
  @Nullable
  String commitId();
}
