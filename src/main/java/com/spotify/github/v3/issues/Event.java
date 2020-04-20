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
import com.spotify.github.GitHubInstant;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.User;
import java.net.URI;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Issue event resource. Records various events that occur around an issue or pull request. This is
 * useful both for display on issue/pull request information pages and also to determine who should
 * be notified of comments.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableEvent.class)
@JsonDeserialize(as = ImmutableEvent.class)
public interface Event {

  /** The Integer ID of the event. */
  @Nullable
  Integer id();

  /** The API URL for fetching the event. */
  @Nullable
  URI url();

  /** The User object that generated the event. */
  @Nullable
  User actor();

  /** Identifies the actual type of Event that occurred. */
  @Nullable
  String event();

  /** The String SHA of a commit that referenced this Issue. */
  @Nullable
  String commitId();

  /** The GitHub API link to a commit that referenced this Issue. */
  @Nullable
  URI commitUrl();

  /** The timestamp indicating when the event occurred. */
  @Nullable
  GitHubInstant createdAt();

  /** Issue. */
  Optional<Issue> issue();
}
