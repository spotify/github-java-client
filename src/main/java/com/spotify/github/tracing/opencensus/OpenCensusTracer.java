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

import static io.opencensus.trace.Span.Kind.CLIENT;
import static java.util.Objects.requireNonNull;

import com.spotify.github.http.HttpRequest;
import com.spotify.github.tracing.BaseTracer;
import com.spotify.github.tracing.Span;
import com.spotify.github.tracing.TraceHelper;
import io.opencensus.trace.Tracing;
import java.util.concurrent.CompletionStage;
import okhttp3.*;

/** Tracer implementation using OpenCensus. */
public class OpenCensusTracer extends BaseTracer {

  private static final io.opencensus.trace.Tracer TRACER = Tracing.getTracer();

  @SuppressWarnings("MustBeClosedChecker")
  protected Span internalSpan(
      final String path, final String method, final CompletionStage<?> future) {
    requireNonNull(path);

    final io.opencensus.trace.Span ocSpan =
        TRACER.spanBuilder("GitHub Request").setSpanKind(CLIENT).startSpan();

    final Span span =
        new OpenCensusSpan(ocSpan)
            .addTag(TraceHelper.TraceTags.COMPONENT, "github-api-client")
            .addTag(TraceHelper.TraceTags.PEER_SERVICE, "github")
            .addTag(TraceHelper.TraceTags.HTTP_URL, path)
            .addTag(TraceHelper.TraceTags.HTTP_METHOD, method);

    if (future != null) {
      attachSpanToFuture(span, future);
    }

    return span;
  }

  @Override
  protected Span internalSpan(final HttpRequest request, final CompletionStage<?> future) {
    requireNonNull(request);
    return internalSpan(request.url(), request.method(), future);
  }
}
