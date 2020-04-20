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
import com.spotify.github.v3.repos.Organization;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Triggered when a repository is created, deleted, made public, or made private.
 *
 * <p>Events of this type are not visible in timelines. These events are only used to trigger hooks.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableRepositoryEvent.class)
@JsonDeserialize(as = ImmutableRepositoryEvent.class)
public interface RepositoryEvent extends BaseEvent {

  /**
   * The action that was performed. This can be one of "created", "deleted", "publicized", or
   * "privatized".
   */
  @Nullable
  String action();

  /** Organization */
  @Nullable
  Organization organization();
}
