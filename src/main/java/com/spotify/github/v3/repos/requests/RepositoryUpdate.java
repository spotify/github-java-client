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

package com.spotify.github.v3.repos.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableRepositoryUpdate.class)
@JsonDeserialize(as = ImmutableRepositoryUpdate.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface RepositoryUpdate {

  /** Description */
  Optional<String> description();

  /** Allow auto merges */
  Optional<Boolean> allowAutoMerge();

  /**
   * Either true to allow private forks, or false to prevent private forks.
   *
   * <p>Default: false
   */
  Optional<Boolean> allowForking();

  /** Allow squash merges */
  Optional<Boolean> allowSquashMerge();

  /** Allow merge commits */
  Optional<Boolean> allowMergeCommit();

  /** Allow rebase merges */
  Optional<Boolean> allowRebaseMerge();

  /**
   * Either true to always allow a pull request head branch that is behind its base branch to be
   * updated even if it is not required to be up to date before merging, or false otherwise.
   *
   * <p>Default: false
   */
  Optional<Boolean> allowUpdateBranch();

  /** Updates the default branch for this repository. */
  Optional<String> defaultBranch();

  /**
   * Either true to allow automatically deleting head branches when pull requests are merged, or
   * false to prevent automatic deletion.
   *
   * <p>Default: false
   */
  Optional<Boolean> deleteBranchOnMerge();

  /** Homepage URL */
  Optional<String> homepage();

  /** Does it have downloads */
  Optional<Boolean> hasDownloads();

  /** Does it have issues */
  Optional<Boolean> hasIssues();

  /** Does it have wiki */
  Optional<Boolean> hasWiki();

  /** Does it have pages */
  Optional<Boolean> hasPages();

  /** Does it have projects */
  Optional<Boolean> hasProjects();

  /**
   * Whether to archive this repository. false will unarchive a previously archived repository.
   *
   * <p>Default: false
   */
  @JsonProperty("archived")
  Optional<Boolean> isArchived();

  /** Is it private */
  @JsonProperty("private")
  Optional<Boolean> isPrivate();

  /**
   * Either true to make this repo available as a template repository or false to prevent it.
   * Default: false
   */
  Optional<Boolean> isTemplate();

  /**
   * The default value for a squash merge commit message:
   *
   * <p>PR_BODY - default to the pull request's body. COMMIT_MESSAGES - default to the branch's
   * commit messages. BLANK - default to a blank commit message. Can be one of: PR_BODY,
   * COMMIT_MESSAGES, BLANK
   */
  Optional<String> squashMergeCommitMessage();

  /**
   * squash_merge_commit_title string The default value for a squash merge commit title:
   *
   * <p>PR_TITLE - default to the pull request's title. COMMIT_OR_PR_TITLE - default to the commit's
   * title (if only one commit) or the pull request's title (when more than one commit). Can be one
   * of: PR_TITLE, COMMIT_OR_PR_TITLE
   */
  Optional<String> squashMergeCommitTitle();

  /**
   * The default value for a merge commit message.
   *
   * <p>PR_TITLE - default to the pull request's title. PR_BODY - default to the pull request's
   * body. BLANK - default to a blank commit message.
   */
  Optional<String> mergeCommitMessage();

  /**
   * The default value for a merge commit title.
   *
   * <p>PR_TITLE - default to the pull request's title. MERGE_MESSAGE - default to the classic title
   * for a merge message (e.g., Merge pull request #123 from branch-name). Can be one of: PR_TITLE,
   * MERGE_MESSAGE
   */
  Optional<String> mergeCommitTitle();

  /**
   * The id of the team that will be granted access to this repository. This is only valid when
   * creating a repository in an organization. Default: false
   */
  Optional<Integer> teamId();

  /** The visibility of the repo. Can be one of `public`, `private`, `internal` */
  Optional<String> visibility();

  /**
   * Either true to require contributors to sign off on web-based commits, or false to not require
   * contributors to sign off on web-based commits.
   *
   * <p>Default: false
   */
  Optional<Boolean> webCommitSignoffRequired();
}
