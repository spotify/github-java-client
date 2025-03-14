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

package com.spotify.github.v3.prs.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Pull request create request resource */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutablePullRequestCreate.class)
@JsonDeserialize(as = ImmutablePullRequestCreate.class)
public interface PullRequestCreate {

  /** The title of the pull request. */
  @Nullable
  String title();

  /** The contents of the pull request. */
  Optional<String> body();

  /**
   * The name of the branch where your changes are implemented. For cross-repository pull requests
   * in the same network, namespace head with a user like this: username:branch.
   */
  @Nullable
  String head();

  /**
   * The name of the branch you want your changes pulled into. This should be an existing branch on
   * the current repository. You cannot submit a pull request to one repository that requests a
   * merge to a base of another repository.
   */
  @Nullable
  String base();

  /**
   * Indicates whether the pull request is a draft.
   */
  @Nullable
  Boolean draft();
}
