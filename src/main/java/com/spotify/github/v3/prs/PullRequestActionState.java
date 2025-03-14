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

package com.spotify.github.v3.prs;

/** Helpful constants for common PullRequest Action states. */
public class PullRequestActionState {

  public static final String ASSIGNED = "assigned";
  public static final String CLOSED = "closed";
  public static final String EDITED = "edited";
  public static final String LABELED = "labeled";
  public static final String OPENED = "opened";
  public static final String REOPENED = "reopened";
  public static final String REVIEW_REQUESTED = "review_requested";
  public static final String REVIEW_REQUEST_REMOVED = "review_request_removed";
  public static final String SYNCHRONIZE = "synchronize";
  public static final String UNASSIGNED = "unassigned";
  public static final String UNLABELED = "unlabeled";

  private PullRequestActionState() {}
}
