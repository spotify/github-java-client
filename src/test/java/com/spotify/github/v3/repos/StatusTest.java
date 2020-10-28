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

package com.spotify.github.v3.repos;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import org.junit.Test;

public class StatusTest {

  @Test
  public void testDeserialization() throws IOException {
    final String fixture =
        Resources.toString(getResource(this.getClass(), "status.json"), defaultCharset());

    final Status status = Json.create().fromJson(fixture, Status.class);

    assertThat(status.state(), is(StatusState.SUCCESS));
    assertThat(
        status.targetUrl(), is(Optional.of(URI.create("https://ci.example.com/1000/output"))));
    assertThat(status.description(), is(Optional.of("Build has completed successfully")));
    assertThat(status.context().get(), is("continuous-integration/jenkins"));
  }
}
