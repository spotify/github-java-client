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

import com.spotify.github.tracing.opentelemetry.OpenTelemetrySpan;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import io.opentelemetry.api.trace.StatusCode;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.Mockito.*;

class OpenTelemetrySpanTest {
    private final io.opentelemetry.api.trace.Span wrapped = mock(io.opentelemetry.api.trace.Span.class);

    @Test
    public void succeed() {
        final Span span = new OpenTelemetrySpan(wrapped);
        span.success();
        span.close();

        verify(wrapped).setStatus(StatusCode.OK);
        verify(wrapped).end();
    }

    @Test
    public void fail() {
        final Span span = new OpenTelemetrySpan(wrapped);
        span.failure(new RequestNotOkException("method", "path", 404, "Not found", Collections.emptyMap()));
        span.close();

        verify(wrapped).setStatus(StatusCode.UNSET);
        verify(wrapped).setAttribute("http.status_code", 404);
        verify(wrapped).end();
    }

    @Test
    public void failOnServerError() {
        final Span span = new OpenTelemetrySpan(wrapped);
        span.failure(new RequestNotOkException("method", "path", 500, "Internal Server Error", Collections.emptyMap()));
        span.close();

        verify(wrapped).setStatus(StatusCode.UNSET);
        verify(wrapped).setAttribute("http.status_code", 500);
        verify(wrapped).setAttribute("error", true);
        verify(wrapped).end();
    }

    @Test
    public void failWithNullThrowable() {
        final Span span = new OpenTelemetrySpan(wrapped);
        span.failure(null);
        span.close();

        verify(wrapped).setStatus(StatusCode.UNSET);
        verify(wrapped, never()).setAttribute(anyString(), any());
        verify(wrapped).end();
    }

    @Test
    public void failWithNonRequestNotOkException() {
        final Span span = new OpenTelemetrySpan(wrapped);
        span.failure(new RuntimeException("Unexpected error"));
        span.close();

        verify(wrapped).setStatus(StatusCode.UNSET);
        verify(wrapped, never()).setAttribute("http.status_code", 404);
        verify(wrapped, never()).setAttribute("error", true);
        verify(wrapped).end();
    }
}
