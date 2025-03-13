/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2021 Spotify AB
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

package com.spotify.github.http;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.util.List;
import java.util.Map;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableHttpRequest.class)
@JsonDeserialize(as = ImmutableHttpRequest.class)
public interface HttpRequest {
  @Value.Default
  default String method() {
    return "GET";
  }

  String url();

  @Nullable
  String body();

  @Value.Default
  default Map<String, List<String>> headers() {
    return Map.of();
  }

  default List<String> headers(String headerName) {
    return headers().get(headerName);
  }

  default String header(String headerName) {
    List<String> headerValues = this.headers(headerName);
    if (headerValues == null || headerValues.isEmpty()) {
      return null;
    }
    if (headerValues.size() == 1) {
      return headerValues.get(0);
    } else {
      return String.join(",", headerValues);
    }
  }
}
