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

import com.spotify.github.tracing.opentelemetry.OpenTelemetryTracer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import okhttp3.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpenTelemetryTracerTest {

  private final String rootSpanName = "root span";
  private static OtTestExportHandler spanExporterHandler;
  private final OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
  private final Tracer tracer = openTelemetry.getTracer("github-java-client-test");

  /**
   * Test that trace() a) returns a future that completes when the input future completes and b)
   * sets up the Spans appropriately so that the Span for the operation is exported with the
   * rootSpan set as the parent.
   */
  @ParameterizedTest
  @ValueSource(strings = {"GET", "POST", "PUT", "DELETE"})
  public void traceCompletionStageSimple(final String requestMethod) throws Exception {
    Span rootSpan = startRootSpan();
    final CompletableFuture<String> future = new CompletableFuture<>();
    OpenTelemetryTracer tracer = new OpenTelemetryTracer();

    tracer.span("path", requestMethod, future);
    future.complete("all done");
    rootSpan.end();

    List<SpanData> exportedSpans = spanExporterHandler.waitForSpansToBeExported(2);
    assertEquals(2, exportedSpans.size());

    SpanData root = findSpan(exportedSpans, rootSpanName);
    SpanData inner = findSpan(exportedSpans, "GitHub Request");

    assertEquals(root.getSpanContext().getTraceId(), inner.getSpanContext().getTraceId());
    assertEquals(root.getSpanContext().getSpanId(), inner.getParentSpanId());
    final Attributes attributes = inner.getAttributes();
    assertEquals("github-api-client", attributes.get(AttributeKey.stringKey("component")));
    assertEquals("github", attributes.get(AttributeKey.stringKey("peer.service")));
    assertEquals("path", attributes.get(AttributeKey.stringKey("http.url")));
    assertEquals(requestMethod, attributes.get(AttributeKey.stringKey("method")));
    assertEquals(StatusCode.OK, inner.getStatus().getStatusCode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"GET", "POST", "PUT", "DELETE"})
  public void traceCompletionStageFails(final String requestMethod) throws Exception {
    Span rootSpan = startRootSpan();
    final CompletableFuture<String> future = new CompletableFuture<>();
    OpenTelemetryTracer tracer = new OpenTelemetryTracer();

    tracer.span("path", requestMethod, future);
    future.completeExceptionally(new Exception("GitHub failed!"));
    rootSpan.end();

    List<SpanData> exportedSpans = spanExporterHandler.waitForSpansToBeExported(2);
    assertEquals(2, exportedSpans.size());

    SpanData root = findSpan(exportedSpans, rootSpanName);
    SpanData inner = findSpan(exportedSpans, "GitHub Request");

    assertEquals(root.getSpanContext().getTraceId(), inner.getSpanContext().getTraceId());
    assertEquals(root.getSpanContext().getSpanId(), inner.getParentSpanId());
    final Attributes attributes = inner.getAttributes();
    assertEquals("github-api-client", attributes.get(AttributeKey.stringKey("component")));
    assertEquals("github", attributes.get(AttributeKey.stringKey("peer.service")));
    assertEquals("path", attributes.get(AttributeKey.stringKey("http.url")));
    assertEquals(requestMethod, attributes.get(AttributeKey.stringKey("method")));
    assertEquals(StatusCode.UNSET, inner.getStatus().getStatusCode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"GET", "POST", "PUT", "DELETE"})
  public void traceCompletionStageWithRequest(final String requestMethod) throws Exception {
    Span rootSpan = startRootSpan();
    final CompletableFuture<String> future = new CompletableFuture<>();
    OpenTelemetryTracer tracer = new OpenTelemetryTracer();
    Request mockRequest = mock(Request.class);
    when(mockRequest.url())
        .thenReturn(HttpUrl.parse("https://api.github.com/repos/spotify/github-java-client"));
    when(mockRequest.method()).thenReturn(requestMethod);

    try (com.spotify.github.tracing.Span span = tracer.span(mockRequest)) {
      tracer.attachSpanToFuture(span, future);
      future.complete("all done");
    }
    rootSpan.end();

    List<SpanData> exportedSpans = spanExporterHandler.waitForSpansToBeExported(2);
    assertEquals(2, exportedSpans.size());

    SpanData root = findSpan(exportedSpans, rootSpanName);
    SpanData inner = findSpan(exportedSpans, "GitHub Request");

    assertEquals(root.getSpanContext().getTraceId(), inner.getSpanContext().getTraceId());
    assertEquals(root.getSpanContext().getSpanId(), inner.getParentSpanId());
    final Attributes attributes = inner.getAttributes();
    assertEquals("github-api-client", attributes.get(AttributeKey.stringKey("component")));
    assertEquals("github", attributes.get(AttributeKey.stringKey("peer.service")));
    assertEquals(
        "https://api.github.com/repos/spotify/github-java-client",
        attributes.get(AttributeKey.stringKey("http.url")));
    assertEquals(requestMethod, attributes.get(AttributeKey.stringKey("method")));
    assertEquals(StatusCode.OK, inner.getStatus().getStatusCode());
  }

  @Test
  public void createTracedClient() throws IOException {
    OpenTelemetryTracer tracer = new OpenTelemetryTracer(openTelemetry);
    OkHttpClient.Builder mockBuilder = mock(OkHttpClient.Builder.class);
    OkHttpClient mockClient = mock(OkHttpClient.class);
    LinkedList<Interceptor> interceptors = new LinkedList<>();
    when(mockClient.newBuilder()).thenReturn(mockBuilder);
    when(mockBuilder.build()).thenReturn(mockClient);
    when(mockBuilder.interceptors()).thenReturn(interceptors);
    when(mockBuilder.networkInterceptors()).thenReturn(interceptors);
    Call.Factory callFactory = tracer.createTracedClient(mockClient);
    assertNotNull(callFactory);
    assertEquals(
        "class io.opentelemetry.instrumentation.okhttp.v3_0.TracingCallFactory",
        callFactory.getClass().toString());
    assertEquals(3, interceptors.size());
  }

  private Span startRootSpan() {
    Span rootSpan = tracer.spanBuilder(rootSpanName).startSpan();
    Context context = Context.current().with(rootSpan);
    context.makeCurrent();
    return rootSpan;
  }

  private SpanData findSpan(final List<SpanData> spans, final String name) {
    return spans.stream().filter(s -> s.getName().equals(name)).findFirst().get();
  }

  @AfterEach
  public void flushSpans() {
    spanExporterHandler.flush();
  }

  @BeforeAll
  public static void setupTracing() {
    spanExporterHandler = new OtTestExportHandler();
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporterHandler))
            .setSampler(Sampler.alwaysOn())
            .build();
    OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();
  }
}
