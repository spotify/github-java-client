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

package com.spotify.github.tracing.opentelemetry;

import com.spotify.github.tracing.BaseTracer;
import com.spotify.github.tracing.Span;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;

import java.util.concurrent.CompletionStage;

import static java.util.Objects.requireNonNull;

public class OpenTelemetryTracer extends BaseTracer {
    private final io.opentelemetry.api.trace.Tracer tracer;
    private final OpenTelemetry openTelemetry;

    public OpenTelemetryTracer(final OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer("github-java-client");
    }

    public OpenTelemetryTracer() {
        this(GlobalOpenTelemetry.get());
    }

    @SuppressWarnings("MustBeClosedChecker")
    protected Span internalSpan(
            final String path,
            final String method,
            final CompletionStage<?> future) {
        requireNonNull(path);

        final io.opentelemetry.api.trace.Span otSpan =
                tracer.spanBuilder("GitHub Request")
                        .setParent(Context.current())
                        .setSpanKind(SpanKind.CLIENT).startSpan();

        otSpan.setAttribute("component", "github-api-client");
        otSpan.setAttribute("peer.service", "github");
        otSpan.setAttribute("http.url", path);
        otSpan.setAttribute("method", method);
        final Span span = new OpenTelemetrySpan(otSpan);

        if (future == null) {
            return span;
        } else {
            attachSpanToFuture(span, future);
        }
        return span;
    }
}
