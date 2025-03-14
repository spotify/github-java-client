/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2016 - 2024 Spotify AB
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

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.checks.Installation;
import com.spotify.github.v3.user.requests.ImmutableSuspensionReason;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserClientTest {

    private GitHubClient github;
    private UserClient userClient;
    private String owner = "github";
    private Json json;
    private static String getFixture(String resource) throws IOException {
        return Resources.toString(getResource(TeamClientTest.class, resource), defaultCharset());
    }

    @BeforeEach
    public void setUp() {
        github = mock(GitHubClient.class);
        userClient = new UserClient(github, owner);
        json = Json.create();
        when(github.json()).thenReturn(json);
    }

    @Test
    public void testSuspendUserSuccess() throws Exception {
        Response response = mock(Response.class);
        when(response.code()).thenReturn(204);
        when(github.put(eq("/users/username/suspended"), any())).thenReturn(completedFuture(response));
        final CompletableFuture<Boolean> result = userClient.suspendUser("username", ImmutableSuspensionReason.builder().reason("That's why").build());
        assertTrue(result.get());
    }

    @Test
    public void testSuspendUserFailure() throws Exception {
        Response response = mock(Response.class);
        when(response.code()).thenReturn(403);
        when(github.put(eq("/users/username/suspended"), any())).thenReturn(completedFuture(response));
        final CompletableFuture<Boolean> result = userClient.suspendUser("username", ImmutableSuspensionReason.builder().reason("That's why").build());
        assertFalse(result.get());
    }

    @Test
    public void testUnSuspendUserSuccess() throws Exception {
        Response response = mock(Response.class);
        when(response.code()).thenReturn(204);
        when(github.delete(eq("/users/username/suspended"), any())).thenReturn(completedFuture(response));
        final CompletableFuture<Boolean> result = userClient.unSuspendUser("username", ImmutableSuspensionReason.builder().reason("That's why").build());
        assertTrue(result.get());
    }

    @Test
    public void testUnSuspendUserFailure() throws Exception {
        Response response = mock(Response.class);
        when(response.code()).thenReturn(403);
        when(github.delete(eq("/users/username/suspended"), any())).thenReturn(completedFuture(response));
        final CompletableFuture<Boolean> result = userClient.unSuspendUser("username", ImmutableSuspensionReason.builder().reason("That's why").build());
        assertFalse(result.get());
    }

    @Test
    public void testAppClient() throws Exception {
        final GithubAppClient githubAppClient = userClient.createGithubAppClient();
        final CompletableFuture<Installation> fixture =
                completedFuture(json.fromJson(getFixture("../githubapp/installation.json"), Installation.class));
        when(github.request("/users/github/installation", Installation.class)).thenReturn(fixture);

        final Installation installation = githubAppClient.getUserInstallation().get();

        assertThat(installation.id(), is(1));
        assertThat(installation.account().login(), is("github"));
    }
}
