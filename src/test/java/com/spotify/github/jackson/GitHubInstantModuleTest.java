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

package com.spotify.github.jackson;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.spotify.github.GitHubInstant;
import java.io.IOException;
import java.time.Instant;
import org.junit.Before;
import org.junit.Test;

public class GitHubInstantModuleTest {

  private Json mapper;

  @Before
  public void setUp() throws Exception {
    mapper = Json.create();
  }

  @Test
  public void shouldSerializeToCreationType() throws IOException {
    final long epoch = 123456789L;
    final Instant instant = Instant.ofEpochSecond(epoch);
    final GitHubInstant gitHubInstant = GitHubInstant.create(instant);
    final GitHubInstant gitHubEpoch = GitHubInstant.create(epoch);
    final String json = "{\"long_value\":123456789,\"instant_value\":\"1973-11-29T21:33:09Z\"}";

    final Foo foo =
        ImmutableFoo.builder().instantValue(gitHubInstant).longValue(gitHubEpoch).build();

    assertThat(mapper.toJsonUnchecked(foo), is(json));
    assertThat(mapper.toJsonUnchecked(mapper.fromJsonUnchecked(json, Foo.class)), is(json));
  }
}
