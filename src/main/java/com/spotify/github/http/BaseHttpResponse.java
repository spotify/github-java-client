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

import java.util.List;
import java.util.Map;

/** BaseHttpResponse is the base implementation of HttpResponse. */
public abstract class BaseHttpResponse implements HttpResponse {
  private static final int HTTP_OK = 200;
  private static final int HTTP_BAD_REQUEST = 400;

  protected final HttpRequest request;
  protected final int statusCode;
  protected final String statusMessage;
  protected final Map<String, List<String>> headers;

  public BaseHttpResponse(
      final HttpRequest request,
      final int statusCode,
      final String statusMessage,
      final Map<String, List<String>> headers) {
    this.request = request;
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
    this.headers = headers;
  }

  /**
   * Returns the request that generated this response.
   *
   * @return HttpRequest the request that generated this response
   */
  @Override
  public HttpRequest request() {
    return request;
  }

  /**
   * Returns the HTTP status code of the response.
   *
   * @return the status code of the response
   */
  @Override
  public int statusCode() {
    return statusCode;
  }

  /**
   * Returns the HTTP status message of the response.
   *
   * @return the status message of the response
   */
  @Override
  public String statusMessage() {
    return statusMessage;
  }

  /**
   * Returns the headers of the response.
   *
   * @return the headers of the response as a Map of strings
   */
  @Override
  public Map<String, List<String>> headers() {
    return this.headers;
  }

  /**
   * Returns the values of the header with the given name. If the header is not present, this method
   * returns null.
   *
   * @param headerName the name of the header
   * @return the values of the header with the given name as a List of strings, or null if the
   *     header is not present
   */
  @Override
  public List<String> headers(final String headerName) {
    return this.headers.get(headerName);
  }

  /**
   * Returns the first value of the header with the given name. If the header is not present, this
   * method returns null.
   *
   * @param headerName the name of the header
   * @return the first value of the header with the given name, or null if the header is not present
   */
  @Override
  public String header(final String headerName) {
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

  /**
   * Was the request successful?
   *
   * @return true if the status code is in the range [200, 400)
   */
  @Override
  public boolean isSuccessful() {
    return this.statusCode() >= HTTP_OK && this.statusCode() < HTTP_BAD_REQUEST;
  }
}
