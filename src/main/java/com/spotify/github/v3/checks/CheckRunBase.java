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

package com.spotify.github.v3.checks;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * The CheckRun base. Used for composing CheckRun objects, as the request and response have slightly
 * different formats.
 *
 * @see "https://developer.github.com/v3/checks/runs/"
 */
public interface CheckRunBase {
  /**
   * The SHA of the commit.
   *
   * @return the string
   */
  String headSha();

  /**
   * The name of the check. For example, "code-coverage".
   *
   * @return the string
   */
  String name();

  /**
   * The current status. Can be one of queued, in_progress, or completed.
   *
   * @return the check run status enum
   */
  CheckRunStatus status();

  /**
   * The final conclusion of the check. Can be one of success, failure, neutral, cancelled,
   * timed_out, or action_required. When the conclusion is action_required, additional details
   * should be provided on the site specified by details_url. Required if you provide completedAt or
   * a status of completed.
   *
   * @return the conclusion
   */
  Optional<CheckRunConclusion> conclusion();

  /**
   * The time that the check run began.
   *
   * @return the zoned date time
   */
  Optional<ZonedDateTime> startedAt();

  /**
   * The time that the check completed.
   *
   * @return the zoned date time
   */
  Optional<ZonedDateTime> completedAt();

  /**
   * A reference for the run on the integrator's system.
   *
   * @return the optional string
   */
  Optional<String> externalId();

  /**
   * The URL of the integrator's site that has the full details of the check. If the integrator does
   * not provide this, then the homepage of the GitHub app is used.
   *
   * @return the optional
   */
  Optional<String> detailsUrl();
}
