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
import com.spotify.github.v3.actions.workflowruns.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkflowRunsClientTest {

  private static final String FIXTURES_PATH = "com/spotify/github/v3/actions/workflowruns/";
  private GitHubClient github;
  private WorkflowRunsClient workflowRunsClient;
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
    workflowRunsClient = new WorkflowRunsClient(github, "someowner", "somerepo");
    json = Json.create();
    when(github.json()).thenReturn(json);
  }

  @Test
  public void getWorkflowRun() throws Exception {
    final WorkflowRunResponse workflowRunResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "workflowruns-get-workflowrun-with-pull-requests-response.json"), WorkflowRunResponse.class);
    final CompletableFuture<WorkflowRunResponse> fixtureResponse = completedFuture(workflowRunResponse);
    when(github.request(any(), eq(WorkflowRunResponse.class), any())).thenReturn(fixtureResponse);

    final CompletableFuture<WorkflowRunResponse> actualResponse =
        workflowRunsClient.getWorkflowRun(30433642, null);

    assertThat(actualResponse.get().id(), is(30433642L));
    assertThat(actualResponse.get().status(), is(WorkflowRunStatus.queued));
    assertThat(actualResponse.get().workflowId(), is(159038));

    assertThat(actualResponse.get().pullRequests().size(), is(1));
    assertThat(actualResponse.get().pullRequests().get(0).id(), is(207696345));
  }

  @Test
  public void getWorkflowRunFiltered() throws Exception {
    ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);

    final WorkflowRunResponse workflowRunResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "workflowruns-get-workflowrun-response.json"), WorkflowRunResponse.class);
    final CompletableFuture<WorkflowRunResponse> fixtureResponse = completedFuture(workflowRunResponse);
    when(github.request(pathCaptor.capture(), eq(WorkflowRunResponse.class), any())).thenReturn(fixtureResponse);

    GetWorkflowRunQueryParams queryParams = ImmutableGetWorkflowRunQueryParams.builder()
        .excludePullRequests(true)
        .build();

    final CompletableFuture<WorkflowRunResponse> actualResponse =
        workflowRunsClient.getWorkflowRun(30433642L, null);

    assertThat(pathCaptor.getValue(), is("/repos/someowner/somerepo/actions/runs/30433642"));

    assertThat(actualResponse.get().id(), is(30433642L));
    assertThat(actualResponse.get().status(), is(WorkflowRunStatus.queued));
    assertThat(actualResponse.get().workflowId(), is(159038));

    assertThat(actualResponse.get().pullRequests().size(), is(0));
  }

  @Test
  public void listAllWorkflows() throws Exception {
    final WorkflowRunsResponseList workflowRunsListResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "workflowruns-list-workflowruns-response.json"), WorkflowRunsResponseList.class);
    final CompletableFuture<WorkflowRunsResponseList> fixtureResponse = completedFuture(workflowRunsListResponse);
    when(github.request(any(), eq(WorkflowRunsResponseList.class), any())).thenReturn(fixtureResponse);

    final CompletableFuture<WorkflowRunsResponseList> actualResponse =
        workflowRunsClient.listAllWorkflowRuns(null);

    assertThat(actualResponse.get().totalCount(), is(2));
    assertThat(actualResponse.get().workflowRuns().size(), is(2));

    assertThat(actualResponse.get().workflowRuns().get(0).name(), is("Build"));
    assertThat(actualResponse.get().workflowRuns().get(0).id(), is(30433643L));
    assertThat(actualResponse.get().workflowRuns().get(0).status(), is(WorkflowRunStatus.queued));
    assertThat(actualResponse.get().workflowRuns().get(0).createdAt(), is(ZonedDateTime.parse("2020-01-22T19:40:08Z")));
    assertThat(actualResponse.get().workflowRuns().get(0).displayTitle(), is("Update README.md again!!"));

    assertThat(actualResponse.get().workflowRuns().get(1).name(), is("Build"));
    assertThat(actualResponse.get().workflowRuns().get(1).id(), is(30433642L));
    assertThat(actualResponse.get().workflowRuns().get(1).status(), is(WorkflowRunStatus.completed));
    assertThat(actualResponse.get().workflowRuns().get(1).createdAt(), is(ZonedDateTime.parse("2020-01-22T19:33:08Z")));
    assertThat(actualResponse.get().workflowRuns().get(1).displayTitle(), is("Update README.md"));
  }

  @Test
  public void listAllWorkflowsFiltered() throws Exception {
    ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);

    final WorkflowRunsResponseList workflowRunsListResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "workflowruns-list-workflowruns-filtered-response.json"), WorkflowRunsResponseList.class);
    final CompletableFuture<WorkflowRunsResponseList> fixtureResponse = completedFuture(workflowRunsListResponse);
    when(github.request(pathCaptor.capture(), eq(WorkflowRunsResponseList.class), any())).thenReturn(fixtureResponse);

    ListWorkflowRunsQueryParams queryParams = ImmutableListWorkflowRunsQueryParams.builder()
        .status(WorkflowRunStatus.completed)
        .build();

    final CompletableFuture<WorkflowRunsResponseList> actualResponse =
        workflowRunsClient.listAllWorkflowRuns(queryParams);

    assertThat(pathCaptor.getValue(), is("/repos/someowner/somerepo/actions/runs?status=completed"));

    assertThat(actualResponse.get().totalCount(), is(1));
    assertThat(actualResponse.get().workflowRuns().size(), is(1));

    assertThat(actualResponse.get().workflowRuns().get(0).name(), is("Build"));
    assertThat(actualResponse.get().workflowRuns().get(0).id(), is(30433642L));
    assertThat(actualResponse.get().workflowRuns().get(0).status(), is(WorkflowRunStatus.completed));
    assertThat(actualResponse.get().workflowRuns().get(0).createdAt(), is(ZonedDateTime.parse("2020-01-22T19:33:08Z")));
    assertThat(actualResponse.get().workflowRuns().get(0).displayTitle(), is("Update README.md"));
  }

  @Test
  public void listWorkflowRuns() throws Exception {
    final WorkflowRunsResponseList workflowRunsListResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "workflowruns-list-workflowruns-response.json"), WorkflowRunsResponseList.class);
    final CompletableFuture<WorkflowRunsResponseList> fixtureResponse = completedFuture(workflowRunsListResponse);
    when(github.request(any(), eq(WorkflowRunsResponseList.class), any())).thenReturn(fixtureResponse);

    final CompletableFuture<WorkflowRunsResponseList> actualResponse =
        workflowRunsClient.listWorkflowRuns(159038, null);

    assertThat(actualResponse.get().totalCount(), is(2));
    assertThat(actualResponse.get().workflowRuns().size(), is(2));

    assertThat(actualResponse.get().workflowRuns().get(0).name(), is("Build"));
    assertThat(actualResponse.get().workflowRuns().get(0).id(), is(30433643L));
    assertThat(actualResponse.get().workflowRuns().get(0).workflowId(), is(159038));
    assertThat(actualResponse.get().workflowRuns().get(0).status(), is(WorkflowRunStatus.queued));
    assertThat(actualResponse.get().workflowRuns().get(0).createdAt(), is(ZonedDateTime.parse("2020-01-22T19:40:08Z")));
    assertThat(actualResponse.get().workflowRuns().get(0).displayTitle(), is("Update README.md again!!"));

    assertThat(actualResponse.get().workflowRuns().get(1).name(), is("Build"));
    assertThat(actualResponse.get().workflowRuns().get(1).id(), is(30433642L));
    assertThat(actualResponse.get().workflowRuns().get(1).workflowId(), is(159038));
    assertThat(actualResponse.get().workflowRuns().get(1).status(), is(WorkflowRunStatus.completed));
    assertThat(actualResponse.get().workflowRuns().get(1).createdAt(), is(ZonedDateTime.parse("2020-01-22T19:33:08Z")));
    assertThat(actualResponse.get().workflowRuns().get(1).displayTitle(), is("Update README.md"));
  }


  @Test
  public void listWorkflowRunsFiltered() throws Exception {
    ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);

    final WorkflowRunsResponseList workflowRunsListResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "workflowruns-list-workflowruns-filtered-response.json"), WorkflowRunsResponseList.class);
    final CompletableFuture<WorkflowRunsResponseList> fixtureResponse = completedFuture(workflowRunsListResponse);
    when(github.request(pathCaptor.capture(), eq(WorkflowRunsResponseList.class), any())).thenReturn(fixtureResponse);

    ListWorkflowRunsQueryParams params = ImmutableListWorkflowRunsQueryParams.builder()
        .status(WorkflowRunStatus.completed)
        .build();

    final CompletableFuture<WorkflowRunsResponseList> actualResponse =
        workflowRunsClient.listWorkflowRuns(159038, params);

    assertThat(pathCaptor.getValue(), is("/repos/someowner/somerepo/actions/workflow/159038/runs?status=completed"));

    assertThat(actualResponse.get().totalCount(), is(1));
    assertThat(actualResponse.get().workflowRuns().size(), is(1));

    assertThat(actualResponse.get().workflowRuns().get(0).name(), is("Build"));
    assertThat(actualResponse.get().workflowRuns().get(0).id(), is(30433642L));
    assertThat(actualResponse.get().workflowRuns().get(0).workflowId(), is(159038));
    assertThat(actualResponse.get().workflowRuns().get(0).status(), is(WorkflowRunStatus.completed));
    assertThat(actualResponse.get().workflowRuns().get(0).createdAt(), is(ZonedDateTime.parse("2020-01-22T19:33:08Z")));
    assertThat(actualResponse.get().workflowRuns().get(0).displayTitle(), is("Update README.md"));
  }

}