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

package com.spotify.github.v3.issues;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.CloseTracking;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.Milestone;
import com.spotify.github.v3.User;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Issue resource. */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableIssue.class)
@JsonDeserialize(as = ImmutableIssue.class)
public interface Issue extends CloseTracking {

  /** ID. */
  @Nullable
  Integer id();

  /** URL. */
  @Nullable
  URI url();

  /** Events URL. */
  @Nullable
  Optional<URI> eventsUrl();

  /** Repository URL. */
  @Nullable
  Optional<URI> repositoryUrl();

  /** Labels URL template. */
  @Nullable
  String labelsUrl();

  /** Comments URL. */
  @Nullable
  URI commentsUrl();

  /** HTML URL. */
  @Nullable
  URI htmlUrl();

  /** Number. */
  @Nullable
  Integer number();

  /** Indicates the state of the issues to return. Can be either open, closed, or all. */
  @Nullable
  String state();

  /** The title of the issue. */
  @Nullable
  String title();

  /** The contents of the issue. */
  @Nullable
  Optional<String> body();

  /** User. */
  @Nullable
  User user();

  /** A list of comma separated label names. Example: bug,ui,@highl. */
  @Nullable
  List<Label> labels();

  /** Login for the user that this issue should be assigned to. */
  Optional<User> assignee();

  /** The milestone associated this issue with. */
  Optional<Milestone> milestone();

  /** Is locked. */
  @Nullable
  Boolean locked();

  /** Number of comments. */
  @Nullable
  Integer comments();

  /** Pull request. */
  Optional<PullRequest> pullRequest();
}
