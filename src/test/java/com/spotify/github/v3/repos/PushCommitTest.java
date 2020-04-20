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
import org.junit.Before;
import org.junit.Test;

public class PushCommitTest {

  private String fixture;

  @Before
  public void setUp() throws Exception {
    fixture =
        Resources.toString(getResource(this.getClass(), "push_commit.json"), defaultCharset());
  }

  @Test
  public void testDeserialization() throws IOException {
    final PushCommit pushCommit = Json.create().fromJson(fixture, PushCommit.class);
    assertThat(pushCommit.modified().get(0), is("README.md"));
    assertThat(pushCommit.id(), is("0d1a26e67d8f5eaf1f6ba5c57fc3c7d91ac0fd1c"));
    assertThat(pushCommit.treeId(), is("f9d2a07e9488b91af2641b26b9407fe22a451433"));
    assertThat(pushCommit.message(), is("Update README.md"));
  }
}
