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

import static com.fasterxml.jackson.core.JsonToken.VALUE_NULL;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.spotify.github.GitHubInstant;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

class GitHubInstantJsonDeserializer extends JsonDeserializer<GitHubInstant> {

  static final GitHubInstantJsonDeserializer INSTANCE = new GitHubInstantJsonDeserializer();

  @Override
  public GitHubInstant deserialize(final JsonParser p, final DeserializationContext ctxt)
      throws IOException {

    switch (Optional.ofNullable(p.currentToken()).orElse(VALUE_NULL)) {
      case VALUE_NULL:
        return null;
      case VALUE_NUMBER_INT:
        return GitHubInstant.create(p.getNumberValue());
      default:
        return GitHubInstant.create(p.readValueAs(Instant.class));
    }
  }
}
