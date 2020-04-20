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

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class UserTest {

  private String fixture;

  public static final void assertUser(final User user) {
    assertThat(user.login(), is("octocat"));
    assertThat(user.id(), is(1));
    assertThat(
        user.avatarUrl(), is(URI.create("https://github.com/images/error/octocat_happy.gif")));
    assertThat(user.gravatarId(), is(Optional.of("")));
    assertThat(user.url(), is(URI.create("https://api.github.com/users/" + user.login())));
    assertThat(user.htmlUrl(), is(URI.create("https://github.com/" + user.login())));
    assertThat(user.followersUrl(), is(URI.create(user.url() + "/followers")));
    assertThat(user.type(), is("User"));
    assertThat(user.siteAdmin().get(), is(false));
  }

  @Before
  public void setUp() throws Exception {
    fixture = Resources.toString(getResource(this.getClass(), "user.json"), defaultCharset());
  }

  @Test
  public void testDeserialization() throws IOException {
    final User user = Json.create().fromJson(fixture, User.class);
    assertUser(user);
  }
}
