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

import com.google.common.base.Strings;
import com.spotify.github.v3.search.SearchIssues;
import com.spotify.github.v3.search.SearchRepositories;
import com.spotify.github.v3.search.SearchUsers;
import com.spotify.github.v3.search.requests.SearchParameters;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Search API client */
public class SearchClient {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String USERS_URI = "/search/users";
  static final String REPOSITORIES_URI = "/search/repositories";
  static final String ISSUES_URI = "/search/issues";
  private final GitHubClient github;

  SearchClient(final GitHubClient github) {
    this.github = github;
  }

  static SearchClient create(final GitHubClient github) {
    return new SearchClient(github);
  }

  /**
   * Search users.
   *
   * @param parameters user search parameters
   * @return user search results
   */
  public CompletableFuture<SearchUsers> users(final SearchParameters parameters) {
    return search(USERS_URI, parameters, SearchUsers.class);
  }

  /**
   * Search issues.
   *
   * @param parameters issue search parameters
   * @return issue search results
   */
  public CompletableFuture<SearchIssues> issues(final SearchParameters parameters) {
    return search(ISSUES_URI, parameters, SearchIssues.class);
  }

  /**
   * Search repositories.
   *
   * @param parameters repository search parameters
   * @return repository search results
   */
  public CompletableFuture<SearchRepositories> repositories(final SearchParameters parameters) {
    return search(REPOSITORIES_URI, parameters, SearchRepositories.class);
  }

  private <T> CompletableFuture<T> search(
      final String baseUrl, final SearchParameters parameters, final Class<T> clazz) {
    final String serial = parameters.serialize();
    final String path = baseUrl + (Strings.isNullOrEmpty(serial) ? "" : "?" + serial);
    log.debug("Fetching search result for:" + path);
    return github.request(path, clazz);
  }
}
