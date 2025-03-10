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

import static com.spotify.github.tracing.TraceHelper.failSpan;
import static java.util.Objects.requireNonNull;

import com.spotify.github.tracing.Span;
import io.opentelemetry.api.trace.StatusCode;

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
    failSpan(this, t);
    span.setStatus(StatusCode.ERROR);
    return this;
  }

  @Override
  public void close() {
    span.end();
  }

  @Override
  public Span addTag(final String key, final String value) {
    this.span.setAttribute(key, value);
    return this;
  }

  @Override
  public Span addTag(final String key, final boolean value) {
    this.span.setAttribute(key, value);
    return this;
  }

  @Override
  public Span addTag(final String key, final long value) {
    this.span.setAttribute(key, value);
    return this;
  }

  @Override
  public Span addTag(final String key, final double value) {
    this.span.setAttribute(key, value);
    return this;
  }

  @Override
  public Span addEvent(final String description) {
    this.span.addEvent(description);
    return this;
  }
}
