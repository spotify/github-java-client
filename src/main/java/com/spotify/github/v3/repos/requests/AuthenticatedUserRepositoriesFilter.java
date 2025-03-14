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

package com.spotify.github.v3.repos.requests;

import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.Parameters;
import org.immutables.value.Value;

/**
 * Filter parameters for listing authenticated user's repositories. To be
 * serialized as key=value.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableAuthenticatedUserRepositoriesFilter.class)
@JsonDeserialize(as = ImmutableAuthenticatedUserRepositoriesFilter.class)
public interface AuthenticatedUserRepositoriesFilter extends Parameters {

  /**
   * Can be one of all, public, or private. Default: all
   */
  @Nullable String visibility();

  /**
   * Comma-separated list of values. Can include:
   * * owner: Repositories that are owned by the authenticated user.
   * * collaborator: Repositories that the user has been added to as a
   *   collaborator.
   * * organization_member: Repositories that the user has access to through
   *   being a member of an organization. This includes every repository on
   *   every team that the user is on.
   *
   * Default: owner,collaborator,organization_member
   */
  @Nullable String affiliation();

  /**
   * Can be one of all, owner, public, private, member. Default: all
   * Will cause a 422 error if used in the same request as visibility or
   * affiliation.
   */
  @Nullable String type();

  /**
   * Can be one of created, updated, pushed, full_name. Default: full_name
   */
  @Nullable String sort();

  /**
   * Can be one of asc or desc. Default: asc when using full_name, otherwise
   * desc
   * */
  @Nullable String direction();
}
