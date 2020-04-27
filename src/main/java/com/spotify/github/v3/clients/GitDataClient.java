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
import static java.lang.String.format;

import com.google.common.collect.ImmutableMap;
import com.spotify.github.v3.git.Reference;
import com.spotify.github.v3.git.Tag;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/** Reference Api client */
public class GitDataClient {

  private static final String BRANCH_REFERENCE_URI = "/repos/%s/%s/git/refs/heads/%s";
  private static final String TAG_REFERENCE_URI = "/repos/%s/%s/git/refs/tags/%s";
  private static final String TAG_URI = "/repos/%s/%s/git/tags/%s";
  private static final String CREATE_REFERENCE_URI = "/repos/%s/%s/git/refs";
  private static final String CREATE_REFERENCE_TAG = "/repos/%s/%s/git/tags";
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
  public CompletableFuture<Void> delete(final String ref) {
    final String path =
        format("/repos/%s/%s/git/refs/heads/%s", owner, repo, ref.replaceAll("refs/heads/", ""));
    return github.delete(path).thenAccept(IGNORE_RESPONSE_CONSUMER);
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
   * Create a git branch reference. It must not include the refs/heads.
   *
   * @param branch tag name
   * @param sha commit to branch from
   */
  public CompletableFuture<Reference> createBranchReference(final String branch, final String sha) {
    final String path = format(CREATE_REFERENCE_URI, owner, repo);
    final ImmutableMap<String, String> body = of(
        "ref", format("refs/heads/%s", branch),
        "sha", sha
    );
    return github.post(path, github.json().toJsonUnchecked(body), Reference.class);
  }

  /**
   * Create a git tag reference. It must not include the refs/tags.
   *
   * @param tag tag name
   * @param sha commit to tag
   */
  public CompletableFuture<Reference> createTagReference(final String tag, final String sha) {
    final String path = format(CREATE_REFERENCE_URI, owner, repo);
    final ImmutableMap<String, String> body = of("ref", format("refs/tags/%s", tag), "sha", sha);
    return github.post(path, github.json().toJsonUnchecked(body), Reference.class);
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
}
