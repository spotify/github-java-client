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

package com.spotify.github.http.okhttp;

import static okhttp3.MediaType.parse;

import com.spotify.github.http.HttpClient;
import com.spotify.github.http.HttpRequest;
import com.spotify.github.http.HttpResponse;
import com.spotify.github.http.ImmutableHttpRequest;
import com.spotify.github.tracing.NoopTracer;
import com.spotify.github.tracing.Span;
import com.spotify.github.tracing.TraceHelper;
import com.spotify.github.tracing.Tracer;
import com.spotify.github.tracing.opencensus.OpenCensusTracer;
import com.spotify.github.tracing.opentelemetry.OpenTelemetryTracer;
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTelemetry;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

/**
 * OkHttpHttpClient is the implementation of HttpClient using OkHttp. This also serves as an example
 * of how to create a custom HttpClient. This HttpClient is also capable of tracing the requests
 * using OpenCensus or OpenTelemetry.
 */
public class OkHttpHttpClient implements HttpClient {
  private final OkHttpClient client;
  private Tracer tracer;
  private Call.Factory callFactory;

  public OkHttpHttpClient(final OkHttpClient client) {
    this.client = client;
    this.tracer = NoopTracer.INSTANCE;
    this.callFactory = createTracedClient();
  }

  public OkHttpHttpClient(final OkHttpClient client, final Tracer tracer) {
    this.client = client;
    this.tracer = tracer;
    this.callFactory = createTracedClient();
  }

  /**
   * Send a request and return a future with the response.
   *
   * @param httpRequest the request to send
   * @return a future with the response
   */
  @Override
  public CompletableFuture<HttpResponse> send(final HttpRequest httpRequest) {
    Request request = buildOkHttpRequest(httpRequest);
    CompletableFuture<HttpResponse> future = new CompletableFuture<>();
    try (Span span = tracer.span(httpRequest)) {
      if (this.callFactory == null) {
        this.callFactory = createTracedClient();
      }
      this.callFactory
          .newCall(request)
          .enqueue(
              new Callback() {

                @Override
                public void onResponse(@NotNull final Call call, @NotNull final Response response)
                    throws IOException {
                  future.complete(new OkHttpHttpResponse(httpRequest, response));
                }

                @Override
                public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                  future.completeExceptionally(e);
                }
              });
      tracer.attachSpanToFuture(span, future);
    }
    return future;
  }

  @Override
  public void setTracer(final Tracer tracer) {
    this.tracer = tracer;
    this.callFactory = createTracedClient();
  }

  /**
   * Build an OkHttp Request from an HttpRequest.
   *
   * @param request the HttpRequest
   * @return the OkHttp Request
   */
  private Request buildOkHttpRequest(final HttpRequest request) {
    Request.Builder requestBuilder = new Request.Builder().url(request.url());
    request
        .headers()
        .forEach(
            (key, values) -> {
              values.forEach(value -> requestBuilder.addHeader(key, value));
            });
    if (request.method().equals("GET")) {
      requestBuilder.get();
    } else {
      requestBuilder.method(
          request.method(),
          RequestBody.create(parse(javax.ws.rs.core.MediaType.APPLICATION_JSON), request.body()));
    }
    return requestBuilder.build();
  }

  /**
   * Build an HttpRequest from an OkHttp Request.
   *
   * @param request the OkHttp Request
   * @return the HttpRequest
   */
  private HttpRequest buildHttpRequest(final Request request) {
    return ImmutableHttpRequest.builder()
        .url(request.url().toString())
        .method(request.method())
        .headers(request.headers().toMultimap())
        .body(Optional.ofNullable(request.body()).map(RequestBody::toString).orElse(""))
        .build();
  }

  /**
   * Create a traced client based on the tracer.
   *
   * @return the traced client
   */
  private Call.Factory createTracedClient() {
    if (this.tracer == null || this.tracer instanceof NoopTracer) {
      return createTracedClientNoopTracer();
    }
    if (this.tracer instanceof OpenCensusTracer) {
      return createTracedClientOpenCensus();
    }
    if (this.tracer instanceof OpenTelemetryTracer) {
      return createTracedClientOpenTelemetry();
    }
    return createTracedClientNoopTracer();
  }

  /**
   * Create a traced client with a NoopTracer.
   *
   * @return the traced client
   */
  private Call.Factory createTracedClientNoopTracer() {
    return new Call.Factory() {
      @NotNull
      @Override
      public Call newCall(@NotNull final Request request) {
        return client.newCall(request);
      }
    };
  }

  /**
   * Create a traced client with OpenTelemetry.
   *
   * @return the traced client
   */
  private Call.Factory createTracedClientOpenTelemetry() {
    // OkHttpTelemetry is a helper class that provides a Call.Factory that can be used to trace
    return OkHttpTelemetry.builder(((OpenTelemetryTracer) this.tracer).getOpenTelemetry())
        .build()
        .newCallFactory(client);
  }

  /**
   * Create a traced client with OpenCensus.
   *
   * @return the traced client
   */
  private Call.Factory createTracedClientOpenCensus() {
    return new Call.Factory() {
      @NotNull
      @Override
      public Call newCall(@NotNull final Request request) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        Span span =
            OkHttpHttpClient.this
                .tracer
                .span(buildHttpRequest(request))
                .addTag(TraceHelper.TraceTags.HTTP_URL, request.url().toString());
        OkHttpClient.Builder okBuilder = client.newBuilder();
        // Add a network interceptor to trace the request
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
