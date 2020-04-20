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
import java.util.List;
import org.immutables.value.Value;

/** The interface InstallationList content. */
@Value.Immutable
@GithubStyle
@JsonDeserialize(as = ImmutableInstallationList.class)
public interface InstallationList {

  /**
   * Count of installations of the App the caller has access to.
   *
   * @see "https://developer.github.com/v3/apps/installations/#list-installations-for-a-user"
   * @return the int
   */
  int totalCount();

  /**
   * Installations list.
   *
   * @return the list
   */
  List<Installation> installations();
}
