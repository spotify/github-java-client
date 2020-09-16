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

package com.spotify.github.v3.prs;

import com.google.common.io.Resources;
import com.spotify.github.jackson.Json;
import org.junit.Test;

import java.io.IOException;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ReviewParametersTest {
    @Test
    public void testDeserialization() throws IOException {
        final String fixture =
                Resources.toString(
                        getResource(this.getClass(), "create_review.json"),
                        defaultCharset());
        final ReviewParameters reviewParameters =
                Json.create().fromJson(fixture, ReviewParameters.class);
        assertThat(reviewParameters.event(), is("APPROVE"));
        assertThat(reviewParameters.commitId().get(), is("some_commit_id"));
        assertThat(reviewParameters.body().get(), is("some_approval_comment"));
        assertThat(reviewParameters.comments().size(), is(1));

        final ReviewComment reviewComment = reviewParameters.comments().get(0);
        assertThat(reviewComment.path(), is("some_file.txt"));
        assertThat(reviewComment.position(), is(2));
        assertThat(reviewComment.body(), is("some_comment_on_file.txt"));
    }
}
