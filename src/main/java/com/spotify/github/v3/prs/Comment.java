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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.UpdateTracking;
import com.spotify.github.v3.User;
import java.net.URI;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Pull Request Review Comments are comments on a portion of the unified diff. These are separate
 * from Commit Comments {@link com.spotify.github.v3.comment.Comment} (which are applied directly to
 * a commit, outside of the Pull Request view), and Issue Comments {@link
 * com.spotify.github.v3.comment.Comment} (which do not reference a portion of the unified diff).
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableComment.class)
@JsonDeserialize(as = ImmutableComment.class)
public interface Comment extends UpdateTracking {

  /** Comment API URL. */
  @Nullable
  URI url();

  /** Comment id. */
  @Nullable
  Long id();

  /** Unified diff. */
  @Nullable
  String diffHunk();

  /** The relative path of the file to comment on. */
  @Nullable
  String path();

  /** The line index in the diff to comment on. */
  @Nullable
  Integer position();

  /** Base content line position. */
  @Nullable
  Integer originalPosition();

  /** The SHA of the commit to comment on. */
  @Nullable
  String commitId();

  /** Base commit sha. */
  @Nullable
  String originalCommitId();

  /** Comment author. */
  @Nullable
  User user();

  /** The text of the comment. */
  @Nullable
  String body();

  /** Comment URL. */
  @Nullable
  URI htmlUrl();

  /** Pull request API URL. */
  @Nullable
  URI pullRequestUrl();

  /** Link references. */
  @Nullable
  @JsonProperty("_links")
  CommentLinks links();
}
