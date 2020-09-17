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

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class ReviewRequestsTest {

  private String fixture;

  public static final void assertRequiredReviews(final ReviewRequests reviewRequests) {
    assertThat(reviewRequests.users().size(), is(1));
    assertThat(reviewRequests.users().get(0).login(), is("octocat"));
    assertThat(reviewRequests.teams().size(), is(1));
    assertThat(reviewRequests.teams().get(0).slug(), is("justice-league"));
  }

  @Before
  public void setUp() throws Exception {
    fixture = Resources.toString(getResource(this.getClass(), "required_reviews.json"),
        defaultCharset());
  }

  @Test
  public void testDeserialization() throws IOException {
    final ReviewRequests reviewRequests = Json.create().fromJson(fixture, ReviewRequests.class);
    assertRequiredReviews(reviewRequests);
  }
}
