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

package com.spotify.github.v3.actions.workflowruns;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.Parameters;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableQueryParams.class)
@JsonDeserialize(as = ImmutableQueryParams.class)
public interface QueryParams extends Parameters {
  /**
   * Returns someone's workflow runs. Use the login for the user who created the push associated with the check suite or workflow run.
   */
  Optional<String> actor();

  /**
   * Returns workflow runs associated with a branch. Use the name of the branch of the push.
   */
  Optional<String> branch();

  /**
   * Returns workflow run triggered by the event you specify. For example, push, pull_request or issue. For more information, see "Events that trigger workflows."
   */
  Optional<String> event();

  /**
   * Returns workflow runs with the check run status or conclusion that you specify. For example, a conclusion can be success or a status can be in_progress. Only GitHub Actions can set a status of waiting, pending, or requested.
   * &gt;p&lt;
   * Can be one of: completed, action_required, cancelled, failure, neutral, skipped, stale, success, timed_out, in_progress, queued, requested, waiting, pending
   */
  Optional<WorkflowRunStatus> status();

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

  /**
   * Returns workflow runs created within the given date-time range. Syntax with examples:
   * &gt;p&lt;
   * &lt;YYYY-MM-DD   created:&lt;2016-04-29 matches workflow runs that were created after April 29, 2016.
   * &lt;=YYYY-MM-DD   created:&lt;=2017-04-01 matches workflow runs that were created on or after April 1, 2017.
   * &gt;YYYY-MM-DD   pushed:&gt;2012-07-05 matches workflow runs that were pushed to before July 5, 2012.
   * &gt;=YYYY-MM-DD   created:&gt;=2012-07-04 matches workflow runs that were created on or before July 4, 2012.
   * YYYY-MM-DD..YYYY-MM-DD   pushed:2016-04-30..2016-07-04 matches workflow runs that were pushed to between the end of April and July of 2016.
   * YYYY-MM-DD..*   created:2012-04-30..* matches workflow runs created on or after April 30th, 2012 containing the word "cats."
   * *..YYYY-MM-DD    created:*..2012-07-04 matches workflow runs created on or before July 4th, 2012 containing the word "cats."
   * &gt;p&lt;
   * You can also add optional time information THH:MM:SS+00:00 after the date, to filter by the hour, minute, and second. That's T, followed by HH:MM:SS (hour-minutes-seconds), and a UTC offset (+00:00).
   * &gt;p&lt;
   * Query  Example
   * YYYY-MM-DDTHH:MM:SS+00:00 created:2017-01-01T01:00:00+07:00..2017-03-01T15:30:15+07:00 matches workflow runs created between January 1, 2017 at 1 a.m. with a UTC offset of 07:00 and March 1, 2017 at 3 p.m. with a UTC offset of 07:00.
   * YYYY-MM-DDTHH:MM:SSZ  created:2016-03-21T14:11:00Z..2016-04-07T20:45:00Z matches workflow runs created between March 21, 2016 at 2:11pm and April 7, 2016 at 8:45pm.
   */
  Optional<String> created();

  /**
   * If true pull requests are omitted from the response (empty array).
   * &gt;p&lt;
   * Default: false
   */
  Optional<Boolean> excludePullRequests();

  /**
   * Returns workflow runs with the check_suite_id that you specify.
   */
  Optional<Integer> checkSuiteId();

  /**
   * Only returns workflow runs that are associated with the specified head_sha.
   */
  Optional<String> headSha();
}
