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
. */

package com.spotify.github.v3.repos;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.git.Author;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Push commit object used in {@link com.spotify.github.v3.activity.events.PushEvent}. */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutablePushCommit.class)
@JsonDeserialize(as = ImmutablePushCommit.class)
public interface PushCommit {

  /** Commit id. */
  @Nullable
  String id();

  /** Tree id. */
  @Nullable
  String treeId();

  /** Whether this commit is distinct from any that have been pushed before.. */
  @Nullable
  Boolean distinct();

  /** Commit message. */
  @Nullable
  String message();

  /** Timestamp. */
  @Nullable
  ZonedDateTime timestamp();

  /** Points to the commit API resource.. */
  @Nullable
  URI url();

  /** Commit author. */
  @Nullable
  Author author();

  /** Commit committer. */
  @Nullable
  Author committer();

  /** Files that were added. */
  @Nullable
  List<String> added();

  /** Files that were removed. */
  @Nullable
  List<String> removed();

  /** Files that were modified. */
  @Nullable
  List<String> modified();
}
