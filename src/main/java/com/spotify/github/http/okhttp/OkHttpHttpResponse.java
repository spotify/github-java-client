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

import com.spotify.github.http.BaseHttpResponse;
import com.spotify.github.http.HttpRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** OkHttpHttpResponse is the implementation of HttpResponse using OkHttp. */
public class OkHttpHttpResponse extends BaseHttpResponse {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Response response;
  private InputStream body;
  private String bodyString;

  public OkHttpHttpResponse(final HttpRequest request, final Response response) {
    super(request, response.code(), response.message(), response.headers().toMultimap());
    this.response = response;
  }

  @Override
  public InputStream body() {
    if (body == null) {
      body = Optional.ofNullable(response.body()).map(ResponseBody::byteStream).orElse(null);
    }
    return body;
  }

  @Override
  public String bodyString() {
    if (bodyString == null) {
      if (response != null) {
        bodyString = responseBodyUnchecked(response);
      }
    }
    return bodyString;
  }

  @Override
  public void close() {
    try {
      if (response != null) {
        if (response.body() != null) {
          response.body().close();
        }
        response.close();
      }
    } catch (IllegalStateException e) {
      log.debug("Failed closing response: {}", e.getMessage());
    }
  }

  /**
   * Get the response body as a string.
   *
   * @param response the response
   * @return the response body as a string
   */
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
