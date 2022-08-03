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
import com.google.common.base.Preconditions;
import com.spotify.github.GithubStyle;
import org.immutables.value.Value;

/**
 * The CheckRun action.
 *
 * @see "https://developer.github.com/v3/checks/runs/#check-runs-and-requested-actions"
 */
@Value.Immutable
@GithubStyle
@JsonDeserialize(as = ImmutableCheckRunAction.class)
public interface CheckRunAction {

  /**
   * The label to be shown at the action button.
   *
   * @return the string
   */
  String label();

  /**
   * The identifier to be sent at the event When a user clicks the button, GitHub sends the
   * check_run.requested_action webhook to your app. When your app receives a
   * check_run.requested_action webhook event, it can look for the requested_action.identifier key
   * in the webhook payload to determine which button was clicked and perform the requested task.
   *
   * @return the string
   */
  String identifier();

  /**
   * Description string.
   *
   * @return the string
   */
  String description();

  /**
   * Automatically validates the maximum length of properties.
   *
   * GitHub does not validate these properly on their side (at least in GHE 3.2)
   * and returns 5xx HTTP responses instead. To avoid that, let's validate the data
   * in this client library.
   */
  @Value.Check
  @SuppressWarnings("checkstyle:magicnumber")
  default void check() {
    // max values from https://docs.github.com/en/rest/checks/runs
    Preconditions.checkState(label().length() <= 20,
        "'label' exceeded max length of 20");
    Preconditions.checkState(identifier().length() <= 20,
        "'identifier' exceeded max length of 20");
    Preconditions.checkState(description().length() <= 40,
        "'description' exceeded max length of 40");
  }
}
