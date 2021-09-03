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

/**
 * Copyright 2016 Spotify AB. All rights reserved.
 *
 * <p>The contents of this file are licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spotify.github.v3.exceptions;

/** HTTP response with non-200 StatusCode. */
public class RequestNotOkException extends GithubException {

  private final int statusCode;
  private final String path;

  private static String decoratedMessage(final String path, final int statusCode, final String msg) {
    return String.format("%s %d: %s", path, statusCode, msg);
  }

  /**
   * Response to request came back with non-2xx status code
   *
   * @param path URI path
   * @param statusCode status of repsonse
   * @param msg response body
   */
  public RequestNotOkException(final String path, final int statusCode, final String msg) {
    super(decoratedMessage(path, statusCode, msg));
    this.statusCode = statusCode;
    this.path = path;
  }

  /**
   * Response to request came back with non-2xx status code
   *
   * @param path URI path
   * @param statusCode status of repsonse
   * @param msg response body
   * @param cause exception cause
   */
  public RequestNotOkException(
      final String path, final int statusCode, final String msg, final Throwable cause) {
    super(decoratedMessage(path, statusCode, msg), cause);
    this.statusCode = statusCode;
    this.path = path;
  }

  /**
   * Get the status code of the response
   *
   * @return status
   */
  public int statusCode() {
    return statusCode;
  }

  /**
   * Get request URI path
   *
   * @return path
   */
  public String path() {
    return path;
  }
}
