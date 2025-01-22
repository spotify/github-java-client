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
import com.spotify.github.v3.actions.workflowjobs.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkflowJobClientTest {

  private static final String FIXTURES_PATH = "com/spotify/github/v3/actions/workflowjobs/";
  private GitHubClient github;
  private WorkflowJobsClient workflowJobsClient;
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
    workflowJobsClient = new WorkflowJobsClient(github, "someowner", "somerepo");
    json = Json.create();
    when(github.json()).thenReturn(json);
  }

  @Test
  public void getWorkflowRun() throws Exception {
    final WorkflowJobResponse workflowJobResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "workflowjobs-get-workflowjob-response.json"), WorkflowJobResponse.class);
    final CompletableFuture<WorkflowJobResponse> fixtureResponse = completedFuture(workflowJobResponse);
    when(github.request(any(), eq(WorkflowJobResponse.class), any())).thenReturn(fixtureResponse);

    final CompletableFuture<WorkflowJobResponse> actualResponse =
        workflowJobsClient.getWorkflowJob(29679449, null);

    assertThat(actualResponse.get().id(), is(399444496L));
    assertThat(actualResponse.get().status(), is(WorkflowJobStatus.completed));

    assertThat(actualResponse.get().steps(), notNullValue());
    assertThat(actualResponse.get().steps().size(), is(10));
    assertThat(actualResponse.get().steps().get(0).name(), is("Set up job"));
    assertThat(actualResponse.get().steps().get(1).name(), is("Run actions/checkout@v2"));
  }

  @Test
  public void listWorkflowJobs() throws Exception {
    final WorkflowJobsResponseList workflowJobsListResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "workflowjobs-list-workflowjobs-response.json"), WorkflowJobsResponseList.class);
    final CompletableFuture<WorkflowJobsResponseList> fixtureResponse = completedFuture(workflowJobsListResponse);
    when(github.request(any(), eq(WorkflowJobsResponseList.class), any())).thenReturn(fixtureResponse);

    final CompletableFuture<WorkflowJobsResponseList> actualResponse =
        workflowJobsClient.listWorkflowJobs(159038, null);

    assertThat(actualResponse.get().totalCount(), is(1));
    assertThat(actualResponse.get().jobs().size(), is(1));

    assertThat(actualResponse.get().jobs().get(0).id(), is(399444496L));
    assertThat(actualResponse.get().jobs().get(0).status(), is(WorkflowJobStatus.completed));

    assertThat(actualResponse.get().jobs().get(0).steps(), notNullValue());
    assertThat(actualResponse.get().jobs().get(0).steps().size(), is(10));
    assertThat(actualResponse.get().jobs().get(0).steps().get(0).name(), is("Set up job"));
    assertThat(actualResponse.get().jobs().get(0).steps().get(1).name(), is("Run actions/checkout@v2"));
  }


  @Test
  public void listWorkflowRunsFiltered() throws Exception {
    ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);

    final WorkflowJobsResponseList workflowJobsListResponse =
        json.fromJson(
            loadResource(FIXTURES_PATH + "workflowjobs-list-workflowjobs-response.json"), WorkflowJobsResponseList.class);
    final CompletableFuture<WorkflowJobsResponseList> fixtureResponse = completedFuture(workflowJobsListResponse);
    when(github.request(pathCaptor.capture(), eq(WorkflowJobsResponseList.class), any())).thenReturn(fixtureResponse);

    ListWorkflowJobsQueryParams params = ImmutableListWorkflowJobsQueryParams.builder()
        .filter(ListWorkflowJobsQueryParams.Filter.latest)
        .build();

    final CompletableFuture<WorkflowJobsResponseList> actualResponse =
        workflowJobsClient.listWorkflowJobs(159038, null);

    assertThat(pathCaptor.getValue(), is("/repos/someowner/somerepo/actions/runs/159038/jobs"));

    assertThat(actualResponse.get().totalCount(), is(1));
    assertThat(actualResponse.get().jobs().size(), is(1));

    assertThat(actualResponse.get().jobs().get(0).id(), is(399444496L));
    assertThat(actualResponse.get().jobs().get(0).status(), is(WorkflowJobStatus.completed));

    assertThat(actualResponse.get().jobs().get(0).steps(), notNullValue());
    assertThat(actualResponse.get().jobs().get(0).steps().size(), is(10));
    assertThat(actualResponse.get().jobs().get(0).steps().get(0).name(), is("Set up job"));
    assertThat(actualResponse.get().jobs().get(0).steps().get(1).name(), is("Run actions/checkout@v2"));
  }

}