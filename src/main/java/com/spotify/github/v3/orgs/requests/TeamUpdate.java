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
package com.spotify.github.v3.orgs.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Request to create a team within a given organisation */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableTeamUpdate.class)
@JsonDeserialize(as = ImmutableTeamUpdate.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface TeamUpdate {

  /** The name of the team. */
  @Nullable
  String name();

  /** The description of the team. */
  Optional<String> description();

  /**
   * The level of privacy this team should have.
   * For a non-nested team:
   * secret - only visible to organization owners and members of this team.
   * closed - visible to all members of this organization.
   * Default: secret
   * For a parent or child team:
   * closed - visible to all members of this organization.
   * Default for child team: closed
   * Can be one of: secret, closed
   */
  Optional<String> privacy();

  /**
   * The notification setting the team has chosen. The options are:
   * notifications_enabled - team members receive notifications when the team is @mentioned.
   * notifications_disabled - no one receives notifications.
   * Default: notifications_enabled
   * Can be one of: notifications_enabled, notifications_disabled
   */
  @JsonProperty("notification_setting")
  Optional<String> notificationSetting();


  /** The ID of a team to set as the parent team. */
  @JsonProperty("parent_team_id")
  Optional<Integer> parentTeamId();
}
