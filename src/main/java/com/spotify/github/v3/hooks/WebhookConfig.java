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
import java.net.URI;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * Key/value pairs to provide settings for this hook. These settings vary between hooks and some are
 * defined in the github-services repository.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableWebhookConfig.class)
@JsonDeserialize(as = ImmutableWebhookConfig.class)
public interface WebhookConfig {

  /** A required string defining the URL to which the payloads will be delivered. */
  @Nullable
  URI url();

  /**
   * An optional string defining the media type used to serialize the payloads. Supported values
   * include json and form. The default is form.
   */
  @Nullable
  String contentType();
}
