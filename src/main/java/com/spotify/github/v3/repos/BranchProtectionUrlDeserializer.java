/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2016 - 2021 Spotify AB
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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class BranchProtectionUrlDeserializer extends JsonDeserializer<Optional<URI>> {

  private URI fixInvalidGithubUrl(final String invalidUrl) {
    // There's a bug in github where it gives you back non-url-encoded characters
    // in the protection_url field. For example if your branch has a single "%" in its name.
    // As of this writing, the protection URL looks something like this
    // https://github-server.tld/api/v3/repos/owner/repo-name/branches/branch-name/protection
    final String[] schemaAndPath = invalidUrl.split("//");
    String[] pathParts = schemaAndPath[1].split("/");
    for (int i = 0; i < pathParts.length; i++) {
      pathParts[i] = URLEncoder.encode(pathParts[i], StandardCharsets.UTF_8);
    }
    String fixedUrlString = schemaAndPath[0] + "//" + String.join("/", pathParts);
    return URI.create(fixedUrlString);
  }

  @Override
  public Optional<URI> deserialize(
      final JsonParser jsonParser, final DeserializationContext deserializationContext)
      throws IOException {

    TypeReference<Optional<String>> ref = new TypeReference<>() {};
    Optional<String> protectionUrlStringOpt = jsonParser.readValueAs(ref);

    return protectionUrlStringOpt.map(
        protectionUrlString -> {
          try {
            return new URI(protectionUrlString);
          } catch (URISyntaxException e) {
            return fixInvalidGithubUrl(protectionUrlString);
          }
        });
  }
}
