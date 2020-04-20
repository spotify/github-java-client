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
import static com.spotify.github.v3.UserTest.assertUser;
import static com.spotify.github.v3.clients.GitHubClient.LIST_COMMIT_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_FOLDERCONTENT_TYPE_REFERENCE;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.comment.Comment;
import com.spotify.github.v3.repos.Branch;
import com.spotify.github.v3.repos.Commit;
import com.spotify.github.v3.repos.CommitComparison;
import com.spotify.github.v3.repos.CommitItem;
import com.spotify.github.v3.repos.CommitStatus;
import com.spotify.github.v3.repos.Content;
import com.spotify.github.v3.repos.FolderContent;
import com.spotify.github.v3.repos.Repository;
import com.spotify.github.v3.repos.RepositoryTest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;

public class RepositoryClientTest {

  private GitHubClient github;
  private RepositoryClient repoClient;
  private Json json;

  private static String getFixture(String resource) throws IOException {
    return Resources.toString(getResource(RepositoryTest.class, resource), defaultCharset());
  }

  @Before
  public void setUp() {
    github = mock(GitHubClient.class);
    repoClient = new RepositoryClient(github, "someowner", "somerepo");
    json = Json.create();
    when(github.json()).thenReturn(json);
  }

  @Test
  public void getRepository() throws Exception {
    final CompletableFuture<Repository> fixture =
        completedFuture(json.fromJson(getFixture("repository_get.json"), Repository.class));
    when(github.request("/repos/someowner/somerepo", Repository.class)).thenReturn(fixture);
    final Repository repository = repoClient.getRepository().get();
    assertThat(repository.id(), is(1296269));
    assertUser(repository.owner());
    assertThat(repository.name(), is("Hello-World"));
    assertThat(repository.fullName(), is(repository.owner().login() + "/Hello-World"));
    assertThat(repository.isPrivate(), is(false));
    assertThat(repository.fork(), is(false));
  }

  @Test
  public void listCommits() throws Exception {
    final CompletableFuture<List<CommitItem>> fixture =
        completedFuture(
            json.fromJson("[" + getFixture("commit_item.json") + "]", LIST_COMMIT_TYPE_REFERENCE));
    when(github.request("/repos/someowner/somerepo/commits", LIST_COMMIT_TYPE_REFERENCE))
        .thenReturn(fixture);
    final List<CommitItem> commits = repoClient.listCommits().get();
    assertThat(commits.size(), is(1));
    assertUser(commits.get(0).author().get());
    assertThat(commits.get(0).commit().message(), is("Fix all the bugs"));
    assertThat(
        commits.get(0).commit().tree().sha(), is("6dcb09b5b57875f334f61aebed695e2e4193db5e"));
  }

  @Test
  public void getCommit() throws Exception {
    final CompletableFuture<Commit> fixture =
        completedFuture(json.fromJson(getFixture("commit.json"), Commit.class));
    when(github.request("/repos/someowner/somerepo/commits/thesha", Commit.class))
        .thenReturn(fixture);
    final Commit commit = repoClient.getCommit("thesha").get();
    assertUser(commit.author().get());
    assertThat(commit.commit().message(), is("Fix all the bugs"));
    assertThat(commit.files().size(), is(1));
    assertThat(commit.files().get(0).filename(), is("file1.txt"));
  }

  @Test
  public void getCommitStatus() throws Exception {
    final CompletableFuture<CommitStatus> fixture =
        completedFuture(json.fromJson(getFixture("commit_status.json"), CommitStatus.class));
    when(github.request("/repos/someowner/somerepo/commits/thesha/status", CommitStatus.class))
        .thenReturn(fixture);
    final CommitStatus status = repoClient.getCommitStatus("thesha").get();
    assertThat(status.state(), is("success"));
  }

  @Test
  public void getFileContent() throws Exception {
    final CompletableFuture<Content> fixture =
        completedFuture(json.fromJson(getFixture("content.json"), Content.class));
    when(github.request("/repos/someowner/somerepo/contents/test/README.md", Content.class))
        .thenReturn(fixture);
    final Content fileContent = repoClient.getFileContent("test/README.md").get();
    assertThat(fileContent.type(), is("file"));
    assertThat(fileContent.name(), is("README.md"));
    assertThat(fileContent.encoding(), is("base64"));
    assertThat(fileContent.content(), is("encoded content ..."));
  }

  @Test
  public void getFolderContent() throws Exception {
    final CompletableFuture<List<FolderContent>> fixture =
        completedFuture(
            json.fromJson(
                "[" + getFixture("content.json") + "]", LIST_FOLDERCONTENT_TYPE_REFERENCE));
    when(github.request(
            "/repos/someowner/somerepo/contents/test/some/folder?ref=theref",
            LIST_FOLDERCONTENT_TYPE_REFERENCE))
        .thenReturn(fixture);
    final List<FolderContent> folderContent =
        repoClient.getFolderContent("test/some/folder", "theref").get();
    assertThat(folderContent.size(), is(1));
    assertThat(folderContent.get(0).type(), is("file"));
    assertThat(folderContent.get(0).name(), is("README.md"));
  }

  @Test
  public void compareCommits() throws Exception {
    final CompletableFuture<CommitComparison> fixture =
        completedFuture(json.fromJson(getFixture("compare_commit.json"), CommitComparison.class));
    when(github.request(
            "/repos/someowner/somerepo/compare/493b8934db4eb02353ecb91a58e8cb353018777c...01a0298b8a805406e42499481594a37dc39ba8f8",
            CommitComparison.class))
        .thenReturn(fixture);
    final CommitComparison commitComparison =
        repoClient
            .compareCommits(
                "493b8934db4eb02353ecb91a58e8cb353018777c",
                "01a0298b8a805406e42499481594a37dc39ba8f8")
            .get();
    assertThat(
        commitComparison.mergeBaseCommit().sha(), is("01a0298b8a805406e42499481594a37dc39ba8f8"));
    assertThat(commitComparison.baseCommit().sha(), is("493b8934db4eb02353ecb91a58e8cb353018777c"));
  }

  @Test
  public void getBranch() throws Exception {
    final CompletableFuture<Branch> fixture =
        completedFuture(json.fromJson(getFixture("branch.json"), Branch.class));
    when(github.request("/repos/someowner/somerepo/branches/somebranch", Branch.class))
        .thenReturn(fixture);
    final Branch branch = repoClient.getBranch("somebranch").get();
    assertThat(branch.commit().sha(), is("6dcb09b5b57875f334f61aebed695e2e4193db5e"));
  }

  @Test
  public void testCommentCreated() throws IOException {
    final String expectedRequestBody = json.toJsonUnchecked(ImmutableMap.of("body", "Me too"));

    final CompletableFuture<Comment> fixture =
        completedFuture(json.fromJson(getFixture("comment.json"), Comment.class));
    when(github.post(
            "/repos/someowner/somerepo/commits/someweirdsha/comments",
            expectedRequestBody,
            Comment.class))
        .thenReturn(fixture);
    final Comment comment = repoClient.createComment("someweirdsha", "Me too").join();

    assertThat(comment.id(), is(123));
    assertThat(comment.commitId().get(), is("6dcb09b5b57875f334f61aebed695e2e4193db5e"));
  }

  @Test
  public void getComment() throws IOException {
    final CompletableFuture<Comment> fixture =
        completedFuture(json.fromJson(getFixture("comment.json"), Comment.class));
    when(github.request("/repos/someowner/somerepo/comments/123", Comment.class))
        .thenReturn(fixture);
    final Comment comment = repoClient.getComment(123).join();

    assertThat(comment.id(), is(123));
    assertThat(comment.commitId().get(), is("6dcb09b5b57875f334f61aebed695e2e4193db5e"));
  }
}
