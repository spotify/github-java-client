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

package com.spotify.github.v3.clients;
import com.spotify.github.Span;
import com.spotify.github.Tracer;

import java.util.concurrent.CompletionStage;

public class NoopTracer implements Tracer {

    public static final NoopTracer INSTANCE = new NoopTracer();
    private static final Span SPAN =
            new Span() {
                @Override
                public Span success() {
                    return this;
                }

                @Override
                public Span failure(final Throwable t) {
                    return this;
                }

                @Override
                public void close() {}
            };

    private NoopTracer() {}

    @Override
    public Span span(
            final String path,
            final String method,
            final CompletionStage<?> future) {
        return SPAN;
    }

}

