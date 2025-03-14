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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

/** The interface App. */
@Value.Immutable
@GithubStyle
@JsonDeserialize(as = ImmutableApp.class)
public interface App {

  /**
   * The App ID.
   *
   * @return the ID
   */
  Integer id();

  /**
   * The URL-friendly name of the GitHub App.
   *
   * @return the optional sting
   */
  Optional<String> slug();

  /**
   * The App name
   *
   * @return the string
   */
  String name();

  /**
   * The App Description.
   *
   * @return the string
   */
  String description();

  /**
   * External url string.
   *
   * @return the string
   */
  String externalUrl();

  /**
   * Html url string.
   *
   * @return the string
   */
  String htmlUrl();

  /**
   * The date the App was created.
   *
   * @return the zoned date time
   */
  ZonedDateTime createdAt();

  /**
   * The date the App was updated.
   *
   * @return the zoned date time
   */
  ZonedDateTime updatedAt();

  /**
   * The permissions the installation of the app has.
   *
   * @see "https://developer.github.com/apps/building-github-apps/creating-github-apps-using-url
   *     -parameters/#github-app-permissions"
   * @return the map with permissions
   */
  Map<String, String> permissions();

  /**
   * Events list this App will consume, such as push, pull_request, etc.
   *
   * @return the list
   */
  List<String> events();

  /**
   * Installation count of the App.
   *
   * @return the optional count
   */
  Optional<Integer> installationsCount();
}
