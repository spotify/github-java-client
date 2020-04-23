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
import static com.spotify.github.v3.clients.GitHubClient.LIST_COMMIT_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_FOLDERCONTENT_TYPE_REFERENCE;
import static com.spotify.github.v3.clients.GitHubClient.LIST_STATUS_TYPE_REFERENCE;

import com.google.common.collect.ImmutableMap;
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
import com.spotify.github.v3.repos.Repository;
import com.spotify.github.v3.repos.Status;
import com.spotify.github.v3.repos.requests.RepositoryCreateStatus;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Repository API client */
public class RepositoryClient {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int CONFLICT = 409;
  private static final int UNPROCESSABLE_ENTITY = 422;
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
  private static final String CREATE_COMMENT_TEMPLATE = "/repos/%s/%s/commits/%s/comments";
  private static final String COMMENT_TEMPLATE = "/repos/%s/%s/comments/%s";

  private final String owner;
  private final String repo;
  private final GitHubClient github;

  RepositoryClient(final GitHubClient github, final String owner, final String repo) {
    this.github = github;
    this.owner = owner;
    this.repo = repo;
  }

  static RepositoryClient create(final GitHubClient github, final String owner, final String repo) {
    return new RepositoryClient(github, owner, repo);
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
    return github.request(path, Repository.class);
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

    return github
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
    return github
        .post(path, github.json().toJsonUnchecked(request))
        .thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * Get status for a given commit.
   *
   * @param ref ref can be a sha, branch or tag name
   */
  public CompletableFuture<CommitStatus> getCommitStatus(final String ref) {
    final String path = String.format(COMMIT_STATUS_URI_TEMPLATE, owner, repo, ref);
    return github.request(path, CommitStatus.class);
  }

  /**
   * List statuses for a specific ref. Statuses are returned in reverse chronological order.
   * The first status in the list will be the latest one.
   *
   * @param sha the commit sha to list the statuses for
   */
  public CompletableFuture<List<Status>> listCommitStatuses(final String sha) {
    final String path = String.format(STATUS_URI_TEMPLATE, owner, repo, sha);
    return github.request(path, LIST_STATUS_TYPE_REFERENCE);
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
    return github.request(path, LIST_COMMIT_TYPE_REFERENCE);
  }

  /**
   * Get a repository commit.
   *
   * @param sha commit sha
   * @return commit
   */
  public CompletableFuture<Commit> getCommit(final String sha) {
    final String path = String.format(COMMIT_SHA_URI_TEMPLATE, owner, repo, sha);
    return github.request(path, Commit.class);
  }

  /**
   * Get a repository tree.
   *
   * @param sha commit sha
   * @return tree
   */
  public CompletableFuture<Tree> getTree(final String sha) {
    final String path = String.format(TREE_SHA_URI_TEMPLATE, owner, repo, sha);
    return github.request(path, Tree.class);
  }

  /**
   * Get repository contents of a file.
   *
   * @param path path to a file
   * @return content
   */
  public CompletableFuture<Content> getFileContent(final String path) {
    return github.request(getContentPath(path, ""), Content.class);
  }

  /**
   * Get repository contents of a file.
   *
   * @param path path to a file
   * @param ref name of the commit/branch/tag
   * @return content
   */
  public CompletableFuture<Content> getFileContent(final String path, final String ref) {
    return github.request(getContentPath(path, "?ref=" + ref), Content.class);
  }

  /**
   * Get repository contents of a folder.
   *
   * @param path path to a folder
   * @return content
   */
  public CompletableFuture<List<FolderContent>> getFolderContent(final String path) {
    return github.request(getContentPath(path, ""), LIST_FOLDERCONTENT_TYPE_REFERENCE);
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
    return github.post(path, requestBody, Comment.class);
  }

  /**
   * Get a specific comment.
   *
   * @param id comment id
   * @return a comment
   */
  public CompletableFuture<Comment> getComment(final int id) {
    final String path = String.format(COMMENT_TEMPLATE, owner, repo, id);
    return github.request(path, Comment.class);
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
    return github.request(getContentPath(path, "?ref=" + ref), LIST_FOLDERCONTENT_TYPE_REFERENCE);
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
    return github.request(path, CommitComparison.class);
  }

  /**
   * Get a specific branch.
   *
   * @param branch the branch name
   * @return a Branch
   */
  public CompletableFuture<Branch> getBranch(final String branch) {
    final String path = String.format(BRANCH_TEMPLATE, owner, repo, branch);
    return github.request(path, Branch.class);
  }

  /**
   * Delete a comment for a given id.
   *
   * @param id the commit id to be deleted
   */
  public CompletableFuture<Void> deleteComment(final int id) {
    final String path = String.format(COMMENT_TEMPLATE, owner, repo, id);
    return github.delete(path).thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * Edit a comment for a given id.
   *
   * @param id the commit id to be edited
   * @param body comment content
   */
  public CompletableFuture<Void> editComment(final int id, final String body) {
    final String path = String.format(COMMENT_TEMPLATE, owner, repo, id);
    return github
        .patch(path, github.json().toJsonUnchecked(ImmutableMap.of("body", body)))
        .thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  private String getContentPath(final String path, final String query) {
    if (path.startsWith("/") || path.endsWith("/")) {
      throw new IllegalArgumentException(path + " starts or ends with '/'");
    }
    return String.format(CONTENTS_URI_TEMPLATE, owner, repo, path, query);
  }
}
