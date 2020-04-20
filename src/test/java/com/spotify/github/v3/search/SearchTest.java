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

package com.spotify.github.v3.search;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.issues.Issue;
import java.io.IOException;
import java.net.URI;
import org.junit.Test;

public class SearchTest {

  public static final void assertSearchIssues(final SearchIssues search) {
    final Issue issues = search.items().get(0);
    assertThat(search.totalCount(), is(280));
    assertThat(search.incompleteResults(), is(false));
    assertThat(
        issues.url(),
        is(URI.create("https://api.github.com/repos/batterseapower/pinyin-toolkit/issues/132")));
    assertThat(issues.number(), is(132));
    assertThat(issues.id(), is(35802));
    assertThat(issues.title(), is("Line Number Indexes Beyond 20 Not Displayed"));
  }

  @Test
  public void testDeserialization() throws IOException {
    final String fixture =
        Resources.toString(getResource(this.getClass(), "issues.json"), defaultCharset());

    final SearchIssues search = Json.create().fromJson(fixture, SearchIssues.class);
    assertSearchIssues(search);
  }
}
