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

public class OrganisationClient {

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
}
