/*-
 * -\-\-
 * github-api
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

package com.spotify.github.hooks;

import static com.spotify.github.FixtureHelper.loadFixture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.spotify.github.jackson.Json;
import com.spotify.github.v3.activity.events.PullRequestEvent;
import java.io.IOException;
import org.junit.Test;

public class PullRequestEventTest {

  @Test
  public void testParsingPullRequestClosedEventWithoutLabelOrUser() throws IOException {
    final String fixture = loadFixture("hooks/requests/pull-request-closed.json");
    final PullRequestEvent prEvent = Json.create().fromJson(fixture, PullRequestEvent.class);

    assertThat(prEvent.pullRequest().head().label().isPresent(), is(false));
  }
}
