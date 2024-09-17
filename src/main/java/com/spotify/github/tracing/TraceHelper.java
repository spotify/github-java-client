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

public class TraceHelper {
    // Tracing Headers
    public static final String HEADER_CLOUD_TRACE_CONTEXT = "X-Cloud-Trace-Context";
    public static final String HEADER_TRACE_PARENT = "traceparent";
    public static final String HEADER_TRACE_STATE = "tracestate";

    // Private constructor to prevent instantiation
    private TraceHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
