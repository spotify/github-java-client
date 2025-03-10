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

import com.spotify.github.tracing.BaseTracer;
import com.spotify.github.tracing.Span;
import com.spotify.github.tracing.TraceHelper;
import io.opencensus.trace.Tracing;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

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
        CompletableFuture<Response> future = new CompletableFuture<>();
        Span span =
            internalSpan(request, future)
                .addTag(TraceHelper.TraceTags.HTTP_URL, request.url().toString());
        OkHttpClient.Builder okBuilder = client.newBuilder();
        okBuilder
            .networkInterceptors()
            .add(
                0,
                new Interceptor() {
                  @NotNull
                  @Override
                  public Response intercept(@NotNull final Chain chain) throws IOException {
                    try {
                      Response response = chain.proceed(chain.request());
                      span.addTag(TraceHelper.TraceTags.HTTP_STATUS_CODE, response.code())
                          .addTag(TraceHelper.TraceTags.HTTP_STATUS_MESSAGE, response.message())
                          .success();
                      future.complete(response);
                      return response;
                    } catch (Exception ex) {
                      span.failure(ex);
                      future.completeExceptionally(ex);
                      throw ex;
                    } finally {
                      span.close();
                    }
                  }
                });

        return okBuilder.build().newCall(request);
      }
    };
  }
}
