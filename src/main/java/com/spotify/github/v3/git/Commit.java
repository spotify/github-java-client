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

package com.spotify.github.v3.git;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.net.URI;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Commit details resource. */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableCommit.class)
@JsonDeserialize(as = ImmutableCommit.class)
public interface Commit {

  /** Commit sha value. */
  Optional<String> sha();

  /** Commit API URL. */
  @Nullable
  URI url();

  /** Author commit user. */
  @Nullable
  Author author();

  /** Committer commit user. */
  @Nullable
  Author committer();

  /** Commit message. */
  @Nullable
  String message();

  /** The SHA of the tree object this commit points to. */
  @Nullable
  ShaLink tree();

  /** Number of comments. */
  Optional<Integer> commentCount();
}
