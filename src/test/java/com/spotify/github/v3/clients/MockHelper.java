/*-
 * -\-\-
 * github-client
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

package com.spotify.github.v3.clients;

import com.spotify.github.http.HttpRequest;
import com.spotify.github.http.HttpResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MockHelper {
  private static final int HTTP_OK = 200;
  private static final int HTTP_BAD_REQUEST = 400;

  //  public static Response createMockResponse(final String headerLinksFixture, final String
  // bodyFixture)
  //      throws IOException {
  //    final ResponseBody body = mock(ResponseBody.class);
  //    when(body.string()).thenReturn(bodyFixture);
  //
  //    final Headers headers = mock(Headers.class);
  //    when(headers.get("Link")).thenReturn(headerLinksFixture);
  //
  //    final Response response = mock(Response.class);
  //    when(response.headers()).thenReturn(headers);
  //    when(response.body()).thenReturn(body);
  //    return response;
  //  }

  public static HttpResponse createMockResponse(
      final String headerLinksFixture, final String bodyFixture) throws IOException {
    int statusCode = 200;
    return createMockHttpResponse(
        "", statusCode, bodyFixture, Map.of("Link", List.of(headerLinksFixture)));
  }

  public static HttpResponse createMockHttpResponse(
      final String url,
      final int statusCode,
      final String body,
      final Map<String, List<String>> headers) {
    return new HttpResponse() {
      @Override
      public HttpRequest request() {
        return ImmutableHttpRequest.builder.url(url).build();
      }

      @Override
      public int statusCode() {
        return statusCode;
      }

      @Override
      public String statusMessage() {
        return "";
      }

      @Override
      public String body() {
        return body;
      }

      @Override
      public Map<String, List<String>> headers() {
        return Optional.ofNullable(headers).orElse(Map.of());
      }

      @Override
      public boolean isSuccessful() {
        return MockHelper.isSuccessful(statusCode);
      }
    };
  }

  private static boolean isSuccessful(final int statusCode) {
    return statusCode >= HTTP_OK && statusCode < HTTP_BAD_REQUEST;
  }
}
