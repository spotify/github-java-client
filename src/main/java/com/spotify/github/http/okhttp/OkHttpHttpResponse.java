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
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpHttpResponse implements HttpResponse {
  private static final int HTTP_OK = 200;
  private static final int HTTP_BAD_REQUEST = 400;

  private final HttpRequest request;
  private final Response response;
  private final int statusCode;
  private final String statusMessage;
  private InputStream body;
  private final Map<String, List<String>> headers;
  private String bodyString;

  public OkHttpHttpResponse(final HttpRequest request, final Response response) {
    this.request = request;
    this.statusCode = response.code();
    this.statusMessage = response.message();
    this.headers = response.headers().toMultimap();
    this.response = response;
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

  @Override
  public InputStream body() {
    if (body == null) {
      body = Optional.ofNullable(response.body()).map(ResponseBody::byteStream).orElse(null);
    }
    return body;
  }

  public String bodyString() {
    if (bodyString == null) {
      if (response != null) {
        bodyString = responseBodyUnchecked(response);
      }
    }
    return bodyString;
  }

  @Override
  public Map<String, List<String>> headers() {
    return this.headers;
  }

  @Override
  public boolean isSuccessful() {
    return this.statusCode() >= HTTP_OK && this.statusCode() < HTTP_BAD_REQUEST;
  }

  @Override
  public void close() {
    if (response != null) {
      response.close();
      if (response.body() != null) {
        response.body().close();
      }
    }
  }

  private static String responseBodyUnchecked(final Response response) {
    if (response.body() == null) {
      return null;
    }
    try (ResponseBody body = response.body()) {
      return body.string();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed getting response body for: " + response, e);
    }
  }
}
