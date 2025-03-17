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

import static com.spotify.github.MockHelper.createMockHttpResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class HttpResponseTest {
  @Test
  void createBareResponse() {
    // When
    HttpResponse httpResponse = createMockHttpResponse("https://example.com", 200, "{}", Map.of());
    // Then
    assertNotNull(httpResponse);
    assertEquals("GET", httpResponse.request().method());
    assertEquals("https://example.com", httpResponse.request().url());
    assertTrue(httpResponse.isSuccessful());
    assertEquals(0, httpResponse.headers().size());
    assertNull(httpResponse.headers("Accept-Encoding"));
    assertNull(httpResponse.header("Accept-Encoding"));
  }

  @Test
  void createResponse() throws IOException {
    // When
    HttpResponse httpResponse =
        createMockHttpResponse(
            "https://example.com",
            200,
            "{\"foo\":\"bar\"}",
            Map.of(
                "Content-Type",
                List.of("application/json", "charset=utf-8"),
                "Accept",
                List.of("application/json"),
                "Cache-Control",
                List.of("no-cache"),
                "Set-Cookie",
                List.of("sessionId=abc123", "userId=xyz789")));
    String responseBody = null;
    try (InputStream is = httpResponse.body()) {
      responseBody = new String(is.readAllBytes());
    }
    // Then
    assertNotNull(httpResponse);
    assertEquals("{\"foo\":\"bar\"}", httpResponse.bodyString());
    assertEquals("{\"foo\":\"bar\"}", responseBody);
    assertEquals(4, httpResponse.headers().size());
    assertThat(
        httpResponse.headers("Content-Type"),
        containsInAnyOrder("application/json", "charset=utf-8"));
    assertEquals("application/json,charset=utf-8", httpResponse.header("Content-Type"));
    assertThat(httpResponse.headers("Accept"), containsInAnyOrder("application/json"));
    assertEquals("application/json", httpResponse.header("Accept"));
    assertThat(httpResponse.headers("Cache-Control"), containsInAnyOrder("no-cache"));
    assertEquals("no-cache", httpResponse.header("Cache-Control"));
    assertThat(
        httpResponse.headers("Set-Cookie"),
        containsInAnyOrder("sessionId=abc123", "userId=xyz789"));
    assertEquals("sessionId=abc123,userId=xyz789", httpResponse.header("Set-Cookie"));
  }
}
