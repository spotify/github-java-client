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
import static com.spotify.github.v3.clients.GitHubClient.IGNORE_RESPONSE_CONSUMER;
import static com.spotify.github.v3.clients.GitHubClient.LIST_REFERENCES;
import static java.lang.String.format;

import com.google.common.collect.ImmutableMap;
import com.spotify.github.v3.git.Reference;
import com.spotify.github.v3.git.ShaLink;
import com.spotify.github.v3.git.Tag;
import com.spotify.github.v3.git.Tree;
import com.spotify.github.v3.git.TreeItem;
import com.spotify.github.v3.repos.Commit;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Reference Api client */
public class GitDataClient {

  private static final String REFERENCE_URI = "/repos/%s/%s/git/refs/%s";
  private static final String BRANCH_REFERENCE_URI = "/repos/%s/%s/git/refs/heads/%s";
  private static final String TAG_REFERENCE_URI = "/repos/%s/%s/git/refs/tags/%s";
  private static final String TAG_URI = "/repos/%s/%s/git/tags/%s";
  private static final String CREATE_REFERENCE_URI = "/repos/%s/%s/git/refs";
  private static final String CREATE_REFERENCE_TAG = "/repos/%s/%s/git/tags";
  private static final String LIST_MATCHING_REFERENCES_URI = "/repos/%s/%s/git/matching-refs/%s";

  private static final String CREATE_COMMIT_URI_TEMPLATE = "/repos/%s/%s/git/commits";

  private static final String TREE_SHA_URI_TEMPLATE = "/repos/%s/%s/git/trees/%s";
  private static final String TREE_URI_TEMPLATE = "/repos/%s/%s/git/trees";

  private static final String BLOB_URI_TEMPLATE = "/repos/%s/%s/git/blobs";

  private final GitHubClient github;
  private final String owner;
  private final String repo;

  GitDataClient(final GitHubClient github, final String owner, final String repo) {
    this.github = github;
    this.owner = owner;
    this.repo = repo;
  }

  static GitDataClient create(final GitHubClient github, final String owner, final String repo) {
    return new GitDataClient(github, owner, repo);
  }

  /**
   * Deletes a git reference.
   *
   * @param ref search parameters
   */
  public CompletableFuture<Void> deleteReference(final String ref) {
    final String path = format(REFERENCE_URI, owner, repo, ref.replaceAll("refs/", ""));
    return github.delete(path).thenAccept(IGNORE_RESPONSE_CONSUMER);
  }

  /**
   * Deletes a git branch.
   *
   * @param branch search parameters
   */
  public CompletableFuture<Void> deleteBranch(final String branch) {
    return deleteReference("heads/" + branch.replaceAll("refs/heads/", ""));
  }

  /**
   * Deletes a git tag.
   *
   * @param tag search parameters
   */
  public CompletableFuture<Void> deleteTag(final String tag) {
    return deleteReference("tags/" + tag.replaceAll("refs/tags/", ""));
  }

  /**
   * Get a git branch reference
   *
   * @param branch branch name
   */
  public CompletableFuture<Reference> getBranchReference(final String branch) {
    final String path = format(BRANCH_REFERENCE_URI, owner, repo, branch);
    return github.request(path, Reference.class);
  }

  /**
   * Get a git tag reference.
   *
   * @param tag tag name
   */
  public CompletableFuture<Reference> getTagReference(final String tag) {
    final String path = format(TAG_REFERENCE_URI, owner, repo, tag);
    return github.request(path, Reference.class);
  }

  /**
   * Get a git annotated tag.
   *
   * @param tag tag name
   */
  public CompletableFuture<Tag> getTag(final String tag) {
    final String path = format(TAG_URI, owner, repo, tag);
    return github.request(path, Tag.class);
  }

  /**
   * List matching references.
   *
   * @param ref reference name
   */
  public CompletableFuture<List<Reference>> listMatchingReferences(final String ref) {
    final String path = format(LIST_MATCHING_REFERENCES_URI, owner, repo, ref);
    return github.request(path, LIST_REFERENCES);
  }

  /**
   * List references. (Replaced by listMatchingReferences for github enterprise version > 2.18)
   *
   * @param ref reference name
   */
  @Deprecated
  public CompletableFuture<List<Reference>> listReferences(final String ref) {
    final String path = format(REFERENCE_URI, owner, repo, ref.replaceAll("refs/", ""));
    return github.request(path, LIST_REFERENCES);
  }

  /** List references. (Replaced by listMatchingReferences for github enterprise version > 2.18) */
  @Deprecated
  public CompletableFuture<List<Reference>> listReferences() {
    return listReferences("");
  }

  /**
   * Create a git reference.
   *
   * @param ref reference name
   * @param sha commit to branch from
   */
  public CompletableFuture<Reference> createReference(final String ref, final String sha) {
    final String path = format(CREATE_REFERENCE_URI, owner, repo);
    final ImmutableMap<String, String> body =
        of(
            "ref", ref,
            "sha", sha);
    return github.post(path, github.json().toJsonUnchecked(body), Reference.class);
  }

  /**
   * Create a git branch reference. It must not include the refs/heads.
   *
   * @param branch tag name
   * @param sha commit to branch from
   */
  public CompletableFuture<Reference> createBranchReference(final String branch, final String sha) {
    return createReference(format("refs/heads/%s", branch), sha);
  }

  /**
   * Create a git tag reference. It must not include the refs/tags.
   *
   * @param tag tag name
   * @param sha commit to tag
   */
  public CompletableFuture<Reference> createTagReference(final String tag, final String sha) {
    return createReference(format("refs/tags/%s", tag), sha);
  }

  /**
   * Create an annotated tag. First it would create a tag reference and then create annotated tag
   *
   * @param tag tag name
   * @param sha commit to tag
   * @param tagMessage message
   * @param taggerName name of the tagger
   * @param taggerEmail email of the tagger
   */
  public CompletableFuture<Tag> createAnnotatedTag(
      final String tag,
      final String sha,
      final String tagMessage,
      final String taggerName,
      final String taggerEmail) {
    final String tagPath = format(CREATE_REFERENCE_TAG, owner, repo);
    final ImmutableMap<String, Object> body =
        of(
            "tag", tag,
            "message", tagMessage,
            "object", sha,
            "type", "commit",
            "tagger",
                of(
                    "name", taggerName,
                    "email", taggerEmail,
                    "date", Instant.now().toString()));
    return createTagReference(tag, sha)
        .thenCompose(
            reference -> github.post(tagPath, github.json().toJsonUnchecked(body), Tag.class));
  }

  /**
   * Create a commit which references a tree
   *
   * @param message commit message
   * @param parents list of parent sha values, usually just one sha
   * @param treeSha sha value of the tree
   */
  public CompletableFuture<Commit> createCommit(
      final String message, final List<String> parents, final String treeSha) {
    final String path = String.format(CREATE_COMMIT_URI_TEMPLATE, owner, repo);
    final String requestBody =
        github
            .json()
            .toJsonUnchecked(
                ImmutableMap.of("message", message, "parents", parents, "tree", treeSha));
    return github.post(path, requestBody, Commit.class);
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
   * Get a repository tree recursively.
   *
   * @param sha commit sha
   * @return tree
   */
  public CompletableFuture<Tree> getRecursiveTree(final String sha) {
    final String path = String.format(TREE_SHA_URI_TEMPLATE, owner, repo, sha);
    return github.request(path + "?recursive=true", Tree.class);
  }

  /**
   * Set a repository tree.
   *
   * @param tree     list of tree items
   * @param baseTreeSha sha of existing tree used as base for new tree
   * @return tree
   */
  public CompletableFuture<Tree> createTree(final List<TreeItem> tree, final String baseTreeSha) {
    final String path = String.format(TREE_URI_TEMPLATE, owner, repo);
    final String requestBody = github.json()
        .toJsonUnchecked(ImmutableMap.of("base_tree", baseTreeSha, "tree", tree));
    return github.post(path, requestBody, Tree.class);
  }


  /**
   * Post new content to the server.
   *
   * @param content the content to be posted
   */
  public CompletableFuture<ShaLink> createBlob(final String content) {
    final String path = String.format(BLOB_URI_TEMPLATE, owner, repo);
    final String encoding = "utf-8|base64";
    final String requestBody = github.json()
        .toJsonUnchecked(ImmutableMap.of("content", content, "encoding", encoding));
    return github.post(path, requestBody, ShaLink.class);
  }


}
