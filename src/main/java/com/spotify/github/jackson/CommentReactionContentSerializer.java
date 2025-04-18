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
package com.spotify.github.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.spotify.github.v3.comment.CommentReactionContent;
import java.io.IOException;
/**
 * Custom serializer for {@link CommentReactionContent}.
 */
public class CommentReactionContentSerializer extends JsonSerializer<CommentReactionContent> {
  @Override
  public void serialize(final CommentReactionContent value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
    gen.writeString(value.toString());
  }
}
