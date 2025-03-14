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
import com.spotify.github.v3.repos.Repository;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Triggered when a user forks a repository. */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableForkEvent.class)
@JsonDeserialize(as = ImmutableForkEvent.class)
public interface ForkEvent extends BaseEvent {

  /** The {@link Repository} that was created. */
  @Nullable
  Repository forkee();
}
