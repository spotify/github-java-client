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
import com.spotify.github.v3.checks.Installation;
import com.spotify.github.v3.orgs.OrgMembership;
import com.spotify.github.v3.orgs.requests.OrgMembershipCreate;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrganisationClientTest {

  private GitHubClient github;

  private OrganisationClient organisationClient;

  private Json json;

  private static String getFixture(String resource) throws IOException {
    return Resources.toString(getResource(TeamClientTest.class, resource), defaultCharset());
  }

  @BeforeEach
  public void setUp() {
    github = mock(GitHubClient.class);
    organisationClient = new OrganisationClient(github, "github");
    json = Json.create();
    when(github.json()).thenReturn(json);
  }

  @Test
  public void testTeamClient() throws Exception {
    final TeamClient teamClient = organisationClient.createTeamClient();
    final CompletableFuture<Team> fixture =
        completedFuture(json.fromJson(getFixture("team_get.json"), Team.class));
    when(github.request("/orgs/github/teams/justice-league", Team.class)).thenReturn(fixture);
    final Team team = teamClient.getTeam("justice-league").get();
    assertThat(team.id(), is(1));
    assertThat(team.name(), is("Justice League"));
  }

  @Test
  public void testAppClient() throws Exception {
    final GithubAppClient githubAppClient = organisationClient.createGithubAppClient();
    final CompletableFuture<Installation> fixture =
        completedFuture(json.fromJson(getFixture("../githubapp/installation.json"), Installation.class));
    when(github.request("/orgs/github/installation", Installation.class)).thenReturn(fixture);
    final Installation installation = githubAppClient.getInstallation().get();
    assertThat(installation.id(), is(1));
    assertThat(installation.account().login(), is("github"));
  }

  @Test
  public void getOrgMembership() throws Exception {
    final CompletableFuture<OrgMembership> fixture =
        completedFuture(json.fromJson(getFixture("org_membership.json"), OrgMembership.class));
    when(github.request("/orgs/github/memberships/octocat", OrgMembership.class))
        .thenReturn(fixture);
    final OrgMembership orgMembership = organisationClient.getOrgMembership("octocat").get();
    assertThat(
        orgMembership.url().toString(), is("https://api.github.com/orgs/github/memberships/octocat"));
    assertThat(orgMembership.role(), is("member"));
    assertThat(orgMembership.state(), is("active"));
  }

  @Test
  public void updateMembership() throws Exception {
    final OrgMembershipCreate orgMembershipCreateRequest =
        json.fromJson(getFixture("membership_update.json"), OrgMembershipCreate.class);

    final CompletableFuture<OrgMembership> fixtureResponse =
        completedFuture(
            json.fromJson(getFixture("org_membership.json"), OrgMembership.class));
    when(github.put(any(), any(), eq(OrgMembership.class))).thenReturn(fixtureResponse);
    final CompletableFuture<OrgMembership> actualResponse =
        organisationClient.updateOrgMembership(orgMembershipCreateRequest, "octocat");

    assertThat(actualResponse.get().role(), is("member"));
    assertThat(actualResponse.get().organization().login(), is("github"));
    assertThat(actualResponse.get().user().login(), is("octocat"));
  }
}
