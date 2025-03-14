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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.spotify.github.GithubStyle;
import java.util.Optional;
import org.immutables.value.Value;

/** The CheckRun response resource. */
@Value.Immutable
@GithubStyle
@JsonDeserialize(as = ImmutableCheckRunResponse.class)
public interface CheckRunResponse extends CheckRunBase {

  /**
   * The CheckRun ID.
   *
   * @return the int
   */
  long id();

  /**
   * Url string.
   *
   * @return the string
   */
  String url();

  /**
   * Html url string.
   *
   * @return the string
   */
  String htmlUrl();

  /**
   * The check run output.
   *
   * @see com.spotify.github.v3.checks.CheckRunOutput
   * @return the check run output
   */
  CheckRunOutput output();

  /**
   * Check suite this CheckRun belongs to.
   *
   * @return the optional
   */
  Optional<CheckSuite> checkSuite();

  /**
   * App which this check ran.
   *
   * @return the optional
   */
  Optional<App> app();
}
