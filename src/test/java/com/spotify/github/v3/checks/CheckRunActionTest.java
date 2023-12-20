/*-
 * -\-\-
 * github-client
 * --
 * Copyright (C) 2016 - 2022 Spotify AB
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

package com.spotify.github.v3.checks;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.spotify.github.v3.checks.ImmutableCheckRunAction.Builder;
import org.junit.jupiter.api.Test;

public class CheckRunActionTest {
  private Builder builder() {
    return ImmutableCheckRunAction.builder()
        .label("label")
        .identifier("identifier")
        .description("description");
  }

  @Test
  public void allowsCreationWithinLimits(){
    builder().build();

    builder()
        .label("a".repeat(20))
        .identifier("a".repeat(20))
        .description("a".repeat(40))
        .build();
  }

  @Test
  public void failsCreationWhenMaxLengthExceeded(){
    assertThrows(IllegalStateException.class, () ->
        builder().label("a".repeat(21)).build()
    );
    assertThrows(IllegalStateException.class, () ->
        builder().identifier("a".repeat(21)).build()
    );
    assertThrows(IllegalStateException.class, () ->
        builder().description("a".repeat(41)).build()
    );
  }
}
