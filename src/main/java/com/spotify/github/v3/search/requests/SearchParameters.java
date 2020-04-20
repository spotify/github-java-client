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

package com.spotify.github.v3.search.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.Parameters;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Search parameters resource defines required and optional parameters. To be serialized as
 * key=value.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableSearchParameters.class)
@JsonDeserialize(as = ImmutableSearchParameters.class)
public interface SearchParameters extends Parameters {

  /** The search keywords, as well as any qualifiers. */
  @Nullable
  String q();

  /** The sort field. One of stars, forks, or updated. Default: results are sorted by best match. */
  Optional<String> sort();

  /** The sort order if sort parameter is provided. One of asc or desc. Default: desc */
  Optional<String> order();
}
