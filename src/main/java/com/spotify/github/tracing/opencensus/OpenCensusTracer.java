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

package com.spotify.github.tracing.opencensus;

import com.spotify.github.tracing.BaseTracer;
import com.spotify.github.tracing.Span;
import io.opencensus.trace.Tracing;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletionStage;

import static io.opencensus.trace.AttributeValue.stringAttributeValue;
import static io.opencensus.trace.Span.Kind.CLIENT;
import static java.util.Objects.requireNonNull;

public class OpenCensusTracer extends BaseTracer {

    private static final io.opencensus.trace.Tracer TRACER = Tracing.getTracer();

    @SuppressWarnings("MustBeClosedChecker")
    protected Span internalSpan(
            final String path,
            final String method,
            final CompletionStage<?> future) {
        requireNonNull(path);

        final io.opencensus.trace.Span ocSpan =
                TRACER.spanBuilder("GitHub Request").setSpanKind(CLIENT).startSpan();

        ocSpan.putAttribute("component", stringAttributeValue("github-api-client"));
        ocSpan.putAttribute("peer.service", stringAttributeValue("github"));
        ocSpan.putAttribute("http.url", stringAttributeValue(path));
        ocSpan.putAttribute("method", stringAttributeValue(method));
        final Span span = new OpenCensusSpan(ocSpan);

        if (future != null) {
            attachSpanToFuture(span, future);
        }

        return span;
    }

    @Override
    protected Span internalSpan(final Request request, final CompletionStage<?> future) {
        requireNonNull(request);
        return internalSpan(request.url().toString(), request.method(), future);
    }

    @Override
    public Call.Factory createTracedClient(final OkHttpClient client) {
        return new Call.Factory() {
            @NotNull
            @Override
            public Call newCall(@NotNull final Request request) {
                return client.newCall(request);
            }
        };
    }
}
