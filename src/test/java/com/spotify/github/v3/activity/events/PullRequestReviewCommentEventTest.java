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

import java.io.IOException;
import static java.nio.charset.Charset.defaultCharset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;
import static com.google.common.io.Resources.getResource;
import com.spotify.github.jackson.Json;

public class PullRequestReviewCommentEventTest {
  @Test
  public void testDeserialization() throws IOException {
    String fixture =
        Resources.toString(
            getResource(this.getClass(), "fixtures/pull_request_review_comment_event.json"),
            defaultCharset());
    final PullRequestReviewCommentEvent event =
        Json.create().fromJson(fixture, PullRequestReviewCommentEvent.class);
    assertThat(event.action(), is("created"));
    assertThat(event.comment().id(), is(29724692L));
    assertThat(event.comment().nodeId(), is("abc234"));
    assertThat(event.pullRequest().nodeId(), is("abc123"));
    assertThat(event.comment().body(), is("Maybe you should use more emojji on this line."));
    assertThat(event.comment().originalCommitId(), is("0d1a26e67d8f5eaf1f6ba5c57fc3c7d91ac0fd1c"));
    assertThat(event.comment().originalLine(), is(1));
    assertThat(event.comment().originalPosition(), is(1));
    assertThat(event.comment().originalStartLine(), is(1));
    assertThat(event.comment().line(), is(1));
    assertThat(event.comment().side(), is("RIGHT"));
    assertThat(event.comment().startLine(), is(1));
    assertThat(event.comment().startSide(), is("RIGHT"));
    assertThat(event.comment().authorAssociation(), is("NONE"));
    assertThat(event.comment().pullRequestReviewId(), is(42L));
    assertThat(event.comment().inReplyToId(), is(426899381L));
  }
}
