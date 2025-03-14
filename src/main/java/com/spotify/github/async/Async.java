/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2024 Spotify AB
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

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

/** Async class to facilitate async operations. */
public class Async {
    private Async() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> Stream<T> streamFromPaginatingIterable(final Iterable<AsyncPage<T>> iterable) {
        return stream(iterable.spliterator(), false)
                   .flatMap(page -> stream(page.spliterator(), false));
    }

    public static <T> CompletableFuture<T> exceptionallyCompose(
            final CompletableFuture<T> future, final Function<Throwable, CompletableFuture<T>> handler) {

        return future
                .handle(
                        (result, throwable) -> {
                            if (throwable != null) {
                                return handler.apply(throwable);
                            } else {
                                return CompletableFuture.completedFuture(result);
                            }
                        })
                .thenCompose(Function.identity());
    }
}
