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

package com.spotify.github.v3.hooks;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.UpdateTracking;
import java.net.URI;
import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Webhook resource */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableWebhook.class)
@JsonDeserialize(as = ImmutableWebhook.class)
public interface Webhook extends UpdateTracking {

  /** ID */
  @Nullable
  Integer id();

  /** URL */
  @Nullable
  URI url();

  /** Test URL */
  @Nullable
  URI testUrl();

  /** Ping URL */
  @Nullable
  URI pingUrl();

  /** Name */
  @Nullable
  String name();

  /** Determines what events the hook is triggered for. Default: ["push"] */
  @Nullable
  List<String> events();

  /** Determines whether the hook is actually triggered on pushes. */
  @Nullable
  Boolean active();

  /** These settings vary between hooks and some are defined in the github-services repository. */
  @Nullable
  WebhookConfig config();
}
