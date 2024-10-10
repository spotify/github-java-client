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

package com.spotify.github.v3.workflows;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.spotify.github.GithubStyle;
import org.immutables.value.Value;

import java.time.ZonedDateTime;

@Value.Immutable
@GithubStyle
@JsonDeserialize(as = ImmutableWorkflowsResponse.class)
public interface WorkflowsResponse {
    /**
     * The Workflow ID.
     *
     * @return the int
     */
    int id();

    /** Node ID */
    String nodeId();

    /** Name. */
    String name();

    /** The workflow path. */
    String path();

    /** Indicates the state of the workflow. */
    WorkflowsState state();

    /**
     * Created At
     *
     * @return The time when the workflow was created
     */
    ZonedDateTime createdAt();

    /**
     * Updated At
     *
     * @return The time when the workflow was updated
     */
    ZonedDateTime updatedAt();

    /**
     * Deleted At
     *
     * @return The time when the workflow was deleted
     */
    ZonedDateTime deletedAt();

    /**
     * Url string.
     *
     * @return the string
     */
    String url();

    /**
     * Html url string.
     *
     * @return the string
     */
    String htmlUrl();

    /**
     * Badge Url string.
     *
     * @return the string
     */
    String badgeUrl();
}
