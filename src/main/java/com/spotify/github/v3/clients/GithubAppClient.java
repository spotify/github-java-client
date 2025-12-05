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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.spotify.github.v3.apps.InstallationRepositoriesResponse;
import com.spotify.github.v3.apps.requests.AccessTokenRequest;
import com.spotify.github.v3.checks.AccessToken;
import com.spotify.github.v3.checks.App;
import com.spotify.github.v3.checks.Installation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.core.HttpHeaders;

/** Apps API client */
public class GithubAppClient {

  private static final String GET_AUTHENTICATED_APP_URL = "/app";
  private static final String GET_INSTALLATION_BY_ID_URL = "/app/installations/%s";
  private static final String GET_ACCESS_TOKEN_URL = "/app/installations/%s/access_tokens";
  private static final String GET_INSTALLATIONS_URL = "/app/installations?per_page=100";
  private static final String GET_INSTALLATION_REPO_URL = "/repos/%s/%s/installation";
  private static final String LIST_ACCESSIBLE_REPOS_URL = "/installation/repositories";

  /*
    Owner and org are interchangeable and therefore "owner" is used to
    refer to the organisation in the installation endpoint
  */
  private static final String GET_INSTALLATION_ORG_URL = "/orgs/%s/installation";
  private static final String GET_INSTALLATION_USER_URL = "/users/%s/installation";

  private final GitHubClient github;
  private final Optional<String> maybeOwner;
  private final Optional<String> maybeRepo;

  private final Map<String, String> extraHeaders =
      ImmutableMap.of(HttpHeaders.ACCEPT, "application/vnd.github.machine-man-preview+json");

  private static final TypeReference<List<Installation>> INSTALLATION_LIST_TYPE_REFERENCE =
      new TypeReference<>() {};

  GithubAppClient(final GitHubClient github, final String owner, final String repo) {
    this.github = github;
    this.maybeOwner = Optional.of(owner);
    this.maybeRepo = Optional.of(repo);
  }

  GithubAppClient(final GitHubClient github, final String owner) {
    this.github = github;
    this.maybeOwner = Optional.of(owner);
    this.maybeRepo = Optional.empty();
  }

  GithubAppClient(final GitHubClient github) {
    this.github = github;
    this.maybeOwner = Optional.empty();
    this.maybeRepo = Optional.empty();
  }

  /**
   * Gets the owner, throwing a descriptive exception if not present.
   *
   * @return the owner string
   * @throws IllegalStateException if owner is not present
   */
  private String requireOwner() {
    return maybeOwner.orElseThrow(
        () ->
            new IllegalStateException(
                "This operation requires an owner context. "
                    + "Use GitHubClient.createOrganisationClient(owner).createGithubAppClient() "
                    + "or GitHubClient.createRepositoryClient(owner, repo).createGithubAppClient() "
                    + "instead of GitHubClient.createGithubAppClient()"));
  }

  /**
   * List Installations of an app.
   *
   * @return a list of Installation
   */
  public CompletableFuture<List<Installation>> getInstallations() {
    return github.request(GET_INSTALLATIONS_URL, INSTALLATION_LIST_TYPE_REFERENCE, extraHeaders);
  }

  /**
   * Get Installation of repo or org
   *
   * @return an Installation
   */
  public CompletableFuture<Installation> getInstallation() {
    return maybeRepo.map(this::getRepoInstallation).orElseGet(this::getOrgInstallation);
  }

  /**
   * Get Installation identified by its installation id
   *
   * @return an Installation
   */
  public CompletableFuture<Installation> getInstallation(final Integer installationId) {
    return github.request(
        String.format(GET_INSTALLATION_BY_ID_URL, installationId), Installation.class);
  }

  /**
   * Get an installation of a repo
   *
   * @return an Installation
   */
  private CompletableFuture<Installation> getRepoInstallation(final String repo) {
    return github.request(
        String.format(GET_INSTALLATION_REPO_URL, requireOwner(), repo), Installation.class);
  }

  /**
   * Get an installation of an org
   *
   * @return an Installation
   */
  private CompletableFuture<Installation> getOrgInstallation() {
    return github.request(
        String.format(GET_INSTALLATION_ORG_URL, requireOwner()), Installation.class);
  }

  /**
   * Get an installation of a user
   *
   * @return an Installation
   */
  public CompletableFuture<Installation> getUserInstallation() {
    return github.request(
        String.format(GET_INSTALLATION_USER_URL, requireOwner()), Installation.class);
  }

  /**
   * Authenticates as an installation
   *
   * @return an Installation Token
   * @see #getAccessToken(Integer, AccessTokenRequest) for repository-scoped tokens
   */
  public CompletableFuture<AccessToken> getAccessToken(final Integer installationId) {
    final String path = String.format(GET_ACCESS_TOKEN_URL, installationId);
    return github.post(path, "", AccessToken.class, extraHeaders);
  }

  /**
   * Authenticates as an installation with repository scoping.
   *
   * @param installationId the installation ID
   * @param request the access token request with optional repository scoping
   * @return an Installation Token
   * @see "https://docs.github.com/en/rest/apps/apps#create-an-installation-access-token-for-an-app"
   */
  public CompletableFuture<AccessToken> getAccessToken(
      final Integer installationId,
      final AccessTokenRequest request) {
    final String path = String.format(GET_ACCESS_TOKEN_URL, installationId);
    return github.post(path, github.json().toJsonUnchecked(request), AccessToken.class, extraHeaders);
  }

  /**
   * Lists the repositories that an app installation can access.
   *
   * <p>see
   * https://docs.github.com/en/free-pro-team@latest/rest/reference/apps#list-repositories-accessible-to-the-app-installation
   */
  public CompletableFuture<InstallationRepositoriesResponse> listAccessibleRepositories(
      final int installationId) {

    return GitHubClient.scopeForInstallationId(github, installationId)
        .request(LIST_ACCESSIBLE_REPOS_URL, InstallationRepositoriesResponse.class, extraHeaders);
  }

  /**
   * Get the authenticated GitHub App.
   *
   * <p>Returns the authenticated app. You must use a JWT to access this endpoint.
   *
   * <p>see https://docs.github.com/en/rest/apps/apps#get-the-authenticated-app
   *
   * @return the authenticated App
   */
  public CompletableFuture<App> getAuthenticatedApp() {
    return github.request(GET_AUTHENTICATED_APP_URL, App.class);
  }
}
