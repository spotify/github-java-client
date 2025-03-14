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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.prs.Comment;
import com.spotify.github.v3.prs.PullRequestItem;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Triggered when a comment {@link Comment} on a Pull Request's unified diff is created, edited, or
 * deleted (in the Files Changed tab).
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutablePullRequestReviewCommentEvent.class)
@JsonDeserialize(as = ImmutablePullRequestReviewCommentEvent.class)
public interface PullRequestReviewCommentEvent extends BaseEvent {

  /**
   * The action that was performed on the comment. Can be one of "created", "edited", or "deleted".
   */
  @Nullable
  String action();

  /** The comment itself. */
  @Nullable
  Comment comment();

  /** The pull request the comment belongs to. */
  @Nullable
  PullRequestItem pullRequest();
}
