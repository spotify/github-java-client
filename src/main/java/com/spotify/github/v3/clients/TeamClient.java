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

import static com.spotify.github.v3.clients.GitHubClient.*;

import com.spotify.github.async.AsyncPage;
import com.spotify.github.v3.Team;
import com.spotify.github.v3.User;
import com.spotify.github.v3.orgs.Membership;
import com.spotify.github.v3.orgs.TeamInvitation;
import com.spotify.github.v3.orgs.requests.MembershipCreate;
import com.spotify.github.v3.orgs.requests.TeamCreate;
import com.spotify.github.v3.orgs.requests.TeamUpdate;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamClient {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TEAM_TEMPLATE = "/orgs/%s/teams";

  private static final String TEAM_SLUG_TEMPLATE = "/orgs/%s/teams/%s";

  private static final String MEMBERS_TEMPLATE = "/orgs/%s/teams/%s/members";

  private static final String PAGED_MEMBERS_TEMPLATE = "/orgs/%s/teams/%s/members?per_page=%d";

  private static final String MEMBERSHIP_TEMPLATE = "/orgs/%s/teams/%s/memberships/%s";

  private static final String INVITATIONS_TEMPLATE = "/orgs/%s/teams/%s/invitations";

  private final GitHubClient github;

  private final String org;

  TeamClient(final GitHubClient github, final String org) {
    this.github = github;
    this.org = org;
  }

  static TeamClient create(final GitHubClient github, final String org) {
    return new TeamClient(github, org);
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
  public CompletableFuture<Team> updateTeam(final TeamUpdate request, final String slug) {
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

  /**
   * Add or update a team membership for a user.
   *
   * @param request update membership request
   * @return membership
   */
  public CompletableFuture<Membership> updateMembership(final MembershipCreate request, final String slug, final String username) {
    final String path = String.format(MEMBERSHIP_TEMPLATE, org, slug, username);
    log.debug("Updating membership in: " + path);
    return github.put(path, github.json().toJsonUnchecked(request), Membership.class);
  }

  /**
   * Get a team membership of a user.
   *
   * @param slug the team slug
   * @param username username of the team member
   * @return membership
   */
  public CompletableFuture<Membership> getMembership(final String slug, final String username) {
    final String path = String.format(MEMBERSHIP_TEMPLATE, org, slug, username);
    log.debug("Fetching membership for: " + path);
    return github.request(path, Membership.class);
  }

  /**
   * List members of a specific team.
   *
   * @param slug the team slug
   * @return list of all users in a team
   */
  public CompletableFuture<List<User>> listTeamMembers(final String slug) {
    final String path = String.format(MEMBERS_TEMPLATE, org, slug);
    log.debug("Fetching members for: " + path);
    return github.request(path, LIST_TEAM_MEMBERS);
  }

  /**
   * List members of a specific team.
   *
   * @param slug the team slug
   * @param pageSize the number of users to fetch per page
   * @return list of all users in a team
   */
  public Iterator<AsyncPage<User>> listTeamMembers(final String slug, final int pageSize) {
    final String path = String.format(PAGED_MEMBERS_TEMPLATE, org, slug, pageSize);
    log.debug("Fetching members for: " + path);
    return new GithubPageIterator<>(new GithubPage<>(github, path, LIST_TEAM_MEMBERS));
  }

  /**
   * Delete a membership for a user.
   *
   * @param slug slug of the team name
   * @return membership
   */
  public CompletableFuture<Void> deleteMembership(final String slug, final String username) {
    final String path = String.format(MEMBERSHIP_TEMPLATE, org, slug, username);
    log.debug("Deleting membership from: " + path);
    return github.delete(path).thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * List pending invitations for a team.
   *
   * @param slug the team slug
   * @return list of pending invitations for a team
   */
  public CompletableFuture<List<TeamInvitation>> listPendingTeamInvitations(final String slug) {
    final String path = String.format(INVITATIONS_TEMPLATE, org, slug);
    log.debug("Fetching pending invitations for: " + path);
    return github.request(path, LIST_PENDING_TEAM_INVITATIONS);
  }
}
