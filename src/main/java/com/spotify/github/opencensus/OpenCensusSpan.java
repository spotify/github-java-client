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

package com.spotify.github.opencensus;
import static java.util.Objects.requireNonNull;
import com.spotify.github.Span;
import com.spotify.github.v3.exceptions.RequestNotOkException;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Status;

class OpenCensusSpan implements Span {

    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_SERVER_ERROR = 500;
    private final io.opencensus.trace.Span span;

    OpenCensusSpan(final io.opencensus.trace.Span span) {
        this.span = requireNonNull(span);
    }

    @Override
    public Span success() {
        span.setStatus(Status.OK);
        return this;
    }

    @Override
    public Span failure(final Throwable t) {
        if (t instanceof RequestNotOkException) {
            RequestNotOkException ex = (RequestNotOkException) t;
            span.putAttribute("http.status_code", AttributeValue.longAttributeValue(ex.statusCode()));
            span.putAttribute("message", AttributeValue.stringAttributeValue(ex.getMessage()));
            if (ex.statusCode() - INTERNAL_SERVER_ERROR >= 0) {
                span.putAttribute("error", AttributeValue.booleanAttributeValue(true));
            }
        }
        span.setStatus(Status.UNKNOWN);
        return this;
    }

    @Override
    public void close() {
        span.end();
    }
}

