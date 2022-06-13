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

package com.spotify.github.v3.repos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.util.Collections;
import java.util.List;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

/** Branch resource */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableRequiredStatusChecks.class)
@JsonDeserialize(as = ImmutableRequiredStatusChecks.class)
public interface RequiredStatusChecks {
  @JsonProperty("enforcement_level")
  String enforcementLevel();

  @Default
  default List<String> contexts() {
    return Collections.emptyList();
  }
}
