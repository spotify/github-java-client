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

package com.spotify.github.v3.activity.events;

import java.io.IOException;
import static java.nio.charset.Charset.defaultCharset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;
import static com.google.common.io.Resources.getResource;
import com.spotify.github.jackson.Json;

public class IssueCommentEventTest {

  @Test
  public void testDeserialization() throws IOException {
    String fixture =
        Resources.toString(
            getResource(this.getClass(), "fixtures/issue_comment_event.json"), defaultCharset());
    final IssueCommentEvent event = Json.create().fromJson(fixture, IssueCommentEvent.class);
    assertThat(event.action(), is("created"));
    assertThat(event.issue().number(), is(2L));
    assertThat(event.comment().id(), is(99262140L));
    assertThat(event.comment().nodeId(), is("asd123"));
    assertThat(
        event.comment().body(), is("You are totally right! I'll get this fixed right away."));
  }
}
