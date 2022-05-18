/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2016 - 2022 Spotify AB
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

package com.spotify.github.v3.repos;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.User;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Collaborator Invitation resource */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableRepositoryInvitation.class)
@JsonDeserialize(as = ImmutableRepositoryInvitation.class)
public interface RepositoryInvitation {

  /** Unique identifier of the repository invitation */
  Integer id();

  /** Node ID */
  String nodeId();

  /** The repository that the invitee is being invited to */
  Repository repository();

  /** The user that is receiving the invite */
  @Nullable
  User invitee();

  /** The user that sent the invite */
  @Nullable
  User inviter();

  /** The permission associated with the invitation */
  String permissions();

  /** Date when invite was created */
  ZonedDateTime createdAt();

  /** Whether or not the invitation has expired */
  @Nullable
  Optional<Boolean> expired();

  /** API URL */
  URI url();

  /** HTML URL */
  URI htmlUrl();
}
