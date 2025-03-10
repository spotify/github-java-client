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

import io.opencensus.trace.Span;

/**
 * OpenCensusSpan is a wrapper around OpenCensus Span. This class is kept for backward
 * compatibility.
 *
 * @deprecated This class has been moved to the package com.spotify.github.tracing.opencensus.
 *             Please use com.spotify.github.tracing.opencensus.OpenCensusSpan instead.
 */
@Deprecated
public class OpenCensusSpan extends com.spotify.github.tracing.opencensus.OpenCensusSpan {
  public OpenCensusSpan(final Span span) {
    super(span);
  }
  // This class is kept for backward compatibility
}
