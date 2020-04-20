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

package com.spotify.github.v3.exceptions;

/** The Read only repository exception. */
public class ReadOnlyRepositoryException extends RequestNotOkException {
  /**
   * Instantiates a new Read only repository exception.
   *
   * @param path the path
   * @param statusCode the status code
   * @param msg the msg
   */
  public ReadOnlyRepositoryException(final String path, final int statusCode, final String msg) {
    super(path, statusCode, msg);
  }

  /**
   * Instantiates a new Read only repository exception.
   *
   * @param path the path
   * @param statusCode the status code
   * @param msg the msg
   * @param cause the cause
   */
  public ReadOnlyRepositoryException(
      final String path, final int statusCode, final String msg, final Throwable cause) {
    super(path, statusCode, msg, cause);
  }
}
