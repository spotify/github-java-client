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

package com.spotify.github.v3.search.requests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

public class SearchParametersTest {

  @Test
  public void testFullSerialize() {
    final SearchParameters params =
        ImmutableSearchParameters.builder()
            .q("bogus-query")
            .sort("bogus-sort")
            .order("bogus-order")
            .build();

    assertThat(params.serialize(), is("order=bogus-order&q=bogus-query&sort=bogus-sort"));
  }
}
