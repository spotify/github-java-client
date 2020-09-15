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

public class TeamTest {

  private String fixture;

  public static final void assertTeam(final Team team) {
    assertThat(team.id(), is(1));
    assertThat(team.name(), is("Justice League"));
    assertThat(team.slug(), is("justice-league"));
    assertThat(team.description(), is("A great team."));
    assertThat(team.privacy(), is("closed"));
    assertThat(team.permission(), is("admin"));
    assertThat(team.nodeId(), is("MDQ6VGVhbTE="));
    assertThat(team.url(), is(URI.create("https://api.github.com/teams/" + team.id())));
    assertThat(team.htmlUrl(), is(URI.create("https://api.github.com/teams/" + team.slug())));
    assertThat(team.repositoriesUrl(), is(URI.create(team.url() + "/repos")));
  }

  @Before
  public void setUp() throws Exception {
    fixture = Resources.toString(getResource(this.getClass(), "team.json"), defaultCharset());
  }

  @Test
  public void testDeserialization() throws IOException {
    final Team team = Json.create().fromJson(fixture, Team.class);
    assertTeam(team);
  }
}
