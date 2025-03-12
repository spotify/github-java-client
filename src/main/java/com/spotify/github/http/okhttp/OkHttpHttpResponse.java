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

package com.spotify.github.http.okhttp;

import com.spotify.github.http.HttpRequest;
import com.spotify.github.http.HttpResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpHttpResponse implements HttpResponse {
  private static final int HTTP_OK = 200;
  private static final int HTTP_BAD_REQUEST = 400;

  private final HttpRequest request;
  private final int statusCode;
  private final String statusMessage;
  private final String body;
  private final Map<String, List<String>> headers;

  public OkHttpHttpResponse(final HttpRequest request, final Response response) throws IOException {
    this.request = request;
    this.statusCode = response.code();
    this.statusMessage = response.message();
    this.body = responseBodyUnchecked(response);
    this.headers = response.headers().toMultimap();
    response.close();
  }

  public OkHttpHttpResponse(
      final HttpRequest request,
      final int statusCode,
      final String statusMessage,
      final String body,
      final Map<String, List<String>> headers) {
    this.request = request;
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
    this.body = body;
    this.headers = headers;
  }

  @Override
  public HttpRequest request() {
    return request;
  }

  public int statusCode() {
    return statusCode;
  }

  @Override
  public String statusMessage() {
    return statusMessage;
  }

  public String body() {
    return body;
  }

  @Override
  public Map<String, List<String>> headers() {
    return this.headers;
  }

  @Override
  public boolean isSuccessful() {
    return this.statusCode() >= HTTP_OK && this.statusCode() < HTTP_BAD_REQUEST;
  }

  private static String responseBodyUnchecked(final Response response) {
    try (ResponseBody body = response.body()) {
      return body.string();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed getting response body for: " + response, e);
    }
  }
}
