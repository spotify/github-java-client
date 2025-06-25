/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2016 - 2025 Spotify AB
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

import static org.hamcrest.core.Is.is;

import java.net.URI;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;

public class GitHubPageTest {
  private static final String MOCK_GITHUB_HOST = "bogus.host";
  private static final URI MOCK_GITHUB_URI = URI.create(String.format("http://%s/api/v3/", MOCK_GITHUB_HOST));

  @Test
  public void testFormatPathWithPerPage() {
    assertThat(GithubPage.formatPath("/commits?page=2", 3), is("/commits?page=2&per_page=3"));
    assertThat(GithubPage.formatPath("NOT_A_CORRECT PATH ", 3), is("NOT_A_CORRECT PATH "));
    assertThat(GithubPage.formatPath("/commits", 3), is("/commits?per_page=3"));
    assertThat(GithubPage.formatPath("/commits?page=2&per_page=7", 3), is("/commits?page=2&per_page=7"));
  }

  @Test
  public void testPageNumberFromURI() {
    assertThat(GithubPage.pageNumberFromUri(MOCK_GITHUB_URI.resolve("/commits?page=5").toString()), isPresentAndIs(5));
    assertThat(GithubPage.pageNumberFromUri(MOCK_GITHUB_URI.resolve("/commits").toString()), isEmpty());
    assertThat(GithubPage.pageNumberFromUri(MOCK_GITHUB_URI.resolve("commits").toString()), isEmpty());
    assertThat(GithubPage.pageNumberFromUri("/commits?page=2"), isPresentAndIs(2));
    assertThat(GithubPage.pageNumberFromUri("/commits?per_page=4&page=2"), isPresentAndIs(2));
    assertThat(GithubPage.pageNumberFromUri("NOT_A_CORRECT PATH "), isEmpty());
    assertThat(GithubPage.pageNumberFromUri("/commits"), isEmpty());
  }
}
