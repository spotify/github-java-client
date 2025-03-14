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
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Deployments are a request for a specific ref (branch, SHA, tag) to be deployed. */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableDeployment.class)
@JsonDeserialize(as = ImmutableDeployment.class)
public interface Deployment extends UpdateTracking {

  /** Deployment API URL */
  @Nullable
  URI url();

  /** The Deployment ID to list the statuses from. */
  @Nullable
  Long id();

  /** The SHA that was recorded at creation time. */
  @Nullable
  String sha();

  /** The name of the ref. This can be a branch, tag, or SHA. */
  @Nullable
  String ref();

  /** The name of the task */
  @Nullable
  String task();

  /** Optional JSON payload with extra information about the deployment. */
  Optional<Map<String, String>> payload();

  /** The name of the environment that was deployed to. e.g. staging or production. */
  @Nullable
  String environment();

  /** Optional short description. */
  Optional<String> description();

  /** Deployment creator */
  @Nullable
  User creator();

  /** Deployment statuses API URL */
  @Nullable
  URI statusesUrl();

  /** Deployment repository API URL */
  @Nullable
  URI repositoryUrl();
}
