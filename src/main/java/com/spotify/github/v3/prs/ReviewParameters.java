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

package com.spotify.github.v3.prs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;

import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * The parameters for creating a review for a Pull Request.
 *
 * @see "https://developer.github.com/v3/pulls/reviews/#input"
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableReviewParameters.class)
@JsonDeserialize(as = ImmutableReviewParameters.class)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public abstract class ReviewParameters {
    /**
     * SHA of the commit that needs a review. If not the latest, the review may be outdated.
     * Defaults to the most recent commit in the PR when you do not specify a value.
     *
     * @return the optional commitId.
     */
    public abstract Optional<String> commitId();

    /**
     * **required** when using REQUEST_CHANGES or COMMENT for the event.
     *
     * @return the optional body for REQUEST_CHANGES or COMMENT events.
     */
    public abstract Optional<String> body();

    /**
     * Review action you want to perform. Should be one of: APPROVE, REQUEST_CHANGES or COMMENT.
     *
     * @return the review action to perform.
     */
    public abstract String event();

    /**
     * List of comments for a non-approve review.
     *
     * @return the list of comments for the review.
     */
    public abstract List<ReviewComment> comments();
}
