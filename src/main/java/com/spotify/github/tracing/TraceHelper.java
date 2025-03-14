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

import com.spotify.github.v3.exceptions.RequestNotOkException;

public class TraceHelper {
  // Tracing Headers
  public static final String HEADER_CLOUD_TRACE_CONTEXT = "X-Cloud-Trace-Context";
  public static final String HEADER_TRACE_PARENT = "traceparent";
  public static final String HEADER_TRACE_STATE = "tracestate";

  public static final int NOT_FOUND = 404;
  public static final int INTERNAL_SERVER_ERROR = 500;

  // Private constructor to prevent instantiation
  private TraceHelper() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static Span failSpan(final Span span, final Throwable t) {
    if (t instanceof RequestNotOkException) {
      RequestNotOkException ex = (RequestNotOkException) t;
      span.addTag(TraceHelper.TraceTags.HTTP_STATUS_CODE, ex.statusCode())
          .addTag(TraceHelper.TraceTags.ERROR_MESSAGE, ex.getRawMessage());
      if (ex.statusCode() - INTERNAL_SERVER_ERROR >= 0) {
        span.addTag(TraceHelper.TraceTags.ERROR, true);
      }
    } else {
      if (t != null) {
        span.addTag(TraceHelper.TraceTags.ERROR_MESSAGE, t.getMessage());
      }
      span.addTag(TraceHelper.TraceTags.ERROR, true);
    }
    return span;
  }

  public static class TraceTags {
    public static final String COMPONENT = "component";
    public static final String PEER_SERVICE = "peer.service";
    public static final String HTTP_URL = "http.url";
    public static final String HTTP_METHOD = "method";
    public static final String HTTP_STATUS_CODE = "http.status_code";
    public static final String HTTP_STATUS_MESSAGE = "http.status_message";
    public static final String ERROR_MESSAGE = "message";
    public static final String ERROR = "error";
  }
}
