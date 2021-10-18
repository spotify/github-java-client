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
import static com.spotify.github.v3.UserTest.assertUser;
import static com.spotify.github.v3.clients.GitHubClient.LIST_REFERENCES;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.git.ImmutableTree;
import com.spotify.github.v3.git.ImmutableTreeItem;
import com.spotify.github.v3.git.Reference;
import com.spotify.github.v3.git.ShaLink;
import com.spotify.github.v3.git.Tag;
import com.spotify.github.v3.git.Tree;
import com.spotify.github.v3.git.TreeItem;
import com.spotify.github.v3.repos.Commit;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
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
  public void listMatchingReferences() throws Exception {
    final CompletableFuture<List<Reference>> fixture =
        completedFuture(json.fromJson(getFixture("reference_list.json"), LIST_REFERENCES));
    when(github.request(
            "/repos/someowner/somerepo/git/matching-refs/heads/feature", LIST_REFERENCES))
        .thenReturn(fixture);
    final List<Reference> matchingReferences =
        gitDataClient.listMatchingReferences("heads/feature").get();
    assertThat(matchingReferences.size(), is(2));
    for (Reference ref : matchingReferences) {
      assertThat(ref.ref(), containsString("heads/feature"));
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  public void listReferences() throws Exception {
    final CompletableFuture<List<Reference>> fixture =
        completedFuture(json.fromJson(getFixture("tags_list.json"), LIST_REFERENCES));
    when(github.request("/repos/someowner/somerepo/git/refs/tags", LIST_REFERENCES))
        .thenReturn(fixture);
    final List<Reference> matchingReferences = gitDataClient.listReferences("refs/tags").get();
    assertThat(matchingReferences.size(), is(1));
    for (Reference ref : matchingReferences) {
      assertThat(ref.ref(), containsString("refs/tags"));
    }
  }

  public void createReference() throws Exception {
    final CompletableFuture<Reference> fixture =
        completedFuture(json.fromJson(getFixture("reference.json"), Reference.class));
    final ImmutableMap<String, String> body =
        of(
            "ref", "featureA",
            "sha", "aa218f56b14c9653891f9e74264a383fa43fefbd");
    when(github.post(
            "/repos/someowner/somerepo/git/refs",
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
    final ImmutableMap<String, String> body =
        of(
            "ref", "refs/heads/featureA",
            "sha", "aa218f56b14c9653891f9e74264a383fa43fefbd");
    when(github.post(
            "/repos/someowner/somerepo/git/refs",
            github.json().toJsonUnchecked(body),
            Reference.class))
        .thenReturn(fixture);
    final Reference reference =
        gitDataClient
            .createBranchReference("featureA", "aa218f56b14c9653891f9e74264a383fa43fefbd")
            .get();
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

  @Test
  public void testCreateCommit() throws Exception {
    final CompletableFuture<Commit> fixture =
        completedFuture(json.fromJson(getFixture("commit.json"), Commit.class));

    final String expectedRequestBody =
        json.toJsonUnchecked(
            ImmutableMap.of("message", "message", "parents", emptyList(), "tree", "thesha"));

    when(github.post("/repos/someowner/somerepo/git/commits", expectedRequestBody, Commit.class))
        .thenReturn(fixture);
    final Commit commit = gitDataClient.createCommit("message", emptyList(), "thesha").get();
    assertUser(commit.author().get());
    assertThat(commit.commit().message(), is("Fix all the bugs"));
    assertThat(commit.files().size(), is(1));
    assertThat(commit.files().get(0).filename(), is("file1.txt"));
  }

  @Test
  public void testGetTree() throws IOException {
    final CompletableFuture<Tree> fixture =
        completedFuture(json.fromJson(getFixture("tree.json"), Tree.class));

    when(github.request("/repos/someowner/somerepo/git/trees/thesha", Tree.class))
        .thenReturn(fixture);

    final Tree tree =
        gitDataClient
            .getTree("thesha")
            .join();
    assertThat(tree.sha(), is("9c27bd92524e2b57b569d4c86695b3993d9b8f9f"));
  }

  @Test
  public void testGetRecursiveTree() throws IOException {
    final CompletableFuture<Tree> fixture =
            completedFuture(json.fromJson(getFixture("recursive-tree.json"), Tree.class));

    when(github.request("/repos/someowner/somerepo/git/trees/thesha", Tree.class))
            .thenReturn(fixture);

    final Tree tree =
            gitDataClient
                    .getTree("thesha")
                    .join();
    assertThat(tree.sha(), is("9c27bd92524e2b57b569d4c86695b3993d9b8f9f"));
    assertThat(tree.tree().size(), is(7));
  }

  @Test
  public void testCreateTree() throws IOException {
    final TreeItem treeItem =
        ImmutableTreeItem.builder()
            .path("somefolder/somefolder/somefile")
            .mode("100644")
            .type("commit")
            .sha("9c27bd92524e2b57b569d4c86695b3993d9b8f9f")
            .build();
    final Tree treeObject = ImmutableTree.builder().addTree(treeItem).build();

    final String expectedRequestBody =
        json.toJsonUnchecked(
            ImmutableMap.of(
                "base_tree",
                "9c27bd92524e2b57b569d4c86695b3993d9b8f9f",
                "tree",
                List.of(treeItem)));

    final CompletableFuture<Tree> fixture =
        completedFuture(json.fromJson(getFixture("tree.json"), Tree.class));

    when(github.post("/repos/someowner/somerepo/git/trees", expectedRequestBody, Tree.class))
        .thenReturn(fixture);

    final Tree tree =
        gitDataClient
            .createTree(treeObject.tree(), "9c27bd92524e2b57b569d4c86695b3993d9b8f9f")
            .join();
    assertThat(tree.sha(), is("9c27bd92524e2b57b569d4c86695b3993d9b8f9f"));
  }

  @Test
  public void testCreateBlob() throws IOException {
    final String expectedRequestBody =
        json.toJsonUnchecked(ImmutableMap.of("content", "content", "encoding", "utf-8|base64"));

    final CompletableFuture<ShaLink> fixture =
        completedFuture(json.fromJson(getFixture("shalink.json"), ShaLink.class));
    when(github.post("/repos/someowner/somerepo/git/blobs", expectedRequestBody, ShaLink.class))
        .thenReturn(fixture);
    final ShaLink shalink = gitDataClient.createBlob("content").join();

    assertThat(shalink.sha(), is("8fc4e0fe57752b892a921806a1352e4cc72dff37"));
  }
}
