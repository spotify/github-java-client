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

package com.spotify.github.v3;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.net.URI;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** User resource */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableUser.class)
@JsonDeserialize(as = ImmutableUser.class)
public interface User {

  /** Login name. */
  @Nullable
  String login();

  /** ID. */
  @Nullable
  Integer id();

  /** Avatar URL. */
  @Nullable
  URI avatarUrl();

  /** Gravatar ID. */
  Optional<String> gravatarId();

  /** User resource API URL. */
  @Nullable
  URI url();

  /** User resource URL returning HTML. */
  @Nullable
  URI htmlUrl();

  /** Followers URL. */
  @Nullable
  URI followersUrl();

  /** Following URL template. */
  @Nullable
  String followingUrl();

  /** Gists URL template. */
  @Nullable
  String gistsUrl();

  /** Starred URL template. */
  @Nullable
  String starredUrl();

  /** Subscriptions URL. */
  @Nullable
  URI subscriptionsUrl();

  /** Organizations URL. */
  @Nullable
  URI organizationsUrl();

  /** Repositories URL. */
  @Nullable
  URI reposUrl();

  /** Events URL template. */
  @Nullable
  String eventsUrl();

  /** Received event URL. */
  @Nullable
  URI receivedEventsUrl();

  /** User type. */
  @Nullable
  String type();

  /** Is user a site admin. */
  Optional<Boolean> siteAdmin();
}
