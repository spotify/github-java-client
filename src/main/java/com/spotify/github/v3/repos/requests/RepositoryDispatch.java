/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2016 - 2023 Spotify AB
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

package com.spotify.github.v3.repos.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableRepositoryDispatch.class)
@JsonDeserialize(as = ImmutableRepositoryDispatch.class)
public interface RepositoryDispatch {

  /** The custom webhook event name */

  String eventType();

  /** JSON payload with extra information about the webhook event
  * that your action or workflow may use. */
  Optional<JsonNode> clientPayload();

}
