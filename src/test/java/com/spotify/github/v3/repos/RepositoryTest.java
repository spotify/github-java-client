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
import static com.spotify.github.v3.UserTest.assertUser;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class RepositoryTest {

  private String fixture;

  @Before
  public void setUp() throws Exception {
    fixture = Resources.toString(getResource(this.getClass(), "repository.json"), defaultCharset());
  }

  @Test
  public void testDeserialization() throws IOException {
    final Repository repository = Json.create().fromJson(fixture, Repository.class);
    assertThat(repository.id(), is(1296269));
    assertUser(repository.owner());
    assertThat(repository.name(), is("Hello-World"));
    assertThat(repository.fullName(), is(repository.owner().login() + "/Hello-World"));
    assertThat(repository.isPrivate(), is(false));
    assertThat(repository.isArchived(), is(false));
  }
}
