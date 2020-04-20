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

package com.spotify.github.v3.repos;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Single repository commit status resource
 *
 * @see "https://developer.github.com/v3/repos/statuses/#get-the-combined-status-for-a-specific-ref"
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableCommitStatus.class)
@JsonDeserialize(as = ImmutableCommitStatus.class)
public interface CommitStatus {

  /**
   * The result of the status. Can be success, failure or pending.
   *
   * @return the string
   */
  @Nullable
  String state();

  /**
   * Statuses list.
   *
   * @return the list
   */
  @Nullable
  List<Status> statuses();

  /**
   * The commit SHA.
   *
   * @return the string
   */
  @Nullable
  String sha();
}
