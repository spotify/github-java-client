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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.spotify.github.http.HttpRequest;
import com.spotify.github.http.HttpResponse;
import com.spotify.github.http.ImmutableHttpRequest;
import com.spotify.github.tracing.NoopTracer;
import com.spotify.github.tracing.Span;
import com.spotify.github.tracing.TraceHelper;
import com.spotify.github.tracing.Tracer;
import com.spotify.github.tracing.opencensus.OpenCensusTracer;
import com.spotify.github.tracing.opentelemetry.OpenTelemetryTracer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;
import okhttp3.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

class OkHttpHttpClientTest {
  private static final OkHttpClient okHttpClient = mock(OkHttpClient.class);
  private static final OkHttpClient.Builder mockOkHttpClientBuilder =
      mock(OkHttpClient.Builder.class);
  private static final Tracer noopTracer = mock(NoopTracer.class);
  private static final Tracer ocTracer = mock(OpenCensusTracer.class);
  private static final Tracer otTracer = mock(OpenTelemetryTracer.class);
  private static final Span mockSpan = mock(Span.class);
  private static final Call.Factory mockCallFactory = mock(Call.Factory.class);

  private static OkHttpHttpClient httpClient;

  static Stream<Tracer> tracers() {
    return Stream.of(noopTracer, ocTracer, otTracer);
  }

  @BeforeAll
  static void setUp() {
    httpClient =
        new OkHttpHttpClient(okHttpClient, noopTracer) {
          @Override
          protected Call.Factory createTracedClientOpenTelemetry() {
            return mockCallFactory;
          }
        };
  }

  @BeforeEach
  void setUpEach() {
    List<Interceptor> interceptors = new ArrayList<>();
    when(okHttpClient.newBuilder()).thenReturn(mockOkHttpClientBuilder);
    when(mockOkHttpClientBuilder.networkInterceptors()).thenReturn(interceptors);
    when(mockOkHttpClientBuilder.build()).thenReturn(okHttpClient);
  }

  @AfterEach
  void tearDown() {
    reset(okHttpClient, noopTracer, ocTracer, otTracer, mockSpan);
  }

  @ParameterizedTest
  @MethodSource("tracers")
  void sendSuccessfully(Tracer tracer) throws IOException {
    // Given
    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> capture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(capture.capture());
    final Response response =
        new okhttp3.Response.Builder()
            .code(200)
            .body(ResponseBody.create(MediaType.get("application/json"), "{\"foo\":\"bar\"}"))
            .message("foo")
            .protocol(Protocol.HTTP_1_1)
            .request(new Request.Builder().url("https://example.com").build())
            .build();

    HttpRequest httpRequest = ImmutableHttpRequest.builder().url("https://example.com").build();
    when(okHttpClient.newCall(any())).thenReturn(call);
    when(mockCallFactory.newCall(any())).thenReturn(call);

    when(tracer.span(any())).thenReturn(mockSpan);

    // When
    httpClient.setTracer(tracer);
    CompletableFuture<HttpResponse> futureResponse = httpClient.send(httpRequest);
    capture.getValue().onResponse(call, response);
    HttpResponse httpResponse = futureResponse.join();

    // Then
    assertNotNull(httpResponse);
    assertEquals("{\"foo\":\"bar\"}", httpResponse.bodyString());
    assertEquals(200, httpResponse.statusCode());
    assertEquals("foo", httpResponse.statusMessage());
    assertTrue(httpResponse.isSuccessful());
    if (tracer instanceof NoopTracer || tracer instanceof OpenTelemetryTracer) {
      verify(tracer, times(1)).span(any(HttpRequest.class));

    } else if (tracer instanceof OpenCensusTracer) {
      verify(tracer, times(2)).span(any(HttpRequest.class));
      verify(mockSpan).addTag(TraceHelper.TraceTags.HTTP_URL, "https://example.com/");
    }
    verify(mockSpan, times(1)).close();
  }

  @ParameterizedTest
  @MethodSource("tracers")
  void sendWithException(Tracer tracer) {
    // Given
    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> capture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(capture.capture());
    final IOException exception = new IOException("Network error");

    HttpRequest httpRequest = ImmutableHttpRequest.builder().url("https://example.com").build();
    when(okHttpClient.newCall(any())).thenReturn(call);
    when(mockCallFactory.newCall(any())).thenReturn(call);
    when(tracer.span(any())).thenReturn(mockSpan);

    // When
    httpClient.setTracer(tracer);
    CompletableFuture<HttpResponse> futureResponse = httpClient.send(httpRequest);
    capture.getValue().onFailure(call, exception);

    // Then
    assertThrows(CompletionException.class, futureResponse::join);
    if (tracer instanceof NoopTracer || tracer instanceof OpenTelemetryTracer) {
      verify(tracer, times(1)).span(any(HttpRequest.class));

    } else if (tracer instanceof OpenCensusTracer) {
      verify(tracer, times(2)).span(any(HttpRequest.class));
      verify(mockSpan).addTag(TraceHelper.TraceTags.HTTP_URL, "https://example.com/");
    }
    verify(mockSpan, times(1)).close();
  }

  @ParameterizedTest
  @MethodSource("tracers")
  void sendWithClientError(Tracer tracer) throws IOException {
    // Given
    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> capture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(capture.capture());
    final Response response =
        new okhttp3.Response.Builder()
            .code(404)
            .body(
                ResponseBody.create(MediaType.get("application/json"), "{\"error\":\"Not Found\"}"))
            .message("Not Found")
            .protocol(Protocol.HTTP_1_1)
            .request(new Request.Builder().url("https://example.com").build())
            .build();

    HttpRequest httpRequest = ImmutableHttpRequest.builder().url("https://example.com").build();
    when(okHttpClient.newCall(any())).thenReturn(call);
    when(mockCallFactory.newCall(any())).thenReturn(call);
    when(tracer.span(any())).thenReturn(mockSpan);

    // When
    httpClient.setTracer(tracer);
    CompletableFuture<HttpResponse> futureResponse = httpClient.send(httpRequest);
    capture.getValue().onResponse(call, response);
    HttpResponse httpResponse = futureResponse.join();

    // Then
    assertNotNull(httpResponse);
    assertEquals("{\"error\":\"Not Found\"}", httpResponse.bodyString());
    assertEquals(404, httpResponse.statusCode());
    assertEquals("Not Found", httpResponse.statusMessage());
    assertFalse(httpResponse.isSuccessful());
    if (tracer instanceof NoopTracer || tracer instanceof OpenTelemetryTracer) {
      verify(tracer, times(1)).span(any(HttpRequest.class));

    } else if (tracer instanceof OpenCensusTracer) {
      verify(tracer, times(2)).span(any(HttpRequest.class));
      verify(mockSpan).addTag(TraceHelper.TraceTags.HTTP_URL, "https://example.com/");
    }
    verify(mockSpan, times(1)).close();
  }
}
