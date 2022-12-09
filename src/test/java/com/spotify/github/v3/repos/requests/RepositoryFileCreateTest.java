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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.git.ImmutableAuthor;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RepositoryFileCreateTest {

    @Test
    public void testSerializeFileCreateRequest() {

        final FileCreate request =
                ImmutableFileCreate.builder()
                        .message("my commit message")
                        .committer( ImmutableAuthor.builder().name("Monalisa Octocat").email("octocat@github.com").build())
                        .content("bXkgbmV3IGZpbGUgY29udGVudHM=")
                        .build();
        assertThat(
                Json.create().toJsonUnchecked(request),
                is(
                        equalTo(
                                "{\"message\":\"my commit message\",\"committer\":{\"name\":\"Monalisa Octocat\",\"email\":\"octocat@github.com\",\"username\":null,\"date\":null},\"content\":\"bXkgbmV3IGZpbGUgY29udGVudHM=\"}")));
    }
}
