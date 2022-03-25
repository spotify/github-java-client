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

package com.spotify.github.v3.repos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GitHubInstant;
import com.spotify.github.GithubStyle;
import com.spotify.github.UpdateTracking;
import java.net.URI;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Repository resource */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableRepositoryBase.class)
@JsonDeserialize(as = ImmutableRepositoryBase.class)
public interface RepositoryBase extends UpdateTracking {

  /** ID */
  @Nullable
  Integer id();

  /** Name */
  @Nullable
  String name();

  /** Full name: org/repo */
  @Nullable
  String fullName();

  /** Description */
  Optional<String> description();

  /** Is it private */
  @Nullable
  @JsonProperty("private")
  Boolean isPrivate();

  /** Is it archived */
  @Nullable
  @JsonProperty("archived")
  Boolean isArchived();

  /** Is it public */
  @JsonProperty("public")
  Optional<Boolean> isPublic();

  /** Is it a fork */
  @Nullable
  Boolean fork();

  /** API URL */
  @Nullable
  URI url();

  /** HTML URL */
  @Nullable
  URI htmlUrl();

  /** Archive URL template */
  @Nullable
  String archiveUrl();

  /** Assignees URL template */
  @Nullable
  String assigneesUrl();

  /** Blobs URL template */
  @Nullable
  String blobsUrl();

  /** Branches URL template */
  @Nullable
  String branchesUrl();

  /** Clone URL */
  @Nullable
  URI cloneUrl();

  /** Collaborators URL template */
  @Nullable
  String collaboratorsUrl();

  /** Comments URL template */
  @Nullable
  String commentsUrl();

  /** Commits URL template */
  @Nullable
  String commitsUrl();

  /** Compare URL template */
  @Nullable
  String compareUrl();

  /** Contents URL template */
  @Nullable
  String contentsUrl();

  /** Contributors URL */
  @Nullable
  URI contributorsUrl();

  /** Deployments URL */
  Optional<URI> deploymentsUrl();

  /** Downloads URL */
  @Nullable
  URI downloadsUrl();

  /** Events URL */
  @Nullable
  URI eventsUrl();

  /** Forks URL */
  @Nullable
  URI forksUrl();

  /** Git commits URL template */
  @Nullable
  String gitCommitsUrl();

  /** Git references URL template */
  @Nullable
  String gitRefsUrl();

  /** Git tags URL template */
  @Nullable
  String gitTagsUrl();

  /** Git URL */
  @Nullable
  URI gitUrl();

  /** Hooks URL */
  @Nullable
  URI hooksUrl();

  /** Homepage URL */
  Optional<String> homepage();

  /** Language */
  Optional<String> language();

  /** Forks count */
  @Nullable
  Integer forksCount();

  /** Stargazers count */
  @Nullable
  Integer stargazersCount();

  /** Watchers count */
  @Nullable
  Integer watchersCount();

  /** Size in kB */
  @Nullable
  Integer size();

  /** Default branch */
  @Nullable
  String defaultBranch();

  /** Open issues count */
  @Nullable
  Integer openIssuesCount();

  /** Does it have issues */
  @Nullable
  Boolean hasIssues();

  /** Does it have wiki */
  @Nullable
  Boolean hasWiki();

  /** Does it have pages */
  @Nullable
  Boolean hasPages();

  /** Does it have downloads */
  @Nullable
  Boolean hasDownloads();

  /** Permissions */
  Optional<Permissions> permissions();

  /** Deprecated forks, {@link #forksCount()} */
  @Deprecated
  @Nullable
  Integer forks();

  /** Pushed date */
  @Nullable
  GitHubInstant pushedAt();

  /** Issues URL template */
  @Nullable
  String issuesUrl();

  /** Issue Comment URL template */
  @Nullable
  String issueCommentUrl();

  /** Issue event URL template */
  @Nullable
  String issueEventsUrl();

  /** Keys URL template */
  @Nullable
  String keysUrl();

  /** Labels URL template */
  @Nullable
  String labelsUrl();

  /** Languages URL */
  @Nullable
  URI languagesUrl();

  /** Merges URL */
  @Nullable
  URI mergesUrl();

  /** Milestones URL template */
  @Nullable
  String milestonesUrl();

  /** Mirror url, if this repo is a mirror */
  Optional<URI> mirrorUrl();

  /** Notifications URL template */
  @Nullable
  String notificationsUrl();

  /** Deprecated open issues, {@link #openIssuesCount()} */
  @Deprecated
  Optional<Integer> openIssues();

  /** Pulls URL template */
  @Nullable
  String pullsUrl();

  /** Releases URL template */
  @Nullable
  String releasesUrl();

  /** SSH URL. */
  @Nullable
  String sshUrl();

  /** Stargazers URL */
  @Nullable
  URI stargazersUrl();

  /** Statuses URL template */
  @Nullable
  String statusesUrl();

  /** Subscribers URL */
  @Nullable
  URI subscribersUrl();

  /** Subscription URL */
  @Nullable
  URI subscriptionUrl();

  /** Subversion URL */
  @Nullable
  URI svnUrl();

  /** Tags URL */
  @Nullable
  URI tagsUrl();

  /** Trees URL template */
  @Nullable
  String treesUrl();

  /** Teams URL */
  @Nullable
  String teamsUrl();

  /** Deprecated watchers, {@link #watchersCount()} */
  @Deprecated
  Optional<Integer> watchers();

  /** Deprecated stargazers, {@link #stargazersCount()} */
  @Deprecated
  Optional<Integer> stargazers();

  /** Deprecated masterBranch, {@link #defaultBranch()} */
  @Deprecated
  Optional<String> masterBranch();
}
