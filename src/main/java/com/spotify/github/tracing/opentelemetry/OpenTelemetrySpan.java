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

import com.spotify.github.tracing.TraceHelper;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import com.spotify.github.tracing.Span;
import io.opentelemetry.api.trace.StatusCode;
import okhttp3.Request;

import static java.util.Objects.requireNonNull;

public class OpenTelemetrySpan implements Span {
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_SERVER_ERROR = 500;

    private final io.opentelemetry.api.trace.Span span;

    public OpenTelemetrySpan(final io.opentelemetry.api.trace.Span span) {
        this.span = requireNonNull(span);
    }

    @Override
    public Span success() {
        span.setStatus(StatusCode.OK);
        return this;
    }

    @Override
    public Span failure(final Throwable t) {
        if (t instanceof RequestNotOkException) {
            RequestNotOkException ex = (RequestNotOkException) t;
            span.setAttribute("http.status_code", ex.statusCode());
            span.setAttribute("message", ex.getRawMessage());
            if (ex.statusCode() - INTERNAL_SERVER_ERROR >= 0) {
                span.setAttribute("error", true);
            }
        }
        span.setStatus(StatusCode.UNSET);
        return this;
    }

    @Override
    public void close() {
        span.end();
    }

    @Override
    public Request decorateRequest(final Request request) {
        return request.newBuilder()
                .header(TraceHelper.HEADER_CLOUD_TRACE_CONTEXT, span.getSpanContext().getTraceId())
                .header(TraceHelper.HEADER_TRACE_PARENT, span.getSpanContext().getTraceId())
                .header(TraceHelper.HEADER_TRACE_STATE, span.getSpanContext().getTraceState().toString())
                .build();
    }
}
