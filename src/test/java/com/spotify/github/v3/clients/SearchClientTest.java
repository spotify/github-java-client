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
import static com.spotify.github.v3.clients.SearchClient.ISSUES_URI;
import static com.spotify.github.v3.search.SearchTest.assertSearchIssues;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.search.SearchIssues;
import com.spotify.github.v3.search.SearchTest;
import com.spotify.github.v3.search.requests.ImmutableSearchParameters;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;

public class SearchClientTest {

  private GitHubClient github;
  private SearchClient searchClient;
  private Json json;

  private static String getFixture(String resource) throws IOException {
    return Resources.toString(getResource(SearchTest.class, resource), defaultCharset());
  }

  @Before
  public void setUp() {
    github = mock(GitHubClient.class);
    searchClient = SearchClient.create(github);
    json = Json.create();
  }

  @Test
  public void testSearchIssue() throws Exception {
    final CompletableFuture<SearchIssues> fixture =
        completedFuture(json.fromJson(getFixture("issues.json"), SearchIssues.class));

    when(github.request(ISSUES_URI + "?q=bogus-q", SearchIssues.class)).thenReturn(fixture);
    final SearchIssues search =
        searchClient.issues(ImmutableSearchParameters.builder().q("bogus-q").build()).get();
    assertSearchIssues(search);
  }
}
