/*-
 * -\-\-
 * github-client
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.spotify.github.FixtureHelper;
import com.spotify.github.jackson.Json;
import java.io.IOException;
import java.time.ZonedDateTime;
import org.junit.Test;

public class AccessTokenTest {
  private final Json json = Json.create();

  @Test
  public void canDeserializeToken() throws IOException {
    final AccessToken accessToken =
        json.fromJson(FixtureHelper.loadFixture("checks/access-token.json"), AccessToken.class);
    assertThat(accessToken.token(), is("v1.1f699f1069f60xxx"));
    assertThat(accessToken.expiresAt(), is(ZonedDateTime.parse("2016-07-11T22:14:10Z")));
  }
}
