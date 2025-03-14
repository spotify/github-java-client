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

package com.spotify.github;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Make sure we can represent a GitHub date, they are inconsistent at best. Sometimes the date is
 * represented as a Long, sometimes represented as a String following: {@link
 * java.time.format.DateTimeFormatter#ISO_INSTANT}
 */
public class GitHubInstant {

  private Optional<Long> epochSeconds;

  private Optional<Instant> instant;

  private GitHubInstant(final Optional<Long> epoch, final Optional<Instant> inst) {
    epochSeconds = epoch;
    instant = inst;
  }

  /**
   * Creates an instance of this class from a number.
   *
   * @param inst a Number
   * @return GitHubDateWrapper
   */
  public static GitHubInstant create(@Nonnull final Number inst) {
    final Number number = requireNonNull(inst);
    return new GitHubInstant(Optional.of(number.longValue()), Optional.empty());
  }

  /**
   * Creates an instance of this class from an instant.
   *
   * @param inst an Instant
   * @return GitHubDateWrapper
   */
  public static GitHubInstant create(@Nonnull final Instant inst) {
    final Instant instant = requireNonNull(inst);
    return new GitHubInstant(Optional.empty(), Optional.of(instant));
  }

  /**
   * Returns the Class type of the contained value.
   *
   * @return a Class.
   */
  public Class<?> type() {
    return epochSeconds.isPresent() ? epochSeconds.get().getClass() : instant.get().getClass();
  }

  /**
   * Returns the contained value as a unix epoch contained in a Long
   *
   * @return epoch in seconds
   */
  public Long epoch() {
    return epochSeconds.orElseGet(() -> instant.get().getEpochSecond());
  }

  /**
   * Rerturns the contained value as a unix epoch contained in an Instant
   *
   * @return an instant
   */
  public Instant instant() {
    return instant.orElseGet(() -> Instant.ofEpochSecond(epochSeconds.get()));
  }
}
