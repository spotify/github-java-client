/*-
 * -\-\-
 * github-client
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * The parameters for merging a Pull Request.
 *
 * @see "https://developer.github.com/v3/pulls/#input-3"
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableMergeParameters.class)
@JsonDeserialize(as = ImmutableMergeParameters.class)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public abstract class MergeParameters {
  /**
   * SHA that pull request head must match to allow merge.
   *
   * @return the string
   */
  public abstract String sha();

  /**
   * Extra detail to append to automatic commit message.
   *
   * @return the optional
   */
  public abstract Optional<String> commitMessage();

  /**
   * Title for the automatic commit message.
   *
   * @return the optional commit title
   */
  public abstract Optional<String> commitTitle();

  /**
   * Merge method to use.
   *
   * @return the merge method enum value
   */
  @Value.Default
  public MergeMethod mergeMethod() {
    return MergeMethod.merge;
  }
}
