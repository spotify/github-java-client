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
package com.spotify.github.v3.comment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.UpdateTracking;
import com.spotify.github.v3.User;
import org.immutables.value.Value;

/**
 * Comment reaction object.
 *
 * <p>See <a
 * href="https://docs.github.com/en/rest/reactions/reactions?apiVersion=2022-11-28#about-reactions">About
 * GitHub Issue Comment reactions</a>
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableCommentReaction.class)
@JsonDeserialize(as = ImmutableCommentReaction.class)
public interface CommentReaction extends UpdateTracking {

  /** Reaction ID. */
  long id();

  /** Reaction user. */
  User user();

  /** Reaction content. */
  CommentReactionContent content();
}
