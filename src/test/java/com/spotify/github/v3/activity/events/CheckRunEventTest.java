/*-
 * -\-\-
 * github-client
 * --
 * Copyright (C) 2016 - 2020 Spotify AB
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

package com.spotify.github.v3.activity.events;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class CheckRunEventTest {

    @Test
    public void testDeserialization() throws IOException {
        // sample payload from https://docs.github.com/en/developers/webhooks-and-events/webhooks/webhook-events-and-payloads
        String fixture =
                Resources.toString(
                        getResource(this.getClass(), "fixtures/check_run_event.json"), defaultCharset());
        final CheckRunEvent checkRunEvent = Json.create().fromJson(fixture, CheckRunEvent.class);
        assertThat(checkRunEvent.action(), is("created"));
        assertThat(checkRunEvent.checkRun().name(), is("Octocoders-linter"));
        assertThat(checkRunEvent.repository().name(), is("Hello-World"));
        assertThat(checkRunEvent.checkRun().checkSuite().get().headBranch().get(), is("changes"));
    }

}