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

package com.spotify.github.v3.orgs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.User;
import java.net.URI;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableTeamInvitation.class)
@JsonDeserialize(as = ImmutableTeamInvitation.class)
public interface TeamInvitation {
  /** ID */
  @Nullable
  Integer id();

  /** login username */
  @Nullable
  String login();

  /** Node ID */
  @Nullable
  String nodeId();

  /** Email address */
  @Nullable
  String email();

  /** Role */
  @Nullable
  String role();

  /** Failed reason */
  @Nullable
  String failedReason();

  /** Inviter */

  @Nullable
  User inviter();

  /** Team Count */
  @Nullable
  Integer teamCount();

  /** Invitation Teams URL */
  @Nullable
  URI invitationTeamsUrl();

  /** Invitation Source */
  @Nullable
  String invitationSource();
}
