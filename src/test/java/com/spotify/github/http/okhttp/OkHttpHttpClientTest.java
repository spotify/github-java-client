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

import com.spotify.github.http.HttpRequest;
import com.spotify.github.http.HttpResponse;
import com.spotify.github.http.ImmutableHttpRequest;
import com.spotify.github.tracing.Tracer;
import okhttp3.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OkHttpHttpClientTest {
  private static OkHttpClient okHttpClient;
  private static OkHttpHttpClient httpClient;
  private static Tracer tracer;

  @BeforeAll
  static void setUp() {
    okHttpClient = mock(OkHttpClient.class);
    tracer = mock(Tracer.class);
    httpClient = new OkHttpHttpClient(okHttpClient, tracer);
  }

  @AfterEach
  void tearDown() {
    reset(okHttpClient, tracer);
  }

  @Test
  void sendSuccessfully() throws IOException {
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

    // When
    CompletableFuture<HttpResponse> futureResponse = httpClient.send(httpRequest);
    capture.getValue().onResponse(call, response);
    HttpResponse httpResponse = futureResponse.join();

    // Then
    assertNotNull(httpResponse);
    assertEquals("{\"foo\":\"bar\"}", httpResponse.bodyString());
    assertEquals(200, httpResponse.statusCode());
    assertEquals("foo", httpResponse.statusMessage());
    assertTrue(httpResponse.isSuccessful());
    verify(tracer, times(1)).span(any(HttpRequest.class));
  }

  @Test
  void sendWithException() {
    // Given
    final Call call = mock(Call.class);
    final ArgumentCaptor<Callback> capture = ArgumentCaptor.forClass(Callback.class);
    doNothing().when(call).enqueue(capture.capture());
    final IOException exception = new IOException("Network error");

    HttpRequest httpRequest = ImmutableHttpRequest.builder().url("https://example.com").build();
    when(okHttpClient.newCall(any())).thenReturn(call);

    // When
    CompletableFuture<HttpResponse> futureResponse = httpClient.send(httpRequest);
    capture.getValue().onFailure(call, exception);

    // Then
    assertThrows(CompletionException.class, futureResponse::join);
    verify(tracer, times(1)).span(any(HttpRequest.class));
  }

  @Test
  void sendWithClientError() throws IOException {
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

    // When
    CompletableFuture<HttpResponse> futureResponse = httpClient.send(httpRequest);
    capture.getValue().onResponse(call, response);
    HttpResponse httpResponse = futureResponse.join();

    // Then
    assertNotNull(httpResponse);
    assertEquals("{\"error\":\"Not Found\"}", httpResponse.bodyString());
    assertEquals(404, httpResponse.statusCode());
    assertEquals("Not Found", httpResponse.statusMessage());
    assertFalse(httpResponse.isSuccessful());
    verify(tracer, times(1)).span(any(HttpRequest.class));
  }
}
