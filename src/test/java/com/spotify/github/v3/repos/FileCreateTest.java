/*-
 * -\-\-
 * github-client
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

import com.google.common.io.Resources;
import com.spotify.github.GitHubInstant;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.git.ImmutableShaLink;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;


public class FileCreateTest {

    private String fixture;

    @Before
    public void setUp() throws Exception {
        fixture =
                Resources.toString(getResource(this.getClass(), "file_create.json"), defaultCharset());
    }

    @Test
    public void testDeserialization() throws IOException {
        final FileCommit fileCommit = Json.create().fromJson(fixture, FileCommit.class);
        assertThat(fileCommit.content(), not(nullValue()));
        assertThat(fileCommit.content().name(), is("hello.txt"));
        assertThat(fileCommit.content().path(), is("notes/hello.txt"));
        assertThat(fileCommit.content().sha(), is("a56507ed892d05a37c6d6128c260937ea4d287bd"));
        assertThat(fileCommit.content().size(), is(9));
        assertThat(fileCommit.content().url(), is(URI.create("https://api.github.com/repos/octocat/Hello-World/contents/notes/hello.txt")));
        assertThat(fileCommit.content().htmlUrl(), is(URI.create("https://github.com/octocat/Hello-World/blob/master/notes/hello.txt")));
        assertThat(fileCommit.content().gitUrl(), is(URI.create("https://api.github.com/repos/octocat/Hello-World/git/blobs/a56507ed892d05a37c6d6128c260937ea4d287bd")));
        assertThat(fileCommit.content().downloadUrl(), is(URI.create("https://raw.githubusercontent.com/octocat/HelloWorld/master/notes/hello.txt")));
        assertThat(fileCommit.content().type(), is("file"));
        assertThat(fileCommit.commit(), not(nullValue()));

        assertThat(fileCommit.commit().sha().isPresent(), is(true));
        assertThat(fileCommit.commit().sha().get(), is("18a43cd8e1e3a79c786e3d808a73d23b6d212b16"));
        assertThat(fileCommit.commit().url(), is(URI.create("https://api.github.com/repos/octocat/Hello-World/git/commits/18a43cd8e1e3a79c786e3d808a73d23b6d212b16")));
        assertThat(fileCommit.commit().author(), not(nullValue()));
        assertThat(fileCommit.commit().author().date().isPresent(), is(true));
        assertThat(fileCommit.commit().author().date().get().instant(), is(GitHubInstant.create(Instant.parse("2014-11-07T22:01:45Z")).instant()));
        assertThat(fileCommit.commit().author().name(), is("Monalisa Octocat"));
        assertThat(fileCommit.commit().author().email().isPresent(), is(true));
        assertThat(fileCommit.commit().author().email().get(), is("octocat@github.com"));
        assertThat(fileCommit.commit().message(), is("my commit message"));
        assertThat(fileCommit.commit().tree(), is(ImmutableShaLink.builder().url(URI.create("https://api.github.com/repos/octocat/Hello-World/git/trees/9a21f8e2018f42ffcf369b24d2cd20bc25c9e66f")).sha("9a21f8e2018f42ffcf369b24d2cd20bc25c9e66f").build()));
    }
}
