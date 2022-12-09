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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.spotify.github.GithubStyle;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * The interface Check run output. Includes all the details in the report
 *
 * @see "https://developer.github.com/v3/checks/runs/#parameters"
 */
@Value.Immutable
@GithubStyle
@JsonDeserialize(as = ImmutableCheckRunOutput.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface CheckRunOutput {

  /**
   * The title of the check run.
   *
   * @return the string
   */
  Optional<String> title();

  /**
   * The summary of the check run. This parameter supports Markdown.
   *
   * @return the optional
   */
  Optional<String> summary();

  /**
   * The details of the check run. This parameter supports Markdown.
   *
   * @return the optional
   */
  Optional<String> text();

  /**
   * Adds images to the output displayed in the GitHub pull request UI.
   *
   * @return the list
   */
  List<CheckRunImage> images();

  /**
   * Adds information from your analysis to specific lines of code. Annotations are visible on
   * GitHub in the Checks and Files changed tab of the pull request.
   *
   * @return the list
   */
  List<Annotation> annotations();

  /**
   * The count of annotations. Used on response objects.
   *
   * @return the optional
   */
  Optional<Integer> annotationsCount();

  /**
   * Annotations URL, used on response objects.
   *
   * @return the optional
   */
  Optional<String> annotationsUrl();

  /**
   * Automatically validates the maximum length of properties.
   * <p>
   * GitHub does not validate these properly on their side (at least in GHE 3.2) and returns 422
   * HTTP responses instead. To avoid that, let's validate the data in this client library.
   */
  @Value.Check
  @SuppressWarnings("checkstyle:magicnumber")
  default void check() {
    // max values from https://docs.github.com/en/enterprise-server@3.5/rest/checks/runs#create-a-check-run
    Preconditions.checkState(summary().map(String::length).orElse(0) <= 65535,
        "'summary' exceeded max length of 65535 characters");
    Preconditions.checkState(text().map(String::length).orElse(0) <= 65535,
        "'text' exceeded max length of 65535 characters");
  }
}
