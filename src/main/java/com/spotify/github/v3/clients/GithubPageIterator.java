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

package com.spotify.github.v3.clients;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;

import com.spotify.github.async.AsyncPage;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Async page iterator implementation
 *
 * @param <T> resource type
 */
public class GithubPageIterator<T> implements Iterator<AsyncPage<T>> {

  private final Object lock = new Object();
  private AsyncPage<T> current;

  /**
   * C'tor.
   *
   * @param initialPage initial async page
   */
  public GithubPageIterator(final AsyncPage<T> initialPage) {
    this.current = initialPage;
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasNext() {
    synchronized (lock) {
      return nonNull(current);
    }
  }

  /** {@inheritDoc} */
  @Override
  public AsyncPage<T> next() {
    synchronized (lock) {
      if (isNull(current)) {
        throw new NoSuchElementException("Iteration exhausted");
      }

      final AsyncPage<T> currentPage = current.clone();
      current =
          current
              .hasNextPage()
              .thenCompose(
                  hasNext ->
                      Optional.of(hasNext)
                          .filter(Boolean::booleanValue)
                          .map(ignore -> current.nextPage())
                          .orElseGet(() -> completedFuture(null)))
              .join();
      return currentPage;
    }
  }
}
