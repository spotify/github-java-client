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

package com.spotify.github.opencensus;

import com.spotify.github.Span;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Status;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OpenCensusSpanTest {
    private io.opencensus.trace.Span wrapped = mock(io.opencensus.trace.Span.class);

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
        span.failure(new RequestNotOkException("path", 404, "Not found"));
        span.close();

        verify(wrapped).setStatus(Status.UNKNOWN);
        verify(wrapped).putAttribute("http.status_code", AttributeValue.longAttributeValue(404));
        verify(wrapped).end();
    }

    @Test
    public void failOnServerError() {
        final Span span = new OpenCensusSpan(wrapped);
        span.failure(new RequestNotOkException("path", 500, "Internal Server Error"));
        span.close();

        verify(wrapped).setStatus(Status.UNKNOWN);
        verify(wrapped).putAttribute("http.status_code", AttributeValue.longAttributeValue(500));
        verify(wrapped).putAttribute("error", AttributeValue.booleanAttributeValue(true));
        verify(wrapped).end();
    }

}
