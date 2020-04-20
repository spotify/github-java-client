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

package com.spotify.github.v3.comment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.UpdateTracking;
import com.spotify.github.v3.User;
import java.net.URI;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Repository commit comment resource */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableComment.class)
@JsonDeserialize(as = ImmutableComment.class)
public interface Comment extends UpdateTracking {

  /** Comment API URL. */
  @Nullable
  URI url();

  /** Comment URL. */
  @Nullable
  URI htmlUrl();

  /** Comment ID. */
  int id();

  /** The {@link User} that made the comment. */
  @Nullable
  User user();

  /** Line index in the diff to comment on. */
  Optional<Integer> position();

  /**
   * Line number in the file to comment on.
   *
   * @deprecated Use {@link #position()} instead
   */
  Optional<Integer> line();

  /** Relative path of the file to comment on. */
  Optional<String> path();

  /** Commit sha this comment relates to */
  Optional<String> commitId();

  /** The contents of the comment. */
  @Nullable
  String body();

  /** The issueURL which the comment belongs to. */
  Optional<URI> issueUrl();
}
