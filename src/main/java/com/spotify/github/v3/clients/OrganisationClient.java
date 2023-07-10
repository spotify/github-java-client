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

import java.lang.invoke.MethodHandles;
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
   * Create a membership API client.
   *
   * @return membership API client
   */
  public MembershipClient createMembershipClient(final GitHubClient github, final String org, final String team) {
    return MembershipClient.create(github, org, team);
  }

  /**
   * Create a Teams API client.
   *
   * @return Teams API client
   */
  public TeamClient createTeamClient(final GitHubClient github, final String org) {
    return TeamClient.create(github, org);
  }
}
