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
import com.spotify.github.v3.git.ShaLink;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Single repository commit resource. */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableCommit.class)
@JsonDeserialize(as = ImmutableCommit.class)
public interface Commit extends CommitItem {

  /** Commit statistics key, value map. E.g. additions: 104 deletions: 4 total: 108 */
  @Nullable
  Map<String, Integer> stats();

  /** File objects included in the commit. */
  @Nullable
  List<File> files();

  /** The SHA of the tree object this commit points to. */
  @Nullable
  ShaLink tree();
}
