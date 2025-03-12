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

package com.spotify.github.tracing;

import static java.util.Objects.requireNonNull;

import com.spotify.github.http.HttpRequest;
import java.util.concurrent.CompletionStage;

public class NoopTracer implements Tracer {

  public static final NoopTracer INSTANCE = new NoopTracer();
  private static final Span SPAN =
      new Span() {
        @Override
        public Span success() {
          return this;
        }

        @Override
        public Span failure(final Throwable t) {
          return this;
        }

        @Override
        public void close() {}

        @Override
        public Span addTag(final String key, final String value) {
          return this;
        }

        @Override
        public Span addTag(final String key, final boolean value) {
          return this;
        }

        @Override
        public Span addTag(final String key, final long value) {
          return this;
        }

        @Override
        public Span addTag(final String key, final double value) {
          return this;
        }

        @Override
        public Span addEvent(final String description) {
          return this;
        }
      };

  private NoopTracer() {}

  @Override
  public Span span(final String path, final String method, final CompletionStage<?> future) {
    return SPAN;
  }

  @Override
  public Span span(final String path, final String method) {
    return SPAN;
  }

  @Override
  public Span span(final HttpRequest request) {
    return SPAN;
  }

  @Override
  public Span span(final HttpRequest request, final CompletionStage<?> future) {
    return SPAN;
  }

  @Override
  public void attachSpanToFuture(final Span span, final CompletionStage<?> future) {
    requireNonNull(span);
    requireNonNull(future);
    future.whenComplete(
        (result, t) -> {
          if (t == null) {
            span.success();
          } else {
            span.failure(t);
          }
          span.close();
        });
  }
}
