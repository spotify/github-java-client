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

package com.spotify.github.v3.prs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.collect.ImmutableList;
import com.spotify.github.jackson.Json;
import org.junit.Test;

public class RequestReviewParametersTest {

  @Test
  public void testFullSerialize() {
    final RequestReviewParameters params =
        ImmutableRequestReviewParameters.builder()
            .reviewers(ImmutableList.of("foo", "bar"))
            .teamReviewers(ImmutableList.of("fox", "dog"))
            .build();

    final String expected = "{"
                            + "\"reviewers\":[\"foo\",\"bar\"],"
                            + "\"team_reviewers\":[\"fox\",\"dog\"]"
                            + "}";

    assertThat(
        Json.create().toJsonUnchecked(params),
        is(expected));
  }
}
