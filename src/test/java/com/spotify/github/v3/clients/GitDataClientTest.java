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

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.git.Reference;
import com.spotify.github.v3.git.Tag;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;

public class GitDataClientTest {

  private GitHubClient github;
  private GitDataClient gitDataClient;
  private Json json;

  private static String getFixture(String resource) throws IOException {
    return Resources.toString(getResource(GitDataClientTest.class, resource), defaultCharset());
  }

  @Before
  public void setUp() {
    github = mock(GitHubClient.class);
    gitDataClient = new GitDataClient(github, "someowner", "somerepo");
    json = Json.create();
    when(github.json()).thenReturn(json);
  }

  @Test
  public void getTagRef() throws Exception {
    final CompletableFuture<Reference> fixture =
        completedFuture(json.fromJson(getFixture("tag.json"), Reference.class));
    when(github.request("/repos/someowner/somerepo/git/refs/tags/0.0.1", Reference.class))
        .thenReturn(fixture);
    final Reference reference = gitDataClient.getTagReference("0.0.1").get();
    assertThat(reference.object().sha(), is("5926dd300de5fee31d445c57be223f00e128a634"));
  }

  @Test
  public void getTag() throws Exception {
    final CompletableFuture<Tag> fixture =
        completedFuture(json.fromJson(getFixture("annotated-tag.json"), Tag.class));
    when(github.request(
            "/repos/someowner/somerepo/git/tags/27210625b551200e7d3dc608935b1454523eaa8",
            Tag.class))
        .thenReturn(fixture);
    final Tag tag = gitDataClient.getTag("27210625b551200e7d3dc608935b1454523eaa8").get();
    assertThat(tag.object().sha(), is("ee959eb71f7041260dc864fb24574eec4caa8019"));
    assertThat(tag.object().type(), is("commit"));
  }

  @Test
  public void createReference() throws Exception {
    final CompletableFuture<Reference> fixture =
        completedFuture(json.fromJson(getFixture("reference.json"), Reference.class));
    final ImmutableMap<String, String> body = of(
        "ref", "featureA",
        "sha", "aa218f56b14c9653891f9e74264a383fa43fefbd"
    );
    when(github.post("/repos/someowner/somerepo/git/refs",
        github.json().toJsonUnchecked(body),
        Reference.class))
        .thenReturn(fixture);
    final Reference reference =
        gitDataClient.createReference("featureA", "aa218f56b14c9653891f9e74264a383fa43fefbd").get();
    assertThat(reference.ref(), is("featureA"));
    assertThat(reference.object().sha(), is("aa218f56b14c9653891f9e74264a383fa43fefbd"));
  }

  @Test
  public void createBranchReference() throws Exception {
    final CompletableFuture<Reference> fixture =
        completedFuture(json.fromJson(getFixture("branch.json"), Reference.class));
    final ImmutableMap<String, String> body = of(
        "ref", "refs/heads/featureA",
        "sha", "aa218f56b14c9653891f9e74264a383fa43fefbd"
    );
    when(github.post("/repos/someowner/somerepo/git/refs",
        github.json().toJsonUnchecked(body),
        Reference.class))
        .thenReturn(fixture);
    final Reference reference =
        gitDataClient.createBranchReference("featureA", "aa218f56b14c9653891f9e74264a383fa43fefbd").get();
    assertThat(reference.ref(), is("refs/heads/featureA"));
    assertThat(reference.object().sha(), is("aa218f56b14c9653891f9e74264a383fa43fefbd"));
  }

  @Test
  public void createTagReference() throws Exception {
    final CompletableFuture<Reference> fixture =
        completedFuture(json.fromJson(getFixture("tag.json"), Reference.class));
    final ImmutableMap<String, String> body =
        of(
            "ref", "refs/tags/0.0.1",
            "sha", "5926dd300de5fee31d445c57be223f00e128a634");
    when(github.post(
            "/repos/someowner/somerepo/git/refs",
            github.json().toJsonUnchecked(body),
            Reference.class))
        .thenReturn(fixture);
    final Reference reference =
        gitDataClient.createTagReference("0.0.1", "5926dd300de5fee31d445c57be223f00e128a634").get();
    assertThat(reference.object().sha(), is("5926dd300de5fee31d445c57be223f00e128a634"));
  }

  @Test
  public void createAnnotateTag() throws Exception {
    final CompletableFuture<Reference> reference =
        completedFuture(json.fromJson(getFixture("tag.json"), Reference.class));
    when(github.post(
            "/repos/someowner/somerepo/git/refs",
            github
                .json()
                .toJsonUnchecked(
                    of(
                        "ref", "refs/tags/0.0.1",
                        "sha", "5926dd300de5fee31d445c57be223f00e128a634")),
            Reference.class))
        .thenReturn(reference);

    final String now = Instant.now().toString();
    final ImmutableMap<String, Object> body =
        of(
            "tag", "0.0.1",
            "message", "release-tag",
            "object", "5926dd300de5fee31d445c57be223f00e128a634",
            "type", "commit",
            "tagger",
                of(
                    "name", "tingle",
                    "email", "janedoe@foo.com",
                    "date", now));
    final CompletableFuture<Tag> fixture =
        completedFuture(json.fromJson(getFixture("release-tag.json"), Tag.class));
    when(github.post(eq("/repos/someowner/somerepo/git/tags"), any(), eq(Tag.class)))
        .thenReturn(fixture);
    Tag tag =
        gitDataClient
            .createAnnotatedTag(
                "0.0.1",
                "5926dd300de5fee31d445c57be223f00e128a634",
                "release-tag",
                "tingle",
                "janedoe@foo.com")
            .join();
    assertThat(tag.object().sha(), is("5926dd300de5fee31d445c57be223f00e128a634"));
  }
}
