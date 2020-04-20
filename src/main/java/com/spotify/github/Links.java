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

package com.spotify.github;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.net.URI;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Entity for mapping the JSON field _links. */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableLinks.class)
@JsonDeserialize(as = ImmutableLinks.class)
public interface Links {

  /**
   * Link to this entity in the API.
   *
   * @return The link to the API of this entity
   */
  @Nullable
  Href<URI> self();

  /**
   * Link to the HTML representaion of this item.
   *
   * @return The link to the HTML representation of this entity
   */
  @Nullable
  Href<URI> html();

  /**
   * Holder for href values.
   *
   * @param <T> either String or URI
   */
  @Value.Immutable
  @GithubStyle
  @JsonSerialize(as = ImmutableHref.class)
  @JsonDeserialize(as = ImmutableHref.class)
  interface Href<T> {

    T href();
  }
}
