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


package com.spotify.github.v3.clients.orgs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.User;
import com.spotify.github.v3.orgs.ImmutableMembership;
import java.net.URI;
import javax.annotation.Nullable;
import org.checkerframework.checker.units.qual.N;
import org.immutables.value.Value;

/**
 * Project resource represents data returned by a single Project get operation.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableProject.class)
@JsonDeserialize(as = ImmutableProject.class)
public interface Project {


  /** Owner URL */
  @Nullable
  URI ownerUrl();

  /** URL */
  @Nullable
  URI url();

  /** Creator */
  @Nullable
  User creator();
}
