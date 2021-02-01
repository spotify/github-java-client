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
import com.spotify.github.Parameters;
import java.util.Optional;
import org.immutables.value.Value;

/** Pull request retrieval parameters resource */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutablePullRequestParameters.class)
@JsonDeserialize(as = ImmutablePullRequestParameters.class)
public interface PullRequestParameters extends Parameters {

  /** Either open, closed, or all to filter by state. Default: open */
  Optional<String> state();

  /**
   * Filter pulls by head user and branch name in the format of user:ref-name. Example:
   * github:new-script-format.
   */
  Optional<String> head();

  /** Filter pulls by base branch name. Example: gh-pages. */
  Optional<String> base();

  /**
   * What to sort results by. Can be either created, updated, popularity (comment count) or
   * long-running (age, filtering by pulls updated in the last month). Default: created
   */
  Optional<String> sort();

  /**
   * The direction of the sort. Can be either asc or desc. Default: desc when sort is created or
   * sort is not specified, otherwise asc.
   */
  Optional<String> direction();

  /**
   * Results per page (max 100)
   */
  @SuppressWarnings("checkstyle:methodname")
  Optional<Integer> per_page();

  /**
   * Page number of the results to fetch.
   */
  Optional<Integer> page();

  /** Serialize declared non-Object methods as key=value joined by & */
}
