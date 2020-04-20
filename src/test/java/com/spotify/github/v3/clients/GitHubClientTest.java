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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.spotify.github.v3.exceptions.ReadOnlyRepositoryException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class GitHubClientTest {

  private GitHubClient github;
  private OkHttpClient client;

  @Before
  public void setUp() {
    client = mock(OkHttpClient.class);
    github = GitHubClient.create(client, URI.create("http://bogus"), "token");
  }

  @Test(expected = ReadOnlyRepositoryException.class)
  public void testSearchIssue() throws Throwable {

    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> capture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(capture.capture());

    final Response response =
        new okhttp3.Response.Builder()
            .code(403)
            .body(
                ResponseBody.create(
                    MediaType.get("application/json"),
                    "{\"message\":\"Repository "
                        + "was archived so is "
                        + "read-only.\","
                        + "\"documentation_url"
                        + "\":\"https://developer"
                        + ".github.com/enterprise/2"
                        + ".12/v3/repos/comments"
                        + "/#update-a-commit-comment"
                        + "\"}"))
            .message("foo")
            .protocol(Protocol.HTTP_1_1)
            .request(new Request.Builder().url("http://localhost/").build())
            .build();

    when(client.newCall(any())).thenReturn(call);
    IssueClient issueClient =
        github.createRepositoryClient("testorg", "testrepo").createIssueClient();

    CompletableFuture<Void> maybeSucceeded = issueClient.editComment(1, "some comment");
    capture.getValue().onResponse(call, response);
    try {
      maybeSucceeded.get();
    } catch (Exception e) {
      throw e.getCause();
    }
  }
}
