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

package com.spotify.github.v3.actions.workflowjobs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.Parameters;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableListWorkflowJobsQueryParams.class)
@JsonDeserialize(as = ImmutableListWorkflowJobsQueryParams.class)
public interface ListWorkflowJobsQueryParams extends Parameters {
  Optional<Filter> filter();

  /**
   * The number of results per page (max 100). For more information, see "Using pagination in the REST API."
   * &gt;p&lt;
   * Default: 30
   */
  Optional<Integer> perPage();

  /**
   * The page number of the results to fetch. For more information, see "Using pagination in the REST API."
   * &gt;p&lt;
   * Default: 1
   */
  Optional<Integer> page();

  enum Filter {
    latest,
    completed_at,
    all
  }
}
