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
import static com.spotify.github.v3.clients.GitHubClient.LIST_PENDING_TEAM_INVITATIONS;
import static com.spotify.github.v3.clients.GitHubClient.LIST_TEAMS;
import static com.spotify.github.v3.clients.GitHubClient.LIST_TEAM_MEMBERS;
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
import com.spotify.github.v3.User;
import com.spotify.github.v3.orgs.Membership;
import com.spotify.github.v3.orgs.TeamInvitation;
import com.spotify.github.v3.orgs.requests.MembershipCreate;
import com.spotify.github.v3.orgs.requests.TeamCreate;
import com.spotify.github.v3.orgs.requests.TeamUpdate;
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
public class TeamClientTest {

  private GitHubClient github;

  private TeamClient teamClient;

  private Json json;

  private static String getFixture(String resource) throws IOException {
    return Resources.toString(getResource(TeamClientTest.class, resource), defaultCharset());
  }

  @Before
  public void setUp() {
    github = mock(GitHubClient.class);
    teamClient = new TeamClient(github, "github");
    json = Json.create();
    when(github.json()).thenReturn(json);
  }

  @Test
  public void getTeam() throws Exception {
    final CompletableFuture<Team> fixture =
        completedFuture(json.fromJson(getFixture("team_get.json"), Team.class));
    when(github.request("/orgs/github/teams/justice-league", Team.class)).thenReturn(fixture);
    final Team team = teamClient.getTeam("justice-league").get();
    assertThat(team.id(), is(1));
    assertThat(team.name(), is("Justice League"));
  }

  @Test
  public void listTeams() throws Exception {
    final CompletableFuture<List<Team>> fixture =
        completedFuture(json.fromJson(getFixture("teams_list.json"), LIST_TEAMS));
    when(github.request("/orgs/github/teams", LIST_TEAMS)).thenReturn(fixture);
    final List<Team> teams = teamClient.listTeams().get();
    assertThat(teams.get(0).slug(), is("justice-league"));
    assertThat(teams.get(1).slug(), is("x-men"));
    assertThat(teams.size(), is(2));
  }

  @Test
  public void deleteTeam() throws Exception {
    final CompletableFuture<Response> response = completedFuture(mock(Response.class));
    final ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
    when(github.delete(capture.capture())).thenReturn(response);

    CompletableFuture<Void> deleteResponse = teamClient.deleteTeam("justice-league");
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
    final CompletableFuture<Team> actualResponse = teamClient.createTeam(teamCreateRequest);

    assertThat(actualResponse.get().name(), is("Justice League"));
  }

  @Test
  public void updateTeam() throws Exception {
    final TeamUpdate teamUpdateRequest =
        json.fromJson(
            getFixture("teams_patch.json"),
            TeamUpdate.class);

    final CompletableFuture<Team> fixtureResponse = completedFuture(json.fromJson(
        getFixture("teams_patch_response.json"),
        Team.class));
    when(github.patch(any(), any(), eq(Team.class))).thenReturn(fixtureResponse);
    final CompletableFuture<Team> actualResponse = teamClient.updateTeam(teamUpdateRequest, "justice-league");

    assertThat(actualResponse.get().name(), is("Justice League2"));
  }

  @Test
  public void getMembership() throws Exception {
    final CompletableFuture<Membership> fixture =
        completedFuture(json.fromJson(getFixture("membership.json"), Membership.class));
    when(github.request("/orgs/github/teams/1/memberships/octocat", Membership.class)).thenReturn(fixture);
    final Membership membership = teamClient.getMembership("1", "octocat").get();
    assertThat(membership.url().toString(), is("https://api.github.com/teams/1/memberships/octocat"));
    assertThat(membership.role(), is("maintainer"));
    assertThat(membership.state(), is("active"));
  }

  @Test
  public void listTeamMembers() throws Exception {
    final CompletableFuture<List<User>> fixture =
        completedFuture(json.fromJson(getFixture("list_members.json"), LIST_TEAM_MEMBERS));
    when(github.request("/orgs/github/teams/1/members", LIST_TEAM_MEMBERS)).thenReturn(fixture);
    final List<User> teamMembers = teamClient.listTeamMembers("1").get();
    assertThat(teamMembers.get(0).login(), is("octocat"));
    assertThat(teamMembers.get(1).id(), is(2));
    assertThat(teamMembers.size(), is(2));
  }

  @Test
  public void updateMembership() throws Exception {
    final MembershipCreate membershipCreateRequest =
        json.fromJson(
            getFixture("membership_update.json"),
            MembershipCreate.class);

    final CompletableFuture<Membership> fixtureResponse = completedFuture(json.fromJson(
        getFixture("membership_update_response.json"),
        Membership.class));
    when(github.put(any(), any(), eq(Membership.class))).thenReturn(fixtureResponse);
    final CompletableFuture<Membership> actualResponse = teamClient.updateMembership(membershipCreateRequest, "1", "octocat");

    assertThat(actualResponse.get().role(), is("member"));
  }

  @Test
  public void deleteMembership() throws Exception {
    final CompletableFuture<Response> response = completedFuture(mock(Response.class));
    final ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
    when(github.delete(capture.capture())).thenReturn(response);

    CompletableFuture<Void> deleteResponse = teamClient.deleteMembership("1", "octocat");
    deleteResponse.get();
    assertThat(capture.getValue(), is("/orgs/github/teams/1/memberships/octocat"));
  }

  @Test
  public void listPendingTeamInvitations() throws Exception {
    final CompletableFuture<List<TeamInvitation>> fixture =
        completedFuture(json.fromJson(getFixture("list_team_invitations.json"), LIST_PENDING_TEAM_INVITATIONS));
    when(github.request("/orgs/github/teams/1/invitations", LIST_PENDING_TEAM_INVITATIONS)).thenReturn(fixture);
    final List<TeamInvitation> pendingInvitations = teamClient.listPendingTeamInvitations("1").get();
    assertThat(pendingInvitations.get(0).login(), is("octocat"));
    assertThat(pendingInvitations.get(1).id(), is(2));
    assertThat(pendingInvitations.size(), is(2));
  }
}
