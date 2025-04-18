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

import com.spotify.github.http.HttpRequest;
import java.util.concurrent.CompletionStage;

public interface Tracer {

  /** Create scoped span. Span will be closed when future completes. */
  Span span(String path, String method, CompletionStage<?> future);

  Span span(String path, String method);

  Span span(HttpRequest request);

  Span span(HttpRequest request, CompletionStage<?> future);

  void attachSpanToFuture(Span span, CompletionStage<?> future);
}
