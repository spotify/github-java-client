package com.spotify.github.v3.clients;

import static com.spotify.github.v3.clients.GitHubClient.IGNORE_RESPONSE_CONSUMER;
import static com.spotify.github.v3.clients.GitHubClient.LIST_TEAMS;
import static com.spotify.github.v3.clients.GitHubClient.LIST_TEAM_MEMBERS;

import com.spotify.github.v3.Team;
import com.spotify.github.v3.User;
import com.spotify.github.v3.orgs.Membership;
import com.spotify.github.v3.orgs.requests.TeamCreate;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Member;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamClient {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TEAM_TEMPLATE = "/orgs/%s/teams";

  private static final String TEAM_SLUG_TEMPLATE = "/orgs/%s/teams/%s";

  private static final String MEMBERS_TEMPLATE = "/orgs/%s/teams/%s/members";

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

  /**
   * Get team membership of a user.
   *
   * @param slug the team slug
   * @param username username of the team member
   * @return membership
   */
  public CompletableFuture<Membership> getTeamMembership(final String slug, final String username) {
    final String path = String.format(MEMBERSHIP_TEMPLATE, org, slug, username);
    log.debug("Fetching membership for: " + path);
    return github.request(path, Membership.class);
  }

  /**
   * List team members.
   *
   * @param slug the team slug
   * @return list of all members in a team
   */
  public CompletableFuture<List<User>> listTeamMembers(final String slug) {
    final String path = String.format(MEMBERS_TEMPLATE, org, slug);
    log.debug("Fetching members for: " + path);
    return github.request(path, LIST_TEAM_MEMBERS);
  }
}
