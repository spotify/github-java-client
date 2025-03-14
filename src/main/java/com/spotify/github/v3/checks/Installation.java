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
import com.spotify.github.v3.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * The App Installation object.
 *
 * @see "https://developer.github.com/v3/apps/installations/"
 */
@Value.Immutable
@GithubStyle
@JsonDeserialize(as = ImmutableInstallation.class)
public interface Installation {

  /**
   * The installation ID.
   *
   * @return the int
   */
  int id();

  /**
   * Account user.
   *
   * @return the user
   */
  User account();

  /**
   * Access tokens url.
   *
   * @return the url string
   */
  String accessTokensUrl();

  /**
   * Repositories url.
   *
   * @return the url string
   */
  String repositoriesUrl();

  /**
   * Html URL to Github.
   *
   * @return the optional url
   */
  Optional<String> htmlUrl();

  /**
   * The APP ID the installations refers to.
   *
   * @return the int
   */
  int appId();

  /**
   * The ID of the entity it is installed on. Usually a repo.
   *
   * @return the int
   */
  int targetId();

  /**
   * Target type. Can be Organization.
   *
   * @return the string
   */
  String targetType();

  /**
   * Permissions map this installation has.
   *
   * @return the map
   */
  Map<String, String> permissions();

  /**
   * Events list this installation will consume, such as push, pull_request, etc.
   *
   * @return the list
   */
  List<String> events();

  /**
   * Single file name optional.
   *
   * @return the optional
   */
  Optional<String> singleFileName();

  /**
   * Indicates if the App is installed in all repos of the org, or just selected. Can be all or
   * selected.
   *
   * @return the optional string
   */
  Optional<String> repositorySelection();
}
