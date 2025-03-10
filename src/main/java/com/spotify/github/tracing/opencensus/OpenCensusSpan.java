/*-
 * -\-\-
 * github-api
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

package com.spotify.github.tracing.opencensus;

import static com.spotify.github.tracing.TraceHelper.failSpan;
import static java.util.Objects.requireNonNull;

import com.spotify.github.tracing.Span;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Status;

public class OpenCensusSpan implements Span {
  private final io.opencensus.trace.Span span;

  public OpenCensusSpan(final io.opencensus.trace.Span span) {
    this.span = requireNonNull(span);
  }

  @Override
  public Span success() {
    span.setStatus(Status.OK);
    return this;
  }

  @Override
  public Span failure(final Throwable t) {
    failSpan(this, t);
    span.setStatus(Status.UNKNOWN);
    return this;
  }

  @Override
  public void close() {
    span.end();
  }

  @Override
  public Span addTag(final String key, final String value) {
    this.span.putAttribute(key, AttributeValue.stringAttributeValue(value));
    return this;
  }

  @Override
  public Span addTag(final String key, final boolean value) {
    this.span.putAttribute(key, AttributeValue.booleanAttributeValue(value));
    return this;
  }

  @Override
  public Span addTag(final String key, final long value) {
    this.span.putAttribute(key, AttributeValue.longAttributeValue(value));
    return this;
  }

  @Override
  public Span addTag(final String key, final double value) {
    this.span.putAttribute(key, AttributeValue.doubleAttributeValue(value));
    return this;
  }

  @Override
  public Span addEvent(final String description) {
    this.span.addAnnotation(description);
    return this;
  }
}
