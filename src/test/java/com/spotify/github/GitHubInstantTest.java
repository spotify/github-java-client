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

package com.spotify.github;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import org.junit.Test;

public class GitHubInstantTest {

  @Test
  public void createdFromNumberShouldReturnLongClass() throws Exception {
    final int intNum = 123456789;
    final long longNum = 123456789L;
    assertThat(GitHubInstant.create(intNum).type(), equalTo(Long.class));
    assertThat(GitHubInstant.create(longNum).type(), equalTo(Long.class));
  }

  @Test
  public void createdFromInstantShouldReturnInstantClass() throws Exception {
    assertThat(
        GitHubInstant.create(Instant.ofEpochSecond(123456789)).type(), equalTo(Instant.class));
  }

  @Test
  public void createdFromInstantShouldReturnBothInstantAndEpoch() throws Exception {
    final long epoch = 123456789L;
    final Instant instant = Instant.ofEpochSecond(epoch);
    final GitHubInstant gitHubInstant = GitHubInstant.create(instant);
    assertThat(gitHubInstant.epoch(), is(epoch));
    assertThat(gitHubInstant.instant(), is(instant));
  }

  @Test
  public void createdFromLongShouldReturnBothInstantAndEpoch() throws Exception {
    final long epoch = 123456789L;
    final GitHubInstant gitHubInstant = GitHubInstant.create(epoch);
    assertThat(gitHubInstant.epoch(), is(epoch));
    assertThat(gitHubInstant.instant(), is(Instant.ofEpochSecond(epoch)));
  }
}
