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

import static com.google.common.io.Resources.getResource;
import static com.spotify.github.v3.clients.GitHubClient.LIST_TEAMS;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.Team;
import com.spotify.github.v3.orgs.requests.TeamCreate;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Headers.class, ResponseBody.class, Response.class})
public class OrganisationClientTest {

  private GitHubClient github;

  private OrganisationClient organisationClient;

  private Json json;

  private static String getFixture(String resource) throws IOException {
    return Resources.toString(getResource(OrganisationClientTest.class, resource), defaultCharset());
  }

  @Before
  public void setUp() {
    github = mock(GitHubClient.class);
    organisationClient = new OrganisationClient(github, "github");
    json = Json.create();
    when(github.json()).thenReturn(json);
  }

  @Test
  public void getTeam() throws Exception {
    final CompletableFuture<Team> fixture =
        completedFuture(json.fromJson(getFixture("team_get.json"), Team.class));
    when(github.request("/orgs/github/teams/justice-league", Team.class)).thenReturn(fixture);
    final Team team = organisationClient.getTeam("justice-league").get();
    assertThat(team.id(), is(1));
    assertThat(team.name(), is("Justice League"));
  }

  @Test
  public void listTeams() throws Exception {
    final CompletableFuture<List<Team>> fixture =
        completedFuture(json.fromJson(getFixture("teams_list.json"), LIST_TEAMS));
    when(github.request("/orgs/github/teams", LIST_TEAMS)).thenReturn(fixture);
    final List<Team> teams = organisationClient.listTeams().get();
    assertThat(teams.get(0).slug(), is("justice-league"));
    assertThat(teams.get(1).slug(), is("x-men"));
    assertThat(teams.size(), is(2));
  }

  @Test
  public void deleteTeam() throws Exception {
    final CompletableFuture<Response> response = completedFuture(mock(Response.class));
    final ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
    when(github.delete(capture.capture())).thenReturn(response);

    CompletableFuture<Void> deleteResponse = organisationClient.deleteTeam("justice-league");
    deleteResponse.get();
    assertThat(capture.getValue(), is("/orgs/github/teams/justice-league"));
  }

  @Test
  public void createTeam() throws Exception {
    final TeamCreate teamCreateRequest =
        json.fromJson(
            getFixture("teams_request.json"),
            TeamCreate.class);

    final CompletableFuture<Team> fixtureResponse = completedFuture(json.fromJson(
        getFixture("team_get.json"),
        Team.class));
    when(github.post(any(), any(), eq(Team.class))).thenReturn(fixtureResponse);
    final CompletableFuture<Team> actualResponse = organisationClient.createTeam(teamCreateRequest);

    assertThat(actualResponse.get().name(), is("Justice League"));
  }

  @Test
  public void updateTeam() throws Exception {
    final TeamCreate teamCreateRequest =
        json.fromJson(
            getFixture("teams_patch.json"),
            TeamCreate.class);

    final CompletableFuture<Team> fixtureResponse = completedFuture(json.fromJson(
        getFixture("teams_patch_response.json"),
        Team.class));
    when(github.patch(any(), any(), eq(Team.class))).thenReturn(fixtureResponse);
    final CompletableFuture<Team> actualResponse = organisationClient.updateTeam(teamCreateRequest, "justice-league");

    assertThat(actualResponse.get().name(), is("Justice League2"));
  }
}
