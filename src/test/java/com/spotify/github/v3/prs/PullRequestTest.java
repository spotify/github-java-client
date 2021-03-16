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
import java.util.Optional;
import org.junit.Test;

public class PullRequestTest {

  @Test
  public void testDeserializationPr() throws IOException {
    String fixture =
        Resources.toString(getResource(this.getClass(), "pull_request.json"), defaultCharset());
    final PullRequest pr = Json.create().fromJson(fixture, PullRequest.class);
    assertThat(pr.mergeCommitSha().get(), is("e5bd3914e2e596debea16f433f57875b5b90bcd6"));
    assertThat(pr.merged(), is(false));
    assertThat(pr.mergeable().get(), is(true));
    assertThat(pr.comments(), is(10));
    assertThat(pr.additions(), is(100));
    assertThat(pr.deletions(), is(3));
    assertThat(pr.changedFiles(), is(5));
    assertThat(pr.draft(), is(Optional.of(false)));
  }

  @Test
  public void testSerializationMergeParams() throws IOException {
    String fixture =
        Resources.toString(getResource(this.getClass(), "merge_params_full.json"), defaultCharset());
    final MergeParameters fixtureParams = Json.create().fromJson(fixture, MergeParameters.class);

    final MergeParameters params = ImmutableMergeParameters.builder()
        .commitTitle("a title")
        .commitMessage("a message")
        .sha("6dcb09b5b57875f334f61aebed695e2e4193db5e")
        .build();
    assertThat(params.commitMessage(), is(fixtureParams.commitMessage()));
    assertThat(params.commitTitle(), is(fixtureParams.commitTitle()));
    assertThat(params.sha(), is(fixtureParams.sha()));
    assertThat(params.mergeMethod(), is(MergeMethod.merge));
  }

  @Test
  public void testDeserializationMergeParamsOmitsFields() throws IOException {
    final MergeParameters params = ImmutableMergeParameters.builder()
        .commitMessage("a message")
        .sha("6dcb09b5b57875f334f61aebed695e2e4193db5e")
        .build();
    final String json = Json.create().toJson(params);

    assertThat(
        json,
        is(
            "{\"sha\":\"6dcb09b5b57875f334f61aebed695e2e4193db5e\",\"commit_message\":\"a message\",\"merge_method\":\"merge\"}"));

    System.out.println(json);
  }
}
