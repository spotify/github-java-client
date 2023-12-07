/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2016 - 2023 Spotify AB
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

import com.spotify.github.v3.git.Reference;
import com.spotify.github.v3.git.Tag;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GitDataClientIT {

  private static final URI URL_TO_MOCK = URI.create("http://localhost:8081");

  private static GitHubClient github;
  private static GitDataClient gitDataClient;

  @BeforeAll
  static void beforeAll() {
    // URL points to mock which is set up with Skyramp
    // Mock config can be found in dir `skyramp/`
    github = GitHubClient.create(URL_TO_MOCK, "TOKEN");
    gitDataClient = GitDataClient.create(github, "someowner", "somerepo");
  }

  @Test
  public void getTagRef() throws Exception {
    final Reference reference = gitDataClient.getTagReference("0.0.1").get();
    assertThat(reference.object().sha(), is("5926dd300de5fee31d445c57be223f00e128a634"));
  }

  @Test
  public void getTag() throws Exception {
    final Tag tag = gitDataClient.getTag("27210625b551200e7d3dc608935b1454523eaa8").get();
    assertThat(tag.object().sha(), is("ee959eb71f7041260dc864fb24574eec4caa8019"));
    assertThat(tag.object().type(), is("commit"));
  }

  @Test
  public void listMatchingReferences() throws Exception {
    final List<Reference> matchingReferences =
        gitDataClient.listMatchingReferences("heads/feature").get();
    assertThat(matchingReferences.size(), is(2));
    for (Reference ref : matchingReferences) {
      assertThat(ref.ref(), containsString("heads/feature"));
    }
  }
}
