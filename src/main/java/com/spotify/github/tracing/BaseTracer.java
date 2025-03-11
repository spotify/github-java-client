/*-
 * -\-\-
 * github-client
 * --
 * Copyright (C) 2016 - 2021 Spotify AB
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

import okhttp3.Request;

import java.util.concurrent.CompletionStage;

public abstract class BaseTracer implements Tracer {
    @Override
    public Span span(final String name, final String method, final CompletionStage<?> future) {
        return internalSpan(name, method, future);
    }

    @Override
    public Span span(final String path, final String method) {
        return internalSpan(path, method, null);
    }

    @Override
    public Span span(final Request request) {
        return internalSpan(request, null);
    }

    @Override
    public Span span(final Request request, final CompletionStage<?> future) {
        return internalSpan(request, future);
    }

    protected abstract Span internalSpan(
            String path,
            String method,
            CompletionStage<?> future);

    protected abstract Span internalSpan(
            Request request,
            CompletionStage<?> future);

    @Override
    public void attachSpanToFuture(final Span span, final CompletionStage<?> future) {
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
