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

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CommentReactionContentTest {
  @ParameterizedTest
  @EnumSource(CommentReactionContent.class)
  public void testDeserialize(CommentReactionContent reaction) throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    String json = "\"" + reaction.toString() + "\"";

    CommentReactionContent content = mapper.readValue(json, CommentReactionContent.class);

    assertEquals(reaction, content);
  }
}
