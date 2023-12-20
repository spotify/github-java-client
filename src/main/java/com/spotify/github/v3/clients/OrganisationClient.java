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

import com.spotify.github.v3.orgs.OrgMembership;
import com.spotify.github.v3.orgs.requests.OrgMembershipCreate;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganisationClient {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String MEMBERSHIP_TEMPLATE = "/orgs/%s/memberships/%s";

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
   * Create a Teams API client.
   *
   * @return Teams API client
   */
  public TeamClient createTeamClient() {
    return TeamClient.create(github, org);
  }

  /**
   * Create GitHub App API client
   *
   * @return GitHub App API client
   */
  public GithubAppClient createGithubAppClient() {
    return new GithubAppClient(github, org);
  }

  /**
   * Get an org membership of a user.
   *
   * @param username username of the org member
   * @return membership
   */
  public CompletableFuture<OrgMembership> getOrgMembership(final String username) {
    final String path = String.format(MEMBERSHIP_TEMPLATE, org, username);
    log.debug("Fetching org membership for: " + path);
    return github.request(path, OrgMembership.class);
  }

  /**
   * Add or update an org membership for a user.
   *
   * @param request update org membership request
   * @return membership
   */
  public CompletableFuture<OrgMembership> updateOrgMembership(final OrgMembershipCreate request, final String username) {
    final String path = String.format(MEMBERSHIP_TEMPLATE, org, username);
    log.debug("Updating membership in org: " + path);
    return github.put(path, github.json().toJsonUnchecked(request), OrgMembership.class);
  }
}
