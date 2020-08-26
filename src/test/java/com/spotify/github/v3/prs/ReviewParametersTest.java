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
