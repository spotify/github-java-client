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
import com.spotify.github.jackson.CommentReactionContentDeserializer;
import com.spotify.github.jackson.CommentReactionContentSerializer;

/**
 * Comment reaction content.
 *
 * <p>See <a
 * href="https://docs.github.com/en/rest/reactions/reactions?apiVersion=2022-11-28#about-reactions">About
 * GitHub Issue Comment reactions</a>
 */
@JsonDeserialize(using = CommentReactionContentDeserializer.class)
@JsonSerialize(using = CommentReactionContentSerializer.class)
public enum CommentReactionContent {
  THUMBS_UP("+1"), // 👍
  THUMBS_DOWN("-1"), // 👎
  LAUGH("laugh"), // 😄
  HOORAY("hooray"), // 🎉
  CONFUSED("confused"), // 😕
  HEART("heart"), // ❤️
  ROCKET("rocket"), // 🚀
  EYES("eyes"); // 👀

  private final String reaction;

  CommentReactionContent(final String reaction) {
    this.reaction = reaction;
  }

  @Override
  public String toString() {
    return reaction;
  }
}
