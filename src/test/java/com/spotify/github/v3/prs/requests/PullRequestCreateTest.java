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

package com.spotify.github.v3.prs.requests;

import com.spotify.github.jackson.Json;
import com.spotify.github.v3.prs.ImmutablePullRequest;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PullRequestCreateTest {

    @Test
    public void testSerializePullRequestCreateWithoutDraftParam() {

        final PullRequestCreate prCreate = ImmutablePullRequestCreate.builder()
                .base("some-base")
                .body("some-body")
                .head("some-head")
                .title("some-title")
                .build();

        final Function<String, String> addQuotes = word -> String.format("\"%s\"", word);
        final String json = String.format(
                "{%s:%s,%s:%s,%s:%s,%s:%s}",
                addQuotes.apply("title"),
                addQuotes.apply("some-title"),
                addQuotes.apply("body"),
                addQuotes.apply("some-body"),
                addQuotes.apply("head"),
                addQuotes.apply("some-head"),
                addQuotes.apply("base"),
                addQuotes.apply("some-base"));

        assertThat(Json.create().toJsonUnchecked(prCreate), is(equalTo(json)));
    }

    @Test
    public void testSerializePullRequestCreateWithDraftParam() {

        final PullRequestCreate prCreate = ImmutablePullRequestCreate.builder()
                .base("some-base")
                .body("some-body")
                .head("some-head")
                .title("some-title")
                .draft(true)
                .build();

        final Function<String, String> addQuotes = word -> String.format("\"%s\"", word);
        final String json = String.format(
                "{%s:%s,%s:%s,%s:%s,%s:%s,%s:%s}",
                addQuotes.apply("title"),
                addQuotes.apply("some-title"),
                addQuotes.apply("body"),
                addQuotes.apply("some-body"),
                addQuotes.apply("head"),
                addQuotes.apply("some-head"),
                addQuotes.apply("base"),
                addQuotes.apply("some-base"),
                addQuotes.apply("draft"),
                "true");

        assertThat(Json.create().toJsonUnchecked(prCreate), is(equalTo(json)));
    }
}
