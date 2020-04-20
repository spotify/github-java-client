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

package com.spotify.github.v3.prs;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class PullRequestTest {

  private String fixture;

  @Before
  public void setUp() throws Exception {
    fixture =
        Resources.toString(getResource(this.getClass(), "pull_request.json"), defaultCharset());
  }

  @Test
  public void testDeserialization() throws IOException {
    final PullRequest pr = Json.create().fromJson(fixture, PullRequest.class);
    assertThat(pr.mergeCommitSha().get(), is("e5bd3914e2e596debea16f433f57875b5b90bcd6"));
    assertThat(pr.merged(), is(false));
    assertThat(pr.mergeable().get(), is(true));
    assertThat(pr.comments(), is(10));
    assertThat(pr.additions(), is(100));
    assertThat(pr.deletions(), is(3));
    assertThat(pr.changedFiles(), is(5));
  }
}
