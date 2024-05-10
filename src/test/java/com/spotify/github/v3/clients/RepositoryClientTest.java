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
import static com.spotify.github.FixtureHelper.loadFixture;
import static com.spotify.github.v3.UserTest.assertUser;
import static com.spotify.github.v3.clients.GitHubClient.LIST_BRANCHES;
import static com.spotify.github.v3.clients.GitHubClient.LIST_COMMIT_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_FOLDERCONTENT_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_PR_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_REPOSITORY;
import static com.spotify.github.v3.clients.GitHubClient.LIST_REPOSITORY_INVITATION;
import static com.spotify.github.v3.clients.MockHelper.createMockResponse;
import static com.spotify.github.v3.clients.RepositoryClient.STATUS_URI_TEMPLATE;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.spotify.github.async.Async;
import com.spotify.github.async.AsyncPage;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.comment.Comment;
import com.spotify.github.v3.prs.PullRequestItem;
import com.spotify.github.v3.repos.Branch;
import com.spotify.github.v3.repos.Commit;
import com.spotify.github.v3.repos.CommitComparison;
import com.spotify.github.v3.repos.CommitItem;
import com.spotify.github.v3.repos.CommitStatus;
import com.spotify.github.v3.repos.CommitWithFolderContent;
import com.spotify.github.v3.repos.Content;
import com.spotify.github.v3.repos.FolderContent;
import com.spotify.github.v3.repos.Repository;
import com.spotify.github.v3.repos.RepositoryInvitation;
import com.spotify.github.v3.repos.RepositoryPermission;
import com.spotify.github.v3.repos.RepositoryTest;
import com.spotify.github.v3.repos.Status;
import com.spotify.github.v3.repos.requests.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

public class RepositoryClientTest {

  private GitHubClient github;
  private RepositoryClient repoClient;
  private Json json;

  private static String getFixture(String resource) throws IOException {
    return Resources.toString(getResource(RepositoryTest.class, resource), defaultCharset());
  }

  @BeforeEach
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
    assertThat(repository.isArchived(), is(false));
    assertThat(repository.fork(), is(false));
  }

  @Test
  public void updateRepository() throws Exception {
    final CompletableFuture<Repository> fixture =
        completedFuture(json.fromJson(getFixture("repository_get.json"), Repository.class));
    when(github.patch(eq("/repos/someowner/somerepo"), eq("{\"allow_auto_merge\":true}"), eq(Repository.class)))
        .thenReturn(fixture);
    RepositoryUpdate request =
        ImmutableRepositoryUpdate.builder().allowAutoMerge(Optional.of(true)).build();
    final Repository repository = repoClient.updateRepository(request).get();
    assertThat(repository.id(), is(1296269));
    assertUser(repository.owner());
    assertThat(repository.name(), is("Hello-World"));
    assertThat(repository.fullName(), is(repository.owner().login() + "/Hello-World"));
    assertThat(repository.isPrivate(), is(false));
    assertThat(repository.isArchived(), is(false));
    assertThat(repository.fork(), is(false));
  }

  @Test
  public void listOrganizationRepositories() throws Exception {
    final CompletableFuture<List<Repository>> fixture =
        completedFuture(json.fromJson(getFixture("list_of_repos_for_org.json"), LIST_REPOSITORY));
    when(github.request("/orgs/someowner/repos", LIST_REPOSITORY)).thenReturn(fixture);
    final List<Repository> repositories = repoClient.listOrganizationRepositories().get();
    assertThat(repositories.get(0).id(), is(1296269));
    assertThat(repositories.size(), is(1));
  }

  @Test
  public void listAuthenticatedUserRepositories() throws Exception {
    final String pageLink = "<https://github.com/api/v3/user/repos>; rel=\"first\"";
    final String pageBody = getFixture("list_of_repos_for_authenticated_user.json");
    final Response pageResponse = createMockResponse(pageLink, pageBody);

    when(github.request("/user/repos")).thenReturn(completedFuture(pageResponse));

    final Iterable<AsyncPage<Repository>> pageIterator =
        () ->
            repoClient.listAuthenticatedUserRepositories(
                ImmutableAuthenticatedUserRepositoriesFilter.builder().build());
    final List<Repository> repositories =
        Async.streamFromPaginatingIterable(pageIterator).collect(Collectors.toList());

    assertThat(repositories.get(0).id(), is(1296269));
    assertThat(repositories.size(), is(1));
  }

  @Test
  public void isCollaborator() throws Exception {
    final Response response = mock(Response.class);
    when(response.code()).thenReturn(204);
    when(github.request("/repos/someowner/somerepo/collaborators/user"))
        .thenReturn(completedFuture(response));
    boolean isCollaborator = repoClient.isCollaborator("user").get();
    assertTrue(isCollaborator);
  }

  @Test
  public void isNotCollaborator() throws Exception {
    final Response response = mock(Response.class);
    when(response.code()).thenReturn(404);
    when(github.request("/repos/someowner/somerepo/collaborators/user"))
        .thenReturn(completedFuture(response));
    boolean isCollaborator = repoClient.isCollaborator("user").get();
    assertFalse(isCollaborator);
  }

  @Test
  public void addCollaborator() throws Exception {
    final Response response = createMockResponse("", getFixture("repository_invitation.json"));
    when(github.put("/repos/someowner/somerepo/collaborators/user", "{\"permission\":\"pull\"}"))
        .thenReturn(completedFuture(response));

    final Optional<RepositoryInvitation> maybeInvite =
        repoClient.addCollaborator("user", RepositoryPermission.PULL).get();

    assertTrue(maybeInvite.isPresent());
    final RepositoryInvitation repoInvite = maybeInvite.get();
    assertThat(repoInvite.id(), is(1));
    assertThat(repoInvite.nodeId(), is("MDEwOlJlcG9zaXRvcnkxMjk2MjY5"));
    assertThat(repoInvite.repository().id(), is(1296269));
    assertUser(repoInvite.repository().owner());
    assertUser(repoInvite.invitee());
    assertUser(repoInvite.inviter());
    assertThat(repoInvite.permissions(), is("write"));
  }

  @Test
  public void addCollaboratorUserExists() throws Exception {
    final Response response = mock(Response.class);
    when(response.code()).thenReturn(204);
    when(github.put("/repos/someowner/somerepo/collaborators/user", "{\"permission\":\"pull\"}"))
        .thenReturn(completedFuture(response));

    final Optional<RepositoryInvitation> maybeInvite =
        repoClient.addCollaborator("user", RepositoryPermission.PULL).get();

    assertTrue(maybeInvite.isEmpty());
  }

  @Test
  public void removeCollaborator() throws Exception {
    CompletableFuture<Response> response = completedFuture(mock(Response.class));
    final ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
    when(github.delete(capture.capture())).thenReturn(response);

    CompletableFuture<Void> deleteResponse = repoClient.removeCollaborator("user");
    deleteResponse.get();

    assertThat(capture.getValue(), is("/repos/someowner/somerepo/collaborators/user"));
  }

  @Test
  public void removeInvite() throws Exception {
    CompletableFuture<Response> response = completedFuture(mock(Response.class));
    final ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
    when(github.delete(capture.capture())).thenReturn(response);

    CompletableFuture<Void> deleteResponse = repoClient.removeInvite("invitation1");
    deleteResponse.get();

    assertThat(capture.getValue(), is("/repos/someowner/somerepo/invitations/invitation1"));
  }

  @Test
  public void listInvites() throws Exception {
    final CompletableFuture<List<RepositoryInvitation>> fixture =
        completedFuture(
            json.fromJson(
                "[" + getFixture("repository_invitation.json") + "]", LIST_REPOSITORY_INVITATION));
    when(github.request("/repos/someowner/somerepo/invitations", LIST_REPOSITORY_INVITATION))
        .thenReturn(fixture);

    final List<RepositoryInvitation> invitations = repoClient.listInvitations().get();
    assertThat(invitations.size(), is(1));
    assertThat(invitations.get(0).repository().name(), is("Hello-World"));
    assertThat(invitations.get(0).inviter().login(), is("octocat"));
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
  public void listPullRequestsForCommit() throws Exception {
    final CompletableFuture<List<PullRequestItem>> fixture =
        completedFuture(
            json.fromJson(
                "[" + getFixture("../prs/pull_request_item.json") + "]", LIST_PR_TYPE_REFERENCE));
    when(github.request(
            eq("/repos/someowner/somerepo/commits/thesha/pulls"),
            eq(LIST_PR_TYPE_REFERENCE),
            any()))
        .thenReturn(fixture);
    final List<PullRequestItem> prs = repoClient.listPullRequestsForCommit("thesha").get();
    assertThat(prs.size(), is(1));
    assertThat(prs.get(0).number(), is(1347));
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
  public void createFileContent() throws Exception {
    String rawFileCreateRequest = getFixture("create-content-request.json");
    final CompletableFuture<CommitWithFolderContent> fixture =
        completedFuture(
            json.fromJson(
                getFixture("create-content-repsonse.json"), CommitWithFolderContent.class));
    when(github.put(
            eq("/repos/someowner/somerepo/contents/test/README.md"),
            argThat(body -> SameJSONAs.sameJSONAs(rawFileCreateRequest).matches(body)),
            eq(CommitWithFolderContent.class)))
        .thenReturn(fixture);

    FileCreate fileCreateRequest =
        ImmutableFileCreate.builder()
            .message("my commit message")
            .content("encoded content ...")
            .build();

    final CommitWithFolderContent commitWithFolderContent =
        repoClient.createFileContent("test/README.md", fileCreateRequest).get();
    assertThat(commitWithFolderContent.commit().message(), is("my commit message"));
    assertThat(commitWithFolderContent.content().type(), is("file"));
    assertThat(commitWithFolderContent.content().name(), is("README.md"));
    assertThat(commitWithFolderContent.content().path(), is("test/README.md"));
  }

  @Test
  public void updateFileContent() throws Exception {
    String rawFileUpdateRequest = getFixture("update-content-request.json");
    final CompletableFuture<CommitWithFolderContent> fixture =
        completedFuture(
            json.fromJson(
                getFixture("create-content-repsonse.json"), CommitWithFolderContent.class));
    when(github.put(
            eq("/repos/someowner/somerepo/contents/test/README.md"),
            argThat(body -> SameJSONAs.sameJSONAs(rawFileUpdateRequest).matches(body)),
            eq(CommitWithFolderContent.class)))
        .thenReturn(fixture);

    FileUpdate fileUpdateRequest =
        ImmutableFileUpdate.builder()
            .message("my commit message")
            .content("encoded content ...")
            .branch("test-branch")
            .sha("12345")
            .build();

    final CommitWithFolderContent commitWithFolderContent =
        repoClient.updateFileContent("test/README.md", fileUpdateRequest).get();
    assertThat(commitWithFolderContent.commit().message(), is("my commit message"));
    assertThat(commitWithFolderContent.content().type(), is("file"));
    assertThat(commitWithFolderContent.content().name(), is("README.md"));
    assertThat(commitWithFolderContent.content().path(), is("test/README.md"));
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
    assertThat(branch.isProtected().orElse(false), is(true));
    assertThat(
        branch.protectionUrl().get().toString(),
        is("https://api.github.com/repos/octocat/Hello-World/branches/master/protection"));
    assertThat(branch.commit().sha(), is("6dcb09b5b57875f334f61aebed695e2e4193db5e"));
    assertThat(
        branch.commit().url().toString(),
        is(
            "https://api.github.com/repos/octocat/Hello-World/commits/c5b97d5ae6c19d5c5df71a34c7fbeeda2479ccbc"));
    assertTrue(branch.protection().isPresent());
    assertTrue(branch.protection().get().enabled());
    assertThat(
        branch.protection().get().requiredStatusChecks().enforcementLevel(), is("non_admins"));
    assertTrue(branch.protection().get().requiredStatusChecks().contexts().contains("Context 1"));
    assertTrue(branch.protection().get().requiredStatusChecks().contexts().contains("Context 2"));
  }

  @Test
  public void getBranchWithNoProtection() throws Exception {
    final CompletableFuture<Branch> fixture =
        completedFuture(json.fromJson(getFixture("branch-not-protected.json"), Branch.class));
    when(github.request("/repos/someowner/somerepo/branches/somebranch", Branch.class))
        .thenReturn(fixture);
    final Branch branch = repoClient.getBranch("somebranch").get();
    assertThat(branch.isProtected().orElse(false), is(false));
    assertTrue(branch.protectionUrl().isEmpty());
    assertThat(branch.commit().sha(), is("6dcb09b5b57875f334f61aebed695e2e4193db5e"));
  }

  @Test
  public void getBranchWithoutProtectionFields() throws Exception {
    final CompletableFuture<Branch> fixture =
        completedFuture(
            json.fromJson(getFixture("branch-no-protection-fields.json"), Branch.class));
    when(github.request("/repos/someowner/somerepo/branches/somebranch", Branch.class))
        .thenReturn(fixture);
    final Branch branch = repoClient.getBranch("somebranch").get();
    assertThat(branch.isProtected().orElse(false), is(false));
    assertTrue(branch.protectionUrl().isEmpty());
    assertThat(branch.commit().sha(), is("6dcb09b5b57875f334f61aebed695e2e4193db5e"));
    assertThat(
        branch.commit().url().toString(),
        is(
            "https://api.github.com/repos/octocat/Hello-World/commits/c5b97d5ae6c19d5c5df71a34c7fbeeda2479ccbc"));
  }

  @Test
  public void getBranchWithCharactersIncorrectlyUnescapedByTheGithubApi() throws Exception {
    final CompletableFuture<Branch> fixture =
        completedFuture(json.fromJson(getFixture("branch-escape-chars.json"), Branch.class));
    when(github.request(
            "/repos/someowner/somerepo/branches/unescaped-percent-sign-%", Branch.class))
        .thenReturn(fixture);
    final Branch branch = repoClient.getBranch("unescaped-percent-sign-%").get();
    assertThat(branch.commit().sha(), is("6dcb09b5b57875f334f61aebed695e2e4193db5e"));
    assertThat(
        branch.protectionUrl().get().toString(),
        is(
            "https://api.github.com/repos/octocat/Hello-World/branches/unescaped-percent-sign-%25/protection"));
  }

  @Test
  public void getBranchWithCharactersIncorrectlyUnescapedByTheGithubApi_uriVariationTwo()
      throws Exception {
    final CompletableFuture<Branch> fixture =
        completedFuture(
            json.fromJson(getFixture("branch-escape-chars-url-variation-two.json"), Branch.class));
    when(github.request(
            "/repos/someowner/somerepo/branches/unescaped-percent-sign-%", Branch.class))
        .thenReturn(fixture);
    final Branch branch = repoClient.getBranch("unescaped-percent-sign-%").get();
    assertThat(branch.commit().sha(), is("6dcb09b5b57875f334f61aebed695e2e4193db5e"));
    assertThat(
        branch.protectionUrl().get().toString(),
        is(
            "https://api.github.com/api/v3/repos/octocat/Hello-World/branches/branch-name-with-slashes/unescaped-percent-sign-%25/protection"));
  }

  @Test
  public void listBranches() throws Exception {
    final CompletableFuture<List<Branch>> fixture =
        completedFuture(json.fromJson(getFixture("list_branches.json"), LIST_BRANCHES));
    when(github.request("/repos/someowner/somerepo/branches", LIST_BRANCHES)).thenReturn(fixture);
    final List<Branch> branches = repoClient.listBranches().get();
    assertThat(branches.get(0).commit().sha(), is("c5b97d5ae6c19d5c5df71a34c7fbeeda2479ccbc"));
    assertThat(branches.size(), is(1));
  }

  @Test
  void listAllBranches() throws Exception {
    final String link =
        "<https://github.com/api/v3/repos/someowner/somerepo/branches>; rel=\"last\"";
    final Response response = createMockResponse(link, getFixture("list_branches.json"));

    when(github.request("/repos/someowner/somerepo/branches"))
        .thenReturn(completedFuture(response));
    final List<Branch> branches =
        Async.streamFromPaginatingIterable(repoClient::listAllBranches)
            .collect(Collectors.toList());
    assertThat(branches.get(0).commit().sha(), is("c5b97d5ae6c19d5c5df71a34c7fbeeda2479ccbc"));
    assertThat(branches.size(), is(1));
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

  @Test
  public void testStatusesPaginationForeach() throws Exception {
    final String firstPageLink =
        "<https://github.com/api/v3/repos/someowner/somerepo/statuses/553c2077f0edc3d5dc5d17262f6aa498e69d6f8e?page=2>; rel=\"next\", <https://github.com/api/v3/repos/someowner/somerepo/statuses/553c2077f0edc3d5dc5d17262f6aa498e69d6f8e?page=2>; rel=\"last\"";
    final String firstPageBody = loadFixture("clients/statuses_page1.json");

    final Response firstPageResponse = createMockResponse(firstPageLink, firstPageBody);

    final String lastPageLink =
        "<https://github.com/api/v3/repos/someowner/somerepo/statuses/553c2077f0edc3d5dc5d17262f6aa498e69d6f8e>; rel=\"first\", <https://github.com/api/v3/repos/someowner/somerepo/statuses/553c2077f0edc3d5dc5d17262f6aa498e69d6f8e>; rel=\"prev\"";
    final String lastPageBody = loadFixture("clients/statuses_page2.json");
    final Response lastPageResponse = createMockResponse(lastPageLink, lastPageBody);

    when(github.urlFor("")).thenReturn("https://github.com/api/v3");

    when(github.request(
            format(
                STATUS_URI_TEMPLATE,
                "someowner",
                "somerepo",
                "553c2077f0edc3d5dc5d17262f6aa498e69d6f8e")))
        .thenReturn(completedFuture(firstPageResponse));
    when(github.request(
            format(
                STATUS_URI_TEMPLATE + "?page=2",
                "someowner",
                "somerepo",
                "553c2077f0edc3d5dc5d17262f6aa498e69d6f8e")))
        .thenReturn(completedFuture(lastPageResponse));

    final List<Status> listStatuses = Lists.newArrayList();
    repoClient
        .listCommitStatuses("553c2077f0edc3d5dc5d17262f6aa498e69d6f8e", 10)
        .forEachRemaining(page -> page.iterator().forEachRemaining(listStatuses::add));

    assertThat(listStatuses.size(), is(12));
    assertThat(listStatuses.get(0).id(), is(61764535L));
    assertThat(listStatuses.get(listStatuses.size() - 1).id(), is(61756641L));
  }

  @Test
  public void merge() throws IOException {
    CompletableFuture<Response> okResponse =
        completedFuture(
            new Response.Builder()
                .request(new Request.Builder().url("http://example.com/whatever").build())
                .protocol(Protocol.HTTP_1_1)
                .message("")
                .code(201)
                .body(
                    ResponseBody.create(
                        MediaType.get("application/json"), getFixture("merge_commit_item.json")))
                .build());
    final String expectedRequestBody =
        json.toJsonUnchecked(
            ImmutableMap.of(
                "base", "basebranch",
                "head", "headbranch"));
    when(github.post("/repos/someowner/somerepo/merges", expectedRequestBody))
        .thenReturn(okResponse);
    final CommitItem commit = repoClient.merge("basebranch", "headbranch").join().get();

    assertThat(commit.parents().size(), is(2));
    assertThat(commit.parents().get(0).sha(), is("553c2077f0edc3d5dc5d17262f6aa498e69d6f8e"));
    assertThat(commit.parents().get(1).sha(), is("762941318ee16e59dabbacb1b4049eec22f0d303"));
  }

  @Test
  public void createFork() throws IOException {
    CompletableFuture<Response> okResponse =
        completedFuture(
            new Response.Builder()
                .request(new Request.Builder().url("http://example.com/whatever").build())
                .protocol(Protocol.HTTP_1_1)
                .message("")
                .code(202)
                .body(
                    ResponseBody.create(
                        MediaType.get("application/json"), getFixture("fork_create_item.json")))
                .build());
    final String expectedRequestBody = json.toJsonUnchecked(ImmutableMap.of());
    when(github.post("/repos/someowner/somerepo/forks", expectedRequestBody))
        .thenReturn(okResponse);

    final Repository repo = repoClient.createFork(null).join();
    assertThat(repo.id(), is(1296269));
  }

  @Test
  public void mergeNoop() {
    CompletableFuture<Response> okResponse =
        completedFuture(
            new Response.Builder()
                .request(new Request.Builder().url("http://example.com/whatever").build())
                .protocol(Protocol.HTTP_1_1)
                .message("")
                .code(204) // No Content
                .build());
    when(github.post(any(), any())).thenReturn(okResponse);
    final Optional<CommitItem> maybeCommit = repoClient.merge("basebranch", "headbranch").join();
    assertThat(maybeCommit, is(Optional.empty()));
  }

  @Test
  public void shouldDownloadTarball() throws Exception {
    CompletableFuture<Response> fixture =
        completedFuture(
            new Response.Builder()
                .request(new Request.Builder().url("https://example.com/whatever").build())
                .protocol(Protocol.HTTP_1_1)
                .message("")
                .code(200)
                .body(
                    ResponseBody.create(
                        "some bytes".getBytes(StandardCharsets.UTF_8),
                        MediaType.get("application/gzip")))
                .build());
    when(github.request("/repos/someowner/somerepo/tarball/")).thenReturn(fixture);

    try (InputStream response = repoClient.downloadTarball().get().orElseThrow()) {
      String result = new String(response.readAllBytes(), StandardCharsets.UTF_8);
      assertThat(result, is("some bytes"));
    }
  }

  @Test
  public void shouldDownloadZipball() throws Exception {
    CompletableFuture<Response> fixture =
        completedFuture(
            new Response.Builder()
                .request(new Request.Builder().url("https://example.com/whatever").build())
                .protocol(Protocol.HTTP_1_1)
                .message("")
                .code(200)
                .body(
                    ResponseBody.create(
                        "some bytes".getBytes(StandardCharsets.UTF_8),
                        MediaType.get("application/gzip")))
                .build());
    when(github.request("/repos/someowner/somerepo/zipball/")).thenReturn(fixture);

    try (InputStream response = repoClient.downloadZipball().get().orElseThrow()) {
      String result = new String(response.readAllBytes(), StandardCharsets.UTF_8);
      assertThat(result, is("some bytes"));
    }
  }

  @Test
  public void shouldReturnEmptyOptionalWhenResponseBodyNotPresent() throws Exception {
    CompletableFuture<Response> fixture =
        completedFuture(
            new Response.Builder()
                .request(new Request.Builder().url("https://example.com/whatever").build())
                .protocol(Protocol.HTTP_1_1)
                .message("")
                .code(204) // No Content
                .build());
    when(github.request("/repos/someowner/somerepo/zipball/master")).thenReturn(fixture);

    Optional<InputStream> response = repoClient.downloadZipball("master").get();
    assertThat(response, is(Optional.empty()));
  }

  @Test
  public void shouldReturnEmptyResponseWhenRepositoryDispatchEndpointTriggered() throws Exception {
    final Response response = mock(Response.class);
    when(response.code()).thenReturn(204);

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode clientPayload = mapper.createObjectNode();
    clientPayload.put("my-custom-true-property","true");
    clientPayload.put("my-custom-false-property", "false");

    RepositoryDispatch repositoryDispatchRequest = ImmutableRepositoryDispatch.builder()
        .eventType("my-custom-event")
        .clientPayload(clientPayload)
        .build();

    when(github.post("/repos/someowner/somerepo/dispatches", json.toJsonUnchecked(repositoryDispatchRequest))).thenReturn(completedFuture(response));

    boolean repoDispatchResult = repoClient.createRepositoryDispatchEvent(repositoryDispatchRequest).get();
    assertTrue(repoDispatchResult);
  }

}
