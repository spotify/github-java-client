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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.git.Author;
import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableFileCreate.class)
@JsonDeserialize(as = ImmutableFileCreate.class)
public interface FileCreate {

    /** The commit message. */
    String message();

    /** Committer commit user. */
    @Nullable
    Author committer();

    /** The author of the file. Default: The committer or the authenticated user if you omit.  */
    @Nullable
    Author author();

    /** The branch name. Default: the repository’s default branch (usually master) */
    @Nullable
    String branch();

    /** The new file content, using Base64 encoding.. */
    String content();
}
