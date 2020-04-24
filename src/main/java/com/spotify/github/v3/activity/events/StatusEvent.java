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
import com.spotify.github.UpdateTracking;
import com.spotify.github.v3.repos.Branch;
import com.spotify.github.v3.repos.CommitItem;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Triggered when the status of a Git commit changes.
 *
 * <p>Events of this type are not visible in timelines. These events are only used to trigger hooks.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableStatusEvent.class)
@JsonDeserialize(as = ImmutableStatusEvent.class)
public interface StatusEvent extends BaseEvent, UpdateTracking {

  /** Status event id */
  @Nullable
  Long id();

  /** The Commit SHA. */
  @Nullable
  String sha();

  /** Full repository name. E.g. organization/repo-name */
  @Nullable
  String name();

  /** The optional link added to the status. */
  Optional<URI> targetUrl();

  /**
   * A string label to differentiate this status from the status of other systems. Example:
   * continuous-integration/jenkins Default: "default"
   */
  @Nullable
  String context();

  /** The optional human-readable description added to the status. */
  @Nullable
  Optional<String> description();

  /** The new state. Can be pending, success, failure, or error. */
  @Nullable
  String state();

  /** Related git commit */
  @Nullable
  CommitItem commit();

  /**
   * An array of branch objects containing the status' SHA. Each branch contains the given SHA, but
   * the SHA may or may not be the head of the branch. The array includes a maximum of 10 branches.
   */
  @Nullable
  List<Branch> branches();
}
