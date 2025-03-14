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

package com.spotify.github.v3.repos.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.net.URI;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Request to create commit statuses for a given ref. */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableRepositoryCreateStatus.class)
@JsonDeserialize(as = ImmutableRepositoryCreateStatus.class)
public interface RepositoryCreateStatus {

  /** The state of the status. Can be one of pending, success, error, or failure. */
  @Nullable
  String state();

  /** The target URL to associate with this status. */
  @Nullable
  URI targetUrl();

  /** A short description of the status. Must be less than 1024 bytes. */
  @Nullable
  String description();

  /**
   * A string label to differentiate this status from the status of other systems. Default:
   * "default"
   */
  @Nullable
  String context();
}
