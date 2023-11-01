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

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class CheckSuiteTest {

  @Test
  public void testDeserialization() throws IOException {
    // sample payload from https://docs.github.com/en/rest/checks/suites#list-check-suites-for-a-git-reference
    String fixture =
        Resources.toString(
            getResource(this.getClass(), "check-suites-response.json"), defaultCharset());
    final CheckSuiteResponseList checkSuiteResponseList = Json.create().fromJson(fixture, CheckSuiteResponseList.class);
    assertThat(checkSuiteResponseList.checkSuites().get(0).id(), is(5L));
    assertThat(checkSuiteResponseList.checkSuites().get(0).app().get().slug().get(), is("octoapp"));
  }


  @Test
  public void testDeserializationWithLongId() throws IOException {
    // sample payload from https://docs.github.com/en/rest/checks/suites#list-check-suites-for-a-git-reference
    String fixture =
            Resources.toString(
                    getResource(this.getClass(), "check-suites-response-long-id.json"), defaultCharset());
    final CheckSuiteResponseList checkSuiteResponseList = Json.create().fromJson(fixture, CheckSuiteResponseList.class);
    assertThat(checkSuiteResponseList.checkSuites().get(0).id(), is(14707641936L));
    assertThat(checkSuiteResponseList.checkSuites().get(0).app().get().slug().get(), is("octoapp"));
  }

}