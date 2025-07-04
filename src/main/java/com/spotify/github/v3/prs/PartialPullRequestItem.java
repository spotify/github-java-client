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
package com.spotify.github.v3.prs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.CloseTracking;
import com.spotify.github.GithubStyle;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.net.URI;

@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutablePartialPullRequestItem.class)
@JsonDeserialize(as = ImmutablePartialPullRequestItem.class)
public interface PartialPullRequestItem extends CloseTracking {
  /** ID. */
  @Nullable
  Long id();

  /** URL. */
  @Nullable
  URI url();

  /** Number. */
  @Nullable
  Long number();

  /** Head reference. */
  @Nullable
  PullRequestRef head();

  /** Base reference. */
  @Nullable
  PullRequestRef base();
}
