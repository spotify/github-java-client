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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.User;
import com.spotify.github.v3.Team;
import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Pull request review resource represents data returned by a single PR review get operation. It
 * contains all the fields from {@link Review} entity.
 *
 * @see https://docs.github.com/en/rest/reference/pulls#list-requested-reviewers-for-a-pull-request
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableReviewRequests.class)
@JsonDeserialize(as = ImmutableReviewRequests.class)
public interface ReviewRequests {

  @Nullable
  List<User> users();

  @Nullable
  List<Team> teams();
}
