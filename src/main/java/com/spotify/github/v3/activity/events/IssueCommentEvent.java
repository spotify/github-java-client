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
import com.spotify.github.v3.comment.Comment;
import com.spotify.github.v3.issues.Issue;
import com.spotify.github.v3.issues.changes.Changes;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Triggered when an issue comment is created, edited, or deleted. */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableIssueCommentEvent.class)
@JsonDeserialize(as = ImmutableIssueCommentEvent.class)
public interface IssueCommentEvent extends BaseEvent {

  /**
   * The action that was performed on the comment. Can be one of "created", "edited", or "deleted".
   */
  @Nullable
  String action();

  /**
   * The changes to the comment if the action was "edited".
   *
   * <p>The GitHub API does also declare "changes" and "changes[body][from]" but it does not provide
   * any examples of that data.
   *
   * @see "https://developer.github.com/v3/activity/events/types/#issuecommentevent"
   */
  Optional<Changes> changes();

  /** The {@link Issue} the comment belongs to. */
  @Nullable
  Issue issue();

  /** The {@link Comment} itself. */
  @Nullable
  Comment comment();
}
