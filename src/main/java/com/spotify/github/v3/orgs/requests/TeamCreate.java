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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Request to create a team within a given organisation */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableTeamCreate.class)
@JsonDeserialize(as = ImmutableTeamCreate.class)
public interface TeamCreate {

  /** The name of the team. */
  @Nullable
  String name();

  /** The description of the team. */
  Optional<String> description();

  /**
   * List GitHub IDs for organization members who will
   * become team maintainers.
   */
  Optional<String> maintainers();

  /** The full name (e.g., "organization-name/repository-name")
   * of repositories to add the team to.
   */
  @SuppressWarnings("checkstyle:methodname")
  Optional<String> repo_names();

  /** The ID of a team to set as the parent team. */
  @SuppressWarnings("checkstyle:methodname")
  Optional<String> parent_team_id();
}
