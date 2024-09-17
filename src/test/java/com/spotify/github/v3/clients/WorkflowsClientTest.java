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

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.workflows.WorkflowsRepositoryResponseList;
import com.spotify.github.v3.workflows.WorkflowsResponse;
import com.spotify.github.v3.workflows.WorkflowsState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkflowsClientTest {

  private static final String FIXTURES_PATH = "com/spotify/github/v3/workflows/";
  private GitHubClient github;
  private WorkflowsClient workflowsClient;
  private Json json;

  public static String loadResource(final String path) {
    try {
      return Resources.toString(Resources.getResource(path), UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @BeforeEach
  public void setUp() {
    github = mock(GitHubClient.class);
    workflowsClient = new WorkflowsClient(github, "someowner", "somerepo");
    json = Json.create();
    when(github.json()).thenReturn(json);
  }

  @Test
  public void getWorkflow() throws Exception {
    final WorkflowsResponse workflowsResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "workflows-get-workflow-response.json"), WorkflowsResponse.class);
    final CompletableFuture<WorkflowsResponse> fixtureResponse = completedFuture(workflowsResponse);
    when(github.request(any(), eq(WorkflowsResponse.class), any())).thenReturn(fixtureResponse);

    final CompletableFuture<WorkflowsResponse> actualResponse =
        workflowsClient.getWorkflow(161335);

    assertThat(actualResponse.get().id(), is(161335));
    assertThat(actualResponse.get().state(), is(WorkflowsState.active));
  }

  @Test
  public void listWorkflows() throws Exception {
    final WorkflowsRepositoryResponseList workflowsListResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "workflows-list-workflows-response.json"), WorkflowsRepositoryResponseList.class);
    final CompletableFuture<WorkflowsRepositoryResponseList> fixtureResponse = completedFuture(workflowsListResponse);
    when(github.request(any(), eq(WorkflowsRepositoryResponseList.class), any())).thenReturn(fixtureResponse);

    final CompletableFuture<WorkflowsRepositoryResponseList> actualResponse =
        workflowsClient.listWorkflows();

    assertThat(actualResponse.get().totalCount(), is(2));
    assertThat(actualResponse.get().workflows().get(0).name(), is("CI"));
    assertThat(actualResponse.get().workflows().get(1).name(), is("Linter"));
  }
}