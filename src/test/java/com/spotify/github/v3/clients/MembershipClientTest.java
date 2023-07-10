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

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.orgs.Membership;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Headers.class, ResponseBody.class, Response.class})
public class MembershipClientTest {

  private GitHubClient github;

  private MembershipClient membershipClient;

  private Json json;

  private static String getFixture(String resource) throws IOException {
    return Resources.toString(getResource(MembershipClientTest.class, resource), defaultCharset());
  }

  @Before
  public void setUp() {
    github = mock(GitHubClient.class);
    membershipClient = new MembershipClient(github, "github", "1");
    json = Json.create();
    when(github.json()).thenReturn(json);
  }

  @Test
  public void getMembership() throws Exception {
    final CompletableFuture<Membership> fixture =
        completedFuture(json.fromJson(getFixture("membership.json"), Membership.class));
    when(github.request("/orgs/github/teams/1/memberships/octocat", Membership.class)).thenReturn(fixture);
    final Membership membership = membershipClient.getMembership("octocat").get();
    assertThat(membership.url().toString(), is("https://api.github.com/teams/1/memberships/octocat"));
    assertThat(membership.role(), is("maintainer"));
    assertThat(membership.state(), is("active"));
  }
}
