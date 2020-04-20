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
import org.immutables.value.Value;

/** The Github Installation Access token. */
@Value.Immutable
@GithubStyle
@JsonDeserialize(as = ImmutableAccessToken.class)
public interface AccessToken {

  /**
   * Access Token.
   *
   * @return the token content
   */
  String token();

  /**
   * Token expiration date.
   *
   * @return the zoned date time
   */
  ZonedDateTime expiresAt();
}
