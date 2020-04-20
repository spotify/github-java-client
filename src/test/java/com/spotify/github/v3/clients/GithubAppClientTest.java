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

package com.spotify.github.v3.clients;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.checks.AccessToken;
import com.spotify.github.v3.checks.InstallationList;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Test;

public class GithubAppClientTest {

  private static final String FIXTURES_PATH = "com/spotify/github/v3/githubapp/";
  private Json json;

  public static String loadResource(final String path) {
    try {
      return Resources.toString(Resources.getResource(path), UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Before
  public void setUp() {
    final GitHubClient github = mock(GitHubClient.class);
    final GithubAppClient client = new GithubAppClient(github, "org", "repo");
    json = Json.create();
    when(github.json()).thenReturn(json);
  }

  @Test
  public void getInstallationsList() throws Exception {
    final InstallationList installations =
        json.fromJson(
            loadResource(FIXTURES_PATH + "installations-list.json"), InstallationList.class);

    assertThat(installations.totalCount(), is(2));
    assertThat(installations.installations().get(0).account().login(), is("github"));
    assertThat(installations.installations().get(0).id(), is(1));
    assertThat(installations.installations().get(1).account().login(), is("octocat"));
    assertThat(installations.installations().get(1).id(), is(3));
  }

  @Test
  public void canDeserializeToken() throws IOException {
    final AccessToken accessToken =
        json.fromJson(loadResource(FIXTURES_PATH + "access-token.json"), AccessToken.class);
    assertThat(accessToken.token(), is("v1.1f699f1069f60xxx"));
    assertThat(accessToken.expiresAt(), is(ZonedDateTime.parse("2016-07-11T22:14:10Z")));
  }
}
