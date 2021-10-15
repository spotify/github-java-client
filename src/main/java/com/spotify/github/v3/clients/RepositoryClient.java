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

import static com.spotify.github.v3.clients.GitHubClient.IGNORE_RESPONSE_CONSUMER;
import static com.spotify.github.v3.clients.GitHubClient.LIST_BRANCHES;
import static com.spotify.github.v3.clients.GitHubClient.LIST_COMMIT_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_FOLDERCONTENT_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_REPOSITORY;
import static com.spotify.github.v3.clients.GitHubClient.LIST_STATUS_TYPE_REFERENCE;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.spotify.github.Tracer;
import com.spotify.github.async.AsyncPage;
import com.spotify.github.v3.comment.Comment;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import com.spotify.github.v3.git.Tree;
import com.spotify.github.v3.hooks.requests.WebhookCreate;
import com.spotify.github.v3.repos.Branch;
import com.spotify.github.v3.repos.Commit;
import com.spotify.github.v3.repos.CommitComparison;
import com.spotify.github.v3.repos.CommitItem;
import com.spotify.github.v3.repos.CommitStatus;
import com.spotify.github.v3.repos.Content;
import com.spotify.github.v3.repos.FolderContent;
import com.spotify.github.v3.repos.Languages;
import com.spotify.github.v3.repos.Repository;
import com.spotify.github.v3.repos.Status;
import com.spotify.github.v3.repos.requests.AuthenticatedUserRepositoriesFilter;
import com.spotify.github.v3.repos.requests.RepositoryCreateStatus;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Repository API client */
public class RepositoryClient {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int CONFLICT = 409;
  private static final int UNPROCESSABLE_ENTITY = 422;
  private static final int NO_CONTENT = 204;

  private static final String REPOSITORY_URI_TEMPLATE = "/repos/%s/%s";
  private static final String HOOK_URI_TEMPLATE = "/repos/%s/%s/hooks";
  private static final String CONTENTS_URI_TEMPLATE = "/repos/%s/%s/contents/%s%s";
  public static final String STATUS_URI_TEMPLATE = "/repos/%s/%s/statuses/%s";
  private static final String COMMITS_URI_TEMPLATE = "/repos/%s/%s/commits";
  private static final String COMMIT_SHA_URI_TEMPLATE = "/repos/%s/%s/commits/%s";
  private static final String COMMIT_STATUS_URI_TEMPLATE = "/repos/%s/%s/commits/%s/status";
  private static final String TREE_SHA_URI_TEMPLATE = "/repos/%s/%s/git/trees/%s";
  private static final String COMPARE_COMMIT_TEMPLATE = "/repos/%s/%s/compare/%s...%s";
  private static final String BRANCH_TEMPLATE = "/repos/%s/%s/branches/%s";
  private static final String LIST_BRANCHES_TEMPLATE = "/repos/%s/%s/branches";
  private static final String CREATE_COMMENT_TEMPLATE = "/repos/%s/%s/commits/%s/comments";
  private static final String COMMENT_TEMPLATE = "/repos/%s/%s/comments/%s";
  private static final String LANGUAGES_TEMPLATE = "/repos/%s/%s/languages";
  private static final String MERGE_TEMPLATE = "/repos/%s/%s/merges";
  private static final String FORK_TEMPLATE = "/repos/%s/%s/forks";
  private static final String LIST_REPOSITORY_TEMPLATE = "/orgs/%s/repos";
  private static final String LIST_REPOSITORIES_FOR_AUTHENTICATED_USER = "/user/repos";
  private static final String IS_USER_COLLABORATOR_OF_REPO = "/repos/%s/%s/collaborators/%s";
  private final String owner;
  private final String repo;
  private final GitHubClient github;
  private Tracer tracer = NoopTracer.INSTANCE;

  RepositoryClient(final GitHubClient github, final String owner, final String repo) {
    this.github = github;
    this.owner = owner;
    this.repo = repo;
  }

  static RepositoryClient create(final GitHubClient github, final String owner, final String repo) {
    return new RepositoryClient(github, owner, repo);
  }

  public RepositoryClient withTracer(Tracer tracer) {
    this.tracer = requireNonNull(tracer);
    return this;
  }

  /**
   * Create an issue API client.
   *
   * @return issue API client
   */
  public IssueClient createIssueClient() {
    return IssueClient.create(github, owner, repo);
  }

  /**
   * Create a pull request API client.
   *
   * @return pull request API client
   */
  public PullRequestClient createPullRequestClient() {
    return PullRequestClient.create(github, owner, repo);
  }

  /**
   * Create Github App API client
   *
   * @return Github App API client
   */
  public GithubAppClient createGithubAppClient() {
    return new GithubAppClient(github, owner, repo);
  }

  /**
   * Create a checks API client
   *
   * @return repository API client
   */
  public ChecksClient createChecksApiClient() {
    if (!github.getPrivateKey().isPresent()) {
      throw new IllegalArgumentException("Checks Client needs a private key");
    }
    return new ChecksClient(github, owner, repo);
  }

  /**
   * Get information about this repository.
   *
   * @return repository information
   */
  public CompletableFuture<Repository> getRepository() {
    final String path = String.format(REPOSITORY_URI_TEMPLATE, owner, repo);
    CompletableFuture<Repository> future = github.request(path, Repository.class);
    tracer.span("Get repository", future);
    return future;
  }

  /**
   * List all repositories in this organization.
   *
   * @return list of all repositories under organization
   */
  public CompletableFuture<List<Repository>> listOrganizationRepositories() {
    final String path = String.format(LIST_REPOSITORY_TEMPLATE, owner);
    CompletableFuture<List<Repository>> future = github.request(path, LIST_REPOSITORY);
    tracer.span("List repositories", future);
    return future;
  }

  /**
   * List repositories for the authenticated user.
   *
   * @param filter filter parameters
   * @return list of repositories for the authenticated user
   */
  public Iterator<AsyncPage<Repository>> listAuthenticatedUserRepositories(
      final AuthenticatedUserRepositoriesFilter filter) {
    final String serial = filter.serialize();
    final String path =
        LIST_REPOSITORIES_FOR_AUTHENTICATED_USER
            + (Strings.isNullOrEmpty(serial) ? "" : "?" + serial);
    return new GithubPageIterator<>(new GithubPage<>(github, path, LIST_REPOSITORY));
  }

  /**
   * Check if a user is collaborator of the repo.
   *
   * @param user the user to check
   * @return boolean indicating if user is collaborator
   */
  public CompletableFuture<Boolean> isCollaborator(final String user) {
    final String path = String.format(IS_USER_COLLABORATOR_OF_REPO, owner, repo, user);
    CompletableFuture<Boolean> future = github.request(path).thenApply(response -> response.code() == NO_CONTENT);
    tracer.span("isCollaborator", future);
    return future;
  }

  /**
   * Create a webhook.
   *
   * @param request create request
   * @param ignoreExisting if true hook exists errors will be ignored
   */
  public CompletableFuture<Void> createWebhook(
      final WebhookCreate request, final boolean ignoreExisting) {
    final String path = String.format(HOOK_URI_TEMPLATE, owner, repo);
    CompletableFuture<Void> future = github
        .post(path, github.json().toJsonUnchecked(request))
        .thenAccept(IGNORE_RESPONSE_CONSUMER)
        .exceptionally(
            e -> {
              if (e instanceof RequestNotOkException) {
                final RequestNotOkException e1 = (RequestNotOkException) e;
                int code = e1.statusCode();

                if (ignoreExisting && (code == CONFLICT || code == UNPROCESSABLE_ENTITY)) {
                  log.debug("Webhook {} for {} already exists, ignoring.", request.name(), this);
                  return null;
                }

                throw new RequestNotOkException(
                    e1.path(), e1.statusCode(), "Failed creating a webhook: " + request, e);
              }

              throw new CompletionException(e);
            });
    tracer.span("Create webhook", future);
    return future;
  }

  /**
   * Set status for a given commit.
   *
   * @param sha the commit sha to set the status for
   * @param request The body of the request to sent to github to create a commit status
   */
  public CompletableFuture<Void> setCommitStatus(
      final String sha, final RepositoryCreateStatus request) {
    final String path = String.format(STATUS_URI_TEMPLATE, owner, repo, sha);
    CompletableFuture<Void> future = github
        .post(path, github.json().toJsonUnchecked(request))
        .thenAccept(IGNORE_RESPONSE_CONSUMER);
    tracer.span("Set commit status", future);
    return future;
  }

  /**
   * Get status for a given commit.
   *
   * @param ref ref can be a sha, branch or tag name
   */
  public CompletableFuture<CommitStatus> getCommitStatus(final String ref) {
    final String path = String.format(COMMIT_STATUS_URI_TEMPLATE, owner, repo, ref);
    CompletableFuture<CommitStatus> future = github.request(path, CommitStatus.class);
    tracer.span("Get commit status", future);
    return future;
  }

  /**
   * List statuses for a specific ref. Statuses are returned in reverse chronological order. The
   * first status in the list will be the latest one.
   *
   * @param sha the commit sha to list the statuses for
   */
  public CompletableFuture<List<Status>> listCommitStatuses(final String sha) {
    final String path = String.format(STATUS_URI_TEMPLATE, owner, repo, sha);
    CompletableFuture<List<Status>> future = github.request(path, LIST_STATUS_TYPE_REFERENCE);
    tracer.span("List commit statuses", future);
    return future;
  }

  /**
   * List statuses for a specific ref. Statuses are returned in reverse chronological order. The
   * first status in the list will be the latest one.
   *
   * @param sha the commit sha to list the statuses for
   * @param itemsPerPage number of items per page
   * @return iterator of Status
   */
  public Iterator<AsyncPage<Status>> listCommitStatuses(final String sha, final int itemsPerPage) {
    // FIXME Use itemsPerPage property
    final String path = String.format(STATUS_URI_TEMPLATE, owner, repo, sha);
    log.debug("Fetching commits from " + path);
    return new GithubPageIterator<>(new GithubPage<>(github, path, LIST_STATUS_TYPE_REFERENCE));
  }

  /**
   * List repository commits.
   *
   * @return commits
   */
  public CompletableFuture<List<CommitItem>> listCommits() {
    final String path = String.format(COMMITS_URI_TEMPLATE, owner, repo);
    CompletableFuture<List<CommitItem>> future = github.request(path, LIST_COMMIT_TYPE_REFERENCE);
    tracer.span("List commits", future);
    return future;
  }

  /**
   * Get a repository commit.
   *
   * @param sha commit sha
   * @return commit
   */
  public CompletableFuture<Commit> getCommit(final String sha) {
    final String path = String.format(COMMIT_SHA_URI_TEMPLATE, owner, repo, sha);
    CompletableFuture<Commit> future = github.request(path, Commit.class);
    tracer.span("Get commit", future);
    return future;
  }

  /**
   * Get a repository tree.
   *
   * @deprecated Use {@link com.spotify.github.v3.clients.GitDataClient#getTree(String)} instead
   * @param sha commit sha
   * @return tree
   */
  @Deprecated
  public CompletableFuture<Tree> getTree(final String sha) {
    final String path = String.format(TREE_SHA_URI_TEMPLATE, owner, repo, sha);
    CompletableFuture<Tree> future =  github.request(path, Tree.class);
    tracer.span("Get tree", future);
    return future;
  }

  /**
   * Get repository contents of a file.
   *
   * @param path path to a file
   * @return content
   */
  public CompletableFuture<Content> getFileContent(final String path) {
    CompletableFuture<Content> future = github.request(getContentPath(path, ""), Content.class);
    tracer.span("Get file content", future);
    return future;
  }

  /**
   * Get repository contents of a file.
   *
   * @param path path to a file
   * @param ref name of the commit/branch/tag
   * @return content
   */
  public CompletableFuture<Content> getFileContent(final String path, final String ref) {
    CompletableFuture<Content> future =  github.request(getContentPath(path, "?ref=" + ref), Content.class);
    tracer.span("Get file content", future);
    return future;
  }

  /**
   * Get repository contents of a folder.
   *
   * @param path path to a folder
   * @return content
   */
  public CompletableFuture<List<FolderContent>> getFolderContent(final String path) {
    CompletableFuture<List<FolderContent>> future = github.request(getContentPath(path, ""), LIST_FOLDERCONTENT_TYPE_REFERENCE);
    tracer.span("Get folder content", future);
    return future;
  }

  /**
   * Create a comment for a given issue number.
   *
   * @param sha the commit sha to create the comment on
   * @param body comment content
   * @return the Comment that was just created
   */
  public CompletableFuture<Comment> createComment(final String sha, final String body) {
    final String path = String.format(CREATE_COMMENT_TEMPLATE, owner, repo, sha);
    final String requestBody = github.json().toJsonUnchecked(ImmutableMap.of("body", body));
    CompletableFuture<Comment> future = github.post(path, requestBody, Comment.class);
    tracer.span("Create comment", future);
    return future;
  }

  /**
   * Get a specific comment.
   *
   * @param id comment id
   * @return a comment
   */
  public CompletableFuture<Comment> getComment(final int id) {
    final String path = String.format(COMMENT_TEMPLATE, owner, repo, id);
    CompletableFuture<Comment> future = github.request(path, Comment.class);
    tracer.span("Get comment", future);
    return future;
  }

  /**
   * Get repository contents of a folder.
   *
   * @param path path to a folder
   * @param ref name of the commit/branch/tag
   * @return content
   */
  public CompletableFuture<List<FolderContent>> getFolderContent(
      final String path, final String ref) {
    CompletableFuture<List<FolderContent>> future = github.request(getContentPath(path, "?ref=" + ref), LIST_FOLDERCONTENT_TYPE_REFERENCE);
    tracer.span("Get folder content", future);
    return future;
  }

  /**
   * Compare two commits content.
   *
   * @param base the base commit
   * @param head the head commit
   * @return a CommitComparison object
   */
  public CompletableFuture<CommitComparison> compareCommits(final String base, final String head) {
    final String path = String.format(COMPARE_COMMIT_TEMPLATE, owner, repo, base, head);
    CompletableFuture<CommitComparison> future = github.request(path, CommitComparison.class);
    tracer.span("Compare commits", future);
    return future;
  }

  /**
   * Get a specific branch.
   *
   * @param branch the branch name
   * @return a Branch
   */
  public CompletableFuture<Branch> getBranch(final String branch) {
    final String path = String.format(BRANCH_TEMPLATE, owner, repo, branch);
    CompletableFuture<Branch> future = github.request(path, Branch.class);
    tracer.span("Get branch", future);
    return future;
  }

  /**
   * Get a specific branch.
   *
   * @return list of all branches in repository
   */
  public CompletableFuture<List<Branch>> listBranches() {
    final String path = String.format(LIST_BRANCHES_TEMPLATE, owner, repo);
    CompletableFuture<List<Branch>> future = github.request(path, LIST_BRANCHES);
    tracer.span("List branches", future);
    return future;
  }

  /**
   * Delete a comment for a given id.
   *
   * @param id the commit id to be deleted
   */
  public CompletableFuture<Void> deleteComment(final int id) {
    final String path = String.format(COMMENT_TEMPLATE, owner, repo, id);
    CompletableFuture<Void> future = github.delete(path).thenAccept(IGNORE_RESPONSE_CONSUMER);
    tracer.span("Delete comment", future);
    return future;
  }

  /**
   * Edit a comment for a given id.
   *
   * @param id the commit id to be edited
   * @param body comment content
   */
  public CompletableFuture<Void> editComment(final int id, final String body) {
    final String path = String.format(COMMENT_TEMPLATE, owner, repo, id);
    CompletableFuture<Void> future = github
        .patch(path, github.json().toJsonUnchecked(ImmutableMap.of("body", body)))
        .thenAccept(IGNORE_RESPONSE_CONSUMER);
    tracer.span("Edit comment", future);
    return future;
  }

  /**
   * Get repository language stats.
   *
   * @return {@link Languages Languages}
   */
  public CompletableFuture<Languages> getLanguages() {
    final String path = String.format(LANGUAGES_TEMPLATE, owner, repo);
    CompletableFuture<Languages> future = github.request(path, Languages.class);
    tracer.span("Get languages", future);
    return future;
  }

  /**
   * Perform a merge.
   *
   * @see "https://developer.github.com/enterprise/2.18/v3/repos/merging/"
   * @param base branch name or sha
   * @param head branch name or sha
   * @return resulting merge commit, or empty if base already contains the head (nothing to merge)
   */
  public CompletableFuture<Optional<CommitItem>> merge(final String base, final String head) {
    CompletableFuture<Optional<CommitItem>> future = merge(base, head, null);
    tracer.span("Merge", future);
    return future;
  }

  /**
   * Perform a merge.
   *
   * @see "https://developer.github.com/enterprise/2.18/v3/repos/merging/"
   * @param base branch name that the head will be merged into
   * @param head branch name or sha to merge
   * @param commitMessage commit message to use for the merge commit
   * @return resulting merge commit, or empty if base already contains the head (nothing to merge)
   */
  public CompletableFuture<Optional<CommitItem>> merge(
      final String base, final String head, final String commitMessage) {
    final String path = String.format(MERGE_TEMPLATE, owner, repo);
    final ImmutableMap<String, String> params =
        (commitMessage == null)
            ? ImmutableMap.of("base", base, "head", head)
            : ImmutableMap.of("base", base, "head", head, "commit_message", commitMessage);
    final String body = github.json().toJsonUnchecked(params);

    CompletableFuture<Optional<CommitItem>> future = github
        .post(path, body)
        .thenApply(
            response -> {
              // Non-successful statuses result in an RequestNotOkException exception and this code
              // not being called.

              if (response.code() == NO_CONTENT) {
                // Base already contains the head, nothing to merge
                return Optional.empty();
              }
              final CommitItem commitItem =
                  github
                      .json()
                      .fromJsonUnchecked(
                          GitHubClient.responseBodyUnchecked(response), CommitItem.class);
              return Optional.of(commitItem);
            });
    tracer.span("Merge", future);
    return future;
  }

  /**
   * Create a fork.
   *
   * @see "https://developer.github.com/v3/repos/forks/#create-a-fork"
   * @param organization the organization where the fork will be created
   * @return resulting repository
   */
  public CompletableFuture<Repository> createFork(final String organization) {
    final String path = String.format(FORK_TEMPLATE, owner, repo);
    final ImmutableMap<String, String> params =
        (organization == null) ? ImmutableMap.of() : ImmutableMap.of("organization", organization);
    final String body = github.json().toJsonUnchecked(params);

    CompletableFuture<Repository> future = github
        .post(path, body)
        .thenApply(
            response -> {
              final Repository repositoryItem =
                  github
                      .json()
                      .fromJsonUnchecked(
                          GitHubClient.responseBodyUnchecked(response), Repository.class);
              return repositoryItem;
            });
    tracer.span("Create fork", future);
    return future;
  }

  private String getContentPath(final String path, final String query) {
    if (path.startsWith("/") || path.endsWith("/")) {
      throw new IllegalArgumentException(path + " starts or ends with '/'");
    }
    return String.format(CONTENTS_URI_TEMPLATE, owner, repo, path, query);
  }
}
