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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import org.immutables.value.Value;

/**
 * Comment parameters for a draft review.
 *
 * @see "https://developer.github.com/v3/pulls/reviews/#input"
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableReviewComment.class)
@JsonDeserialize(as = ImmutableReviewComment.class)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public abstract class ReviewComment {
    /**
     * Relative path to the file that necessitates a review comment.
     *
     * @return the path to the file.
     */
    public abstract String path();

    /**
     * Position in the diff where you want to add a review comment.
     *
     * @return the position in the diff.
     */
    public abstract int position();

    /**
     * Text of the review comment.
     *
     * @return the text of the review.
     */
    public abstract String body();
}
