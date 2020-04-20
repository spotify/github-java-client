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

package com.spotify.github.v3.hooks.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.hooks.WebhookConfig;
import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/** Webhooks create request resource */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableWebhookCreate.class)
@JsonDeserialize(as = ImmutableWebhookCreate.class)
public interface WebhookCreate {

  /** Webhook name */
  @Nullable
  String name();

  /** Should it be active */
  @Nullable
  Boolean active();

  /** Events */
  @Nullable
  List<String> events();

  /** Webhook config. See {@link WebhookConfig} for more details. */
  @Nullable
  WebhookConfig config();
}
