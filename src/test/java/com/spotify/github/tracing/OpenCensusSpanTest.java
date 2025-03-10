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

import com.spotify.github.tracing.opencensus.OpenCensusSpan;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Status;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OpenCensusSpanTest {
  private final io.opencensus.trace.Span wrapped = mock(io.opencensus.trace.Span.class);

  @Test
  public void succeed() {
    final Span span = new OpenCensusSpan(wrapped);
    span.success();
    span.close();

    verify(wrapped).setStatus(Status.OK);
    verify(wrapped).end();
  }

  @Test
  public void fail() {
    final Span span = new OpenCensusSpan(wrapped);
    span.failure(
        new RequestNotOkException("method", "path", 404, "Not found", Collections.emptyMap()));
    span.close();

    verify(wrapped).setStatus(Status.UNKNOWN);
    verify(wrapped).putAttribute("http.status_code", AttributeValue.longAttributeValue(404));
    verify(wrapped).end();
  }

  @Test
  public void failOnServerError() {
    final Span span = new OpenCensusSpan(wrapped);
    span.failure(
        new RequestNotOkException(
            "method", "path", 500, "Internal Server Error", Collections.emptyMap()));
    span.close();

    verify(wrapped).setStatus(Status.UNKNOWN);
    verify(wrapped).putAttribute("http.status_code", AttributeValue.longAttributeValue(500));
    verify(wrapped).putAttribute("error", AttributeValue.booleanAttributeValue(true));
    verify(wrapped).end();
  }

  @Test
  public void addTags() {
    final Span span = new OpenCensusSpan(wrapped);
    span.addTag("key", "value");
    span.addTag("key", true);
    span.addTag("key", 42L);
    span.close();

    verify(wrapped).putAttribute("key", AttributeValue.stringAttributeValue("value"));
    verify(wrapped).putAttribute("key", AttributeValue.booleanAttributeValue(true));
    verify(wrapped).putAttribute("key", AttributeValue.longAttributeValue(42L));
    verify(wrapped).end();
  }

  @Test
  public void addEvent() {
    final Span span = new OpenCensusSpan(wrapped);
    span.addEvent("description");
    span.close();

    verify(wrapped).addAnnotation("description");
    verify(wrapped).end();
  }

  @Test
  @SuppressWarnings("deprecation")
  public void succeedDeprecated() {
    final Span span = new com.spotify.github.opencensus.OpenCensusSpan(wrapped);
    span.success();
    span.close();

    verify(wrapped).setStatus(Status.OK);
    verify(wrapped).end();
  }

  @Test
  @SuppressWarnings("deprecation")
  public void failDeprecated() {
    final Span span = new com.spotify.github.opencensus.OpenCensusSpan(wrapped);
    span.failure(
        new RequestNotOkException("method", "path", 404, "Not found", Collections.emptyMap()));
    span.close();

    verify(wrapped).setStatus(Status.UNKNOWN);
    verify(wrapped).putAttribute("http.status_code", AttributeValue.longAttributeValue(404));
    verify(wrapped).end();
  }

  @Test
  @SuppressWarnings("deprecation")
  public void failOnServerErrorDeprecated() {
    final Span span = new com.spotify.github.opencensus.OpenCensusSpan(wrapped);
    span.failure(
        new RequestNotOkException(
            "method", "path", 500, "Internal Server Error", Collections.emptyMap()));
    span.close();

    verify(wrapped).setStatus(Status.UNKNOWN);
    verify(wrapped).putAttribute("http.status_code", AttributeValue.longAttributeValue(500));
    verify(wrapped).putAttribute("error", AttributeValue.booleanAttributeValue(true));
    verify(wrapped).end();
  }

  @Test
  @SuppressWarnings("deprecation")
  public void addTagsDeprecated() {
    final Span span = new com.spotify.github.opencensus.OpenCensusSpan(wrapped);
    span.addTag("key", "value");
    span.addTag("key", true);
    span.addTag("key", 42L);
    span.close();

    verify(wrapped).putAttribute("key", AttributeValue.stringAttributeValue("value"));
    verify(wrapped).putAttribute("key", AttributeValue.booleanAttributeValue(true));
    verify(wrapped).putAttribute("key", AttributeValue.longAttributeValue(42L));
    verify(wrapped).end();
  }

  @Test
  @SuppressWarnings("deprecation")
  public void addEventDeprecated() {
    final Span span = new com.spotify.github.opencensus.OpenCensusSpan(wrapped);
    span.addEvent("description");
    span.close();

    verify(wrapped).addAnnotation("description");
    verify(wrapped).end();
  }
}
