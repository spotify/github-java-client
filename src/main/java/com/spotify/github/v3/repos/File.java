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
import java.net.URI;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** File resource. See {@link Commit} for example usage */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableFile.class)
@JsonDeserialize(as = ImmutableFile.class)
public interface File {

  /** File name */
  @Nullable
  String filename();

  /** Number of added lines */
  @Nullable
  Integer additions();

  /** Number of removed lines */
  @Nullable
  Integer deletions();

  /** Numnber of changed lines */
  @Nullable
  Integer changes();

  /** File status. E.g added, modified */
  @Nullable
  String status();

  /** Raw file content API URL */
  Optional<URI> rawUrl();

  /** Blob file content API URL */
  Optional<URI> blobUrl();

  /** Patch content */
  Optional<String> patch();

  /** Sha that included action taken on the given file */
  Optional<String> sha();

  /** File content API URL */
  Optional<URI> contentsUrl();
}
