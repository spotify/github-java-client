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

package com.spotify.github.opencensus;


import io.grpc.Context;
import io.opencensus.trace.*;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.samplers.Samplers;
import io.opencensus.trace.unsafe.ContextUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.opencensus.trace.AttributeValue.stringAttributeValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpenCensusTracerTest {


    private final String rootSpanName = "root span";
    private TestExportHandler spanExporterHandler;

    /**
     * Test that trace() a) returns a future that completes when the input future completes and b)
     * sets up the Spans appropriately so that the Span for the operation is exported with the
     * rootSpan set as the parent.
     */
    @Test
    public void testTrace_CompletionStage_Simple() throws Exception {
        Span rootSpan = startRootSpan();
        final CompletableFuture<String> future = new CompletableFuture<>();
        OpenCensusTracer tracer = new OpenCensusTracer();

        tracer.span("path", "GET", future);
        future.complete("all done");
        rootSpan.end();

        List<SpanData> exportedSpans = spanExporterHandler.waitForSpansToBeExported(2);
        assertEquals(2, exportedSpans.size());

        SpanData root = findSpan(exportedSpans, rootSpanName);
        SpanData inner = findSpan(exportedSpans, "GitHub Request");

        assertEquals(root.getContext().getTraceId(), inner.getContext().getTraceId());
        assertEquals(root.getContext().getSpanId(), inner.getParentSpanId());
        final Map<String, AttributeValue> attributes = inner.getAttributes().getAttributeMap();
        assertEquals(stringAttributeValue("github-api-client"), attributes.get("component"));
        assertEquals(stringAttributeValue("github"), attributes.get("peer.service"));
        assertEquals(stringAttributeValue("path"), attributes.get("http.url"));
        assertEquals(stringAttributeValue("GET"), attributes.get("method"));
        assertEquals(Status.OK, inner.getStatus());
    }

    @Test
    public void testTrace_CompletionStage_Fails() throws Exception {
        Span rootSpan = startRootSpan();
        final CompletableFuture<String> future = new CompletableFuture<>();
        OpenCensusTracer tracer = new OpenCensusTracer();

        tracer.span("path", "POST", future);
        future.completeExceptionally(new Exception("GitHub failed!"));
        rootSpan.end();

        List<SpanData> exportedSpans = spanExporterHandler.waitForSpansToBeExported(2);
        assertEquals(2, exportedSpans.size());

        SpanData root = findSpan(exportedSpans, rootSpanName);
        SpanData inner = findSpan(exportedSpans, "GitHub Request");

        assertEquals(root.getContext().getTraceId(), inner.getContext().getTraceId());
        assertEquals(root.getContext().getSpanId(), inner.getParentSpanId());
        final Map<String, AttributeValue> attributes = inner.getAttributes().getAttributeMap();
        assertEquals(stringAttributeValue("github-api-client"), attributes.get("component"));
        assertEquals(stringAttributeValue("github"), attributes.get("peer.service"));
        assertEquals(stringAttributeValue("path"), attributes.get("http.url"));
        assertEquals(stringAttributeValue("POST"), attributes.get("method"));
        assertEquals(Status.UNKNOWN, inner.getStatus());
    }

    private Span startRootSpan() {
        Span rootSpan = Tracing.getTracer().spanBuilder(rootSpanName).startSpan();
        Context context = ContextUtils.withValue(Context.current(), rootSpan);
        context.attach();
        return rootSpan;
    }

    private SpanData findSpan(final List<SpanData> spans, final String name) {
        return spans.stream().filter(s -> s.getName().equals(name)).findFirst().get();
    }

    @Before
    public void setUpExporter() {
        spanExporterHandler = new TestExportHandler();
        Tracing.getExportComponent().getSpanExporter().registerHandler("test", spanExporterHandler);
    }

    @BeforeClass
    public static void setupTracing() {
        final TraceConfig traceConfig = Tracing.getTraceConfig();
        final Sampler sampler = Samplers.alwaysSample();
        final TraceParams newParams =
                traceConfig.getActiveTraceParams().toBuilder().setSampler(sampler).build();
        traceConfig.updateActiveTraceParams(newParams);
    }
}