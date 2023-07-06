///*-
// * -\-\-
// * github-api
// * --
// * Copyright (C) 2016 - 2020 Spotify AB
// * --
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * -/-/-
// */
//
package com.spotify.github.v3.clients;

import static com.spotify.github.v3.clients.GitHubClient.IGNORE_RESPONSE_CONSUMER;
import static com.spotify.github.v3.clients.GitHubClient.LIST_TEAMS;

import com.spotify.github.v3.Team;
import com.spotify.github.v3.orgs.requests.TeamCreate;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganisationClient {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TEAM_TEMPLATE = "/orgs/%s/teams";

  private static final String TEAM_SLUG_TEMPLATE = "/orgs/%s/teams/%s";

  private final GitHubClient github;

  private final String org;

  OrganisationClient(final GitHubClient github, final String org) {
    this.github = github;
    this.org = org;
  }

  static OrganisationClient create(final GitHubClient github, final String org) {
    return new OrganisationClient(github, org);
  }

  /**
   * Create a team in an organisation.
   *
   * @param request create team request
   * @return team
   */
  public CompletableFuture<Team> createTeam(final TeamCreate request) {
    final String path = String.format(TEAM_TEMPLATE, org);
    log.debug("Creating team in: " + path);
    return github.post(path, github.json().toJsonUnchecked(request), Team.class);
  }

  /**
   * Get a specific team in an organisation.
   *
   * @param slug slug of the team name
   * @return team
   */
  public CompletableFuture<Team> getTeam(final String slug) {
    final String path = String.format(TEAM_SLUG_TEMPLATE, org, slug);
    log.debug("Fetching team from " + path);
    return github.request(path, Team.class);
  }

  /**
   * List teams within an organisation.
   *
   * @return list of all teams in an organisation
   */
  public CompletableFuture<List<Team>> listTeams() {
    final String path = String.format(TEAM_TEMPLATE, org);
    log.debug("Fetching teams from " + path);
    return github.request(path, LIST_TEAMS);
  }

  /**
   * Update a team in an organisation.
   *
   * @param request update team request
   * @param slug slug of the team name
   * @return team
   */
  public CompletableFuture<Team> updateTeam(final TeamCreate request, final String slug) {
    final String path = String.format(TEAM_SLUG_TEMPLATE, org, slug);
    log.debug("Updating team in: " + path);
    return github.patch(path, github.json().toJsonUnchecked(request), Team.class);
  }

  /**
   * Delete a specific team in an organisation.
   *
   * @param slug slug of the team name
   * @return team
   */
  public CompletableFuture<Void> deleteTeam(final String slug) {
    final String path = String.format(TEAM_SLUG_TEMPLATE, org, slug);
    log.debug("Deleting team from: " + path);
    return github.delete(path).thenAccept(IGNORE_RESPONSE_CONSUMER);
  }
}
