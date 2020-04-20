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
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Represents a created repository, branch, or tag. */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableCreateEvent.class)
@JsonDeserialize(as = ImmutableCreateEvent.class)
public interface CreateEvent extends BaseEvent {

  /** The git ref (or null if only a repository was created). */
  @Nullable
  String ref();

  /** The object that was created. Can be one of "repository", "branch", or "tag" */
  @Nullable
  String refType();

  /** The name of the repository's default branch (usually master). */
  @Nullable
  String masterBranch();

  /** The repository's current description. */
  @Nullable
  Optional<String> description();

  /** No doc found on github - Usually is "user". */
  @Nullable
  String pusherType();
}
