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
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.User;
import com.spotify.github.v3.repos.Repository;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Pull request reference resource */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutablePullRequestRef.class)
@JsonDeserialize(as = ImmutablePullRequestRef.class)
public interface PullRequestRef {

  /** Label */
  Optional<String> label();

  /** Reference */
  @Nullable
  String ref();

  /** SHA */
  @Nullable
  String sha();

  /** User */
  Optional<User> user();

  /** Repository */
  Optional<Repository> repo();
}
