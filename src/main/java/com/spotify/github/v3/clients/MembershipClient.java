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

import com.spotify.github.v3.orgs.Membership;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MembershipClient {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String MEMBERS_TEMPLATE = "/orgs/%s/teams/%s/members";

  private static final String MEMBERSHIP_TEMPLATE = "/orgs/%s/teams/%s/memberships/%s";

  private static final String INVITATIONS_TEMPLATE = "/orgs/%s/teams/%s/invitations";

  private final GitHubClient github;

  private final String org;

  private final String team;

  MembershipClient(final GitHubClient github, final String org, final String team) {
    this.github = github;
    this.org = org;
    this.team = team;
  }

  static MembershipClient create(final GitHubClient github, final String org, final String team) {
    return new MembershipClient(github, org, team);
  }

//  /**
//   * Update a users membership within a team.
//   *
//   * @param request update membership request
//   * @return membership
//   */
//  public CompletableFuture<Member> createMember(final MemberCreate request) {
//    final String path = String.format(TEAM_TEMPLATE, org);
//    log.debug("Creating team in: " + path);
//    return github.post(path, github.json().toJsonUnchecked(request), Team.class);
//  }

  /**
   * Get team membership of a user.
   *
   * @param username username of the team member
   * @return membership
   */
  public CompletableFuture<Membership> getMembership(final String username) {
    final String path = String.format(MEMBERSHIP_TEMPLATE, org, team, username);
    log.debug("Fetching membership for: " + path);
    return github.request(path, Membership.class);
  }
}
