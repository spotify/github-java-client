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
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Represents a deleted branch or tag event */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableDeleteEvent.class)
@JsonDeserialize(as = ImmutableDeleteEvent.class)
public interface DeleteEvent extends BaseEvent {

  /** Full git reference */
  @Nullable
  String ref();

  /** The object that was deleted. Can be "branch" or "tag". */
  @Nullable
  String refType();

  /**
   * Pusher type. E.g. user. The github api does not document any other types, yet. To be totally
   * honest, it does not document *ANYTHING* about this field, so leave it as string for now.
   */
  @Nullable
  String pusherType();
}
