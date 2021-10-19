/*-
 * -\-\-
 * github-client
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

import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dummy SpanExporter.Handler which keeps any exported Spans in memory, so we can query against
 * them in tests.
 *
 * <p>The opencensus-testing library has a TestHandler that can be used in tests like this, but the
 * only method it exposes to gain access to the received spans is waitForExport(int) which blocks
 * forever until the given number of spans is exported, which could be never. So instead we define
 * our own very simple implementation.
 */
class TestExportHandler extends SpanExporter.Handler {
    private static final Logger LOG = LoggerFactory.getLogger(TestExportHandler.class);

    private final List<SpanData> receivedSpans = new ArrayList<>();
    private final Object lock = new Object();

    @Override
    public void export(final Collection<SpanData> spanDataList) {
        synchronized (lock) {
            receivedSpans.addAll(spanDataList);
            LOG.info("received {} spans, {} total", spanDataList.size(), receivedSpans.size());
        }
    }

    List<SpanData> receivedSpans() {
        synchronized (lock) {
            return new ArrayList<>(receivedSpans);
        }
    }

    /** Wait up to waitTime for at least `count` spans to be exported */
    List<SpanData> waitForSpansToBeExported(final int count) throws InterruptedException {
        // opencensus is hardcoded to export batches every 5 seconds (see
        // io.opencensus.implcore.trace.export.ExportComponentImpl), so wait slightly longer than that
        Duration waitTime = Duration.ofSeconds(7);
        Instant deadline = Instant.now().plus(waitTime);

        List<SpanData> spanData = receivedSpans();
        while (spanData.size() < count) {
            //noinspection BusyWait
            Thread.sleep(100);
            spanData = receivedSpans();

            if (!Instant.now().isBefore(deadline)) {
                LOG.warn("ending busy wait for spans because deadline passed");
                break;
            }
        }
        return spanData;
    }
}
