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

package com.spotify.github.v3.repos;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.UpdateTracking;
import com.spotify.github.v3.User;
import java.net.URI;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Status resource marks commits with a success, failure, error, or pending state, which is then
 * reflected in pull requests involving those commits.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableStatus.class)
@JsonDeserialize(as = ImmutableStatus.class)
public interface Status extends UpdateTracking {

  /** The state of the status. Can be one of pending, success, error, or failure. */
  @Nullable
  String state();

  /**
   * The target URL to associate with this status. This URL will be linked from the GitHub UI to
   * allow users to easily see the 'source' of the Status.
   */
  Optional<URI> targetUrl();

  /** A short description of the status. */
  Optional<String> description();

  /** Status id */
  @Nullable
  Long id();

  /** Status URL */
  @Nullable
  URI url();

  /**
   * A string label to differentiate this status from the status of other systems. Default:
   * "default"
   */
  Optional<String> context();

  /** Status creator user entity */
  Optional<User> creator();

  /** Deployment URL */
  Optional<URI> deploymentUrl();

  /** Repository URL */
  Optional<URI> repositoryUrl();
}
