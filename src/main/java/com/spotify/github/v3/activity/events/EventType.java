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

package com.spotify.github.v3.activity.events;

import java.util.Locale;

public enum EventType {
  BRANCH_PROTECTION_RULE,
  CHECK_RUN,
  CHECK_SUITE,
  CODE_SCANNING_ALERT,
  COMMIT_COMMENT,
  CONTENT_REFERENCE,
  CREATE,
  DELETE,
  DEPLOY_KEY,
  DEPLOYMENT,
  DEPLOYMENT_STATUS,
  DISCUSSION,
  DISCUSSION_COMMENT,
  DOWNLOAD,
  FOLLOW,
  FORK,
  FORK_APPLY,
  GITHUB_APP_AUTHORIZATION,
  GIST,
  GOLLUM,
  INSTALLATION,
  INSTALLATION_REPOSITORIES,
  INTEGRATION_INSTALLATION_REPOSITORIES,
  ISSUE_COMMENT,
  ISSUES,
  LABEL,
  MARKETPLACE_PURCHASE,
  MEMBER,
  MEMBERSHIP,
  MERGE_QUEUE_ENTRY,
  MERGE_GROUP,
  META,
  MILESTONE,
  ORGANIZATION,
  ORG_BLOCK,
  PACKAGE,
  PAGE_BUILD,
  PROJECT_CARD,
  PROJECT_COLUMN,
  PROJECT,
  PING,
  PUBLIC,
  PULL_REQUEST,
  PULL_REQUEST_REVIEW,
  PULL_REQUEST_REVIEW_COMMENT,
  PULL_REQUEST_REVIEW_THREAD,
  PUSH,
  REGISTRY_PACKAGE,
  RELEASE,
  REPOSITORY_DISPATCH,
  REPOSITORY,
  REPOSITORY_IMPORT,
  REPOSITORY_VULNERABILITY_ALERT,
  SCHEDULE,
  SECURITY_ADVISORY,
  STAR,
  STATUS,
  TEAM,
  TEAM_ADD,
  WATCH,
  WORKFLOW_JOB,
  WORKFLOW_DISPATCH,
  WORKFLOW_RUN,
  UNKNOWN,
  ALL;

  private EventType() {
  }

  String toString() {
    return this.name().toLowerCase();
  }
}
