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

import static com.spotify.github.v3.checks.CheckRunConclusion.neutral;
import static com.spotify.github.v3.checks.CheckRunStatus.completed;
import static com.spotify.github.v3.checks.CheckRunStatus.in_progress;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.checks.CheckRunOutput;
import com.spotify.github.v3.checks.CheckRunRequest;
import com.spotify.github.v3.checks.CheckRunResponse;
import com.spotify.github.v3.checks.CheckRunResponseList;
import com.spotify.github.v3.checks.ImmutableCheckRunOutput;
import com.spotify.github.v3.checks.ImmutableCheckRunRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;

public class ChecksClientTest {

  private static final String FIXTURES_PATH = "com/spotify/github/v3/checks/";
  private GitHubClient github;
  private ChecksClient checksClient;
  private Json json;

  public static String loadResource(final String path) {
    try {
      return Resources.toString(Resources.getResource(path), UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Before
  public void setUp() {
    github = mock(GitHubClient.class);
    checksClient = new ChecksClient(github, "someowner", "somerepo");
    json = Json.create();
    when(github.json()).thenReturn(json);
  }

  @Test
  public void createCompletedCheckRun() throws Exception {
    final CheckRunRequest checkRunRequest =
        json.fromJson(
            loadResource(FIXTURES_PATH + "checks-run-completed-request.json"),
            CheckRunRequest.class);

    final CheckRunResponse checkRunResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "checks-run-completed-response.json"),
            CheckRunResponse.class);

    final CompletableFuture<CheckRunResponse> fixtureResponse = completedFuture(checkRunResponse);
    when(github.post(any(), any(), eq(CheckRunResponse.class), any())).thenReturn(fixtureResponse);

    final CompletableFuture<CheckRunResponse> actualResponse =
        checksClient.createCheckRun(checkRunRequest);

    assertThat(actualResponse.get().status(), is(completed));
    assertThat(actualResponse.get().headSha(), is("ce587453ced02b1526dfb4cb910479d431683101"));
    assertThat(actualResponse.get().output().annotationsCount().get(), is(2));
  }

  @Test
  public void createInProgressCheckRun() throws Exception {
    final CheckRunRequest checkRunRequest =
        json.fromJson(
            loadResource(FIXTURES_PATH + "checks-run-in-progress-request.json"),
            CheckRunRequest.class);

    final CheckRunResponse checkRunResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "checks-run-in-progress-response.json"),
            CheckRunResponse.class);

    final CompletableFuture<CheckRunResponse> fixtureResponse = completedFuture(checkRunResponse);
    when(github.post(any(), any(), eq(CheckRunResponse.class), any())).thenReturn(fixtureResponse);

    final CompletableFuture<CheckRunResponse> actualResponse =
        checksClient.createCheckRun(checkRunRequest);

    assertThat(actualResponse.get().status(), is(in_progress));
    assertThat(actualResponse.get().headSha(), is("ce587453ced02b1526dfb4cb910479d431683101"));
    assertThat(actualResponse.get().output().annotationsCount().isPresent(), is(false));
  }

  @Test
  public void getCompletedCheckRun() throws Exception {
    final CheckRunResponse checkRunResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "checks-run-completed-response.json"),
            CheckRunResponse.class);

    final CompletableFuture<CheckRunResponse> fixtureResponse = completedFuture(checkRunResponse);
    when(github.request(any(), eq(CheckRunResponse.class), any())).thenReturn(fixtureResponse);

    final CompletableFuture<CheckRunResponse> actualResponse = checksClient.getCheckRun(4);

    assertThat(actualResponse.get().status(), is(completed));
    assertThat(actualResponse.get().id(), is(4));
    assertThat(actualResponse.get().headSha(), is("ce587453ced02b1526dfb4cb910479d431683101"));
    assertThat(actualResponse.get().output().annotationsCount().get(), is(2));
  }

  @Test
  public void getCheckRunsList() throws Exception {
    final CheckRunResponseList checkRunResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "checks-runs-list.json"), CheckRunResponseList.class);

    final CompletableFuture<CheckRunResponseList> fixtureResponse =
        completedFuture(checkRunResponse);
    when(github.request(any(), eq(CheckRunResponseList.class), any())).thenReturn(fixtureResponse);

    final CompletableFuture<CheckRunResponseList> actualResponse =
        checksClient.getCheckRuns("some-sha");

    assertThat(actualResponse.get().totalCount(), is(1));
    assertThat(actualResponse.get().checkRuns().get(0).name(), is("mighty_readme"));
  }

  @Test
  public void ensureOmitsOptionalFieldsWhenSerializing() throws Exception {
    final CheckRunOutput out =
        ImmutableCheckRunOutput.builder().text("Text").title("Title").summary("summary").build();
    final CheckRunRequest req =
        ImmutableCheckRunRequest.builder()
            .name("bla")
            .headSha("6c9a91c6067bc5251266e77d5a9461f584b019c9")
            .status(completed)
            .conclusion(neutral)
            .output(out)
            .build();
    final String result = json.toJson(req);
    assertThat(result.contains("external"), is(false));
    assertThat(result.contains("started_at"), is(false));
    assertThat(result.contains("details_url"), is(false));
    assertThat(result.contains("annotations_count"), is(false));
  }
}
