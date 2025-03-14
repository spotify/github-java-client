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

package com.spotify.github.async;

import com.spotify.github.http.Pagination;
import java.util.concurrent.CompletableFuture;

/**
 * Async page
 *
 * @param <T> resource type
 */
public interface AsyncPage<T> extends Iterable<T> {

  /**
   * Pagination data.
   *
   * @return pagination object
   */
  CompletableFuture<Pagination> pagination();

  /**
   * Next page.
   *
   * @return page
   */
  CompletableFuture<AsyncPage<T>> nextPage();

  /**
   * Has next page.
   *
   * @return true or false
   */
  CompletableFuture<Boolean> hasNextPage();

  /**
   * Clone page object.
   *
   * @return new page object
   */
  AsyncPage<T> clone();
}
