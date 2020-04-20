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

package com.spotify.github.v3.activity.events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.User;
import com.spotify.github.v3.git.Author;
import com.spotify.github.v3.repos.PushCommit;
import com.spotify.github.v3.repos.PushRepository;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Triggered when a repository branch is pushed to. In addition to branch pushes, webhook push
 * events are also triggered when repository tags are pushed.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutablePushEvent.class)
@JsonDeserialize(as = ImmutablePushEvent.class)
public interface PushEvent {

  /** The {@link PushRepository} */
  @Nullable
  PushRepository repository();

  /** The {@link User} that triggered/sent the event. */
  @Nullable
  User sender();

  /** The full Git ref that was pushed. Example: "refs/heads/master". */
  @Nullable
  String ref();

  /** The SHA of the most recent commit on ref before the push. */
  @Nullable
  String before();

  /** SHA of the repository state after the push. */
  @Nullable
  String after();

  /** True if given reference was created */
  @Nullable
  Boolean created();

  /** True if given reference was deleted */
  @Nullable
  Boolean deleted();

  /** True if given reference was force pushed */
  @Nullable
  Boolean forced();

  /** Base reference */
  Optional<String> baseRef();

  /**
   * Compare API URL This is a string because of malformed URIs sent from github. They send
   * unencoded '^' in the uri path.
   */
  @Nullable
  String compare();

  /**
   * An array of commit objects describing the pushed commits. (The array includes a maximum of 20
   * commits. If necessary, you can use the Commits API to fetch additional commits. This limit is
   * applied to timeline events only and isn't applied to webhook deliveries.)
   */
  @Nullable
  List<PushCommit> commits();

  /** The push commit object of the most recent commit on ref after the push. */
  @Nullable
  Optional<PushCommit> headCommit();

  /** Pusher */
  @Nullable
  Author pusher();
}
