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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

public class HttpRequestTest {
  @Test
  void createBareRequest() {
    // When
    HttpRequest httpRequest = ImmutableHttpRequest.builder().url("https://example.com").build();
    // Then
    assertNotNull(httpRequest);
    assertEquals("GET", httpRequest.method());
    assertEquals("https://example.com", httpRequest.url());
    assertNull(httpRequest.body());
    assertEquals(0, httpRequest.headers().size());
    assertNull(httpRequest.headers("Accept-Encoding"));
    assertNull(httpRequest.header("Accept-Encoding"));
  }

  @Test
  void createRequest() {
    // When
    HttpRequest httpRequest =
        ImmutableHttpRequest.builder()
            .url("https://example.com")
            .method("POST")
            .body("{\"foo\":\"bar\"}")
            .headers(
                Map.of(
                    "Content-Type",
                    List.of("application/json", "charset=utf-8"),
                    "Accept",
                    List.of("application/json")))
            .build();
    // Then
    assertNotNull(httpRequest);
    assertEquals("POST", httpRequest.method());
    assertEquals("https://example.com", httpRequest.url());
    assertEquals("{\"foo\":\"bar\"}", httpRequest.body());
    assertEquals(2, httpRequest.headers().size());
    assertThat(
        httpRequest.headers("Content-Type"),
        containsInAnyOrder("application/json", "charset=utf-8"));
    assertEquals("application/json,charset=utf-8", httpRequest.header("Content-Type"));
    assertThat(httpRequest.headers("Accept"), containsInAnyOrder("application/json"));
    assertEquals("application/json", httpRequest.header("Accept"));
  }
}
