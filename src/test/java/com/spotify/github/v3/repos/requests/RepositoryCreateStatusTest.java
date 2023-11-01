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

package com.spotify.github.v3.repos.requests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.repos.StatusState;
import java.net.URI;
import org.junit.jupiter.api.Test;

public class RepositoryCreateStatusTest {

  @Test
  public void testSerializeStatusCreateRequest() throws JsonProcessingException {

    final RepositoryCreateStatus request =
        ImmutableRepositoryCreateStatus.builder()
            .state(StatusState.ERROR)
            .context("Jenkins")
            .description("Testing state")
            .targetUrl(URI.create("http://my.jenkins.com/somepr"))
            .build();

    assertThat(
        Json.create().toJson(request),
        is(
            equalTo(
                "{\"state\":\"error\",\"target_url\":\"http://my.jenkins.com/somepr\",\"description\":\"Testing state\",\"context\":\"Jenkins\"}")));
  }
}
