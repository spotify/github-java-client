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

package com.spotify.github.opencensus;

import com.spotify.github.Span;
import com.spotify.github.Tracer;
import io.opencensus.trace.Tracing;

import java.util.concurrent.CompletionStage;

import static io.opencensus.trace.AttributeValue.stringAttributeValue;
import static io.opencensus.trace.Span.Kind.CLIENT;
import static java.util.Objects.requireNonNull;

public class OpenCensusTracer implements Tracer {

    private static final io.opencensus.trace.Tracer TRACER = Tracing.getTracer();

    @Override
    public Span span(final String name, final CompletionStage<?> future) {
        return internalSpan(name, future);
    }

    @SuppressWarnings("MustBeClosedChecker")
    private Span internalSpan(
            final String name,
            final CompletionStage<?> future) {
        requireNonNull(name);
        requireNonNull(future);

        final io.opencensus.trace.Span ocSpan =
                TRACER.spanBuilder(name).setSpanKind(CLIENT).startSpan();

        ocSpan.putAttribute("component", stringAttributeValue("github-api-client"));
        ocSpan.putAttribute("peer.service", stringAttributeValue("github"));
        final Span span = new OpenCensusSpan(ocSpan);

        future.whenComplete(
                (result, t) -> {
                    if (t == null) {
                        span.success();
                    } else {
                        span.failure();
                    }
                    span.close();
                });

        return span;
    }
}
