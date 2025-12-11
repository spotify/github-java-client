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
import static org.hamcrest.core.IsNull.notNullValue;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class MergeGroupEventTest {
  @Test
  public void testDeserialization() throws IOException {
    String fixture =
        Resources.toString(
            getResource(this.getClass(), "fixtures/merge_group_event.json"),
            defaultCharset());
    final MergeGroupEvent mergeGroupEvent =
        Json.create().fromJson(fixture, MergeGroupEvent.class);
    assertThat(mergeGroupEvent.action(), is("checks_requested"));
    assertThat(mergeGroupEvent.mergeGroup(), notNullValue());
    assertThat(mergeGroupEvent.mergeGroup().headSha(), is("cd84187b3e9a3e8f5b5f5b5f5b5f5b5f5b5f5b5f"));
    assertThat(mergeGroupEvent.mergeGroup().headRef(), is("refs/heads/gh-readonly-queue/main/pr-123-cd84187b3e9a3e8f5b5f5b5f5b5f5b5f5b5f5b5f"));
    assertThat(mergeGroupEvent.mergeGroup().baseSha(), is("9049f1265b7d61be4a8904a9a27120d2064dab3b"));
    assertThat(mergeGroupEvent.mergeGroup().baseRef(), is("refs/heads/main"));
    assertThat(mergeGroupEvent.repository(), notNullValue());
    assertThat(mergeGroupEvent.repository().name(), is("public-repo"));
    assertThat(mergeGroupEvent.repository().owner().login(), is("baxterthehacker"));
  }
}