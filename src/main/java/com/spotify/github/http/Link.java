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

package com.spotify.github.http;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.net.URI;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.immutables.value.Value;

/**
 * Used to express a typed relationship with another resource, where the relation type is defined by
 * RFC 5988.
 */
@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableLink.class)
@JsonDeserialize(as = ImmutableLink.class)
public interface Link {

  /**
   * Link value.
   *
   * @return url
   */
  URI url();

  /**
   * The relation type of a link is conveyed in the "rel" parameter's value. The "rel" parameter
   * MUST NOT appear more than once in a given link-value; occurrences after the first MUST be
   * ignored by parsers.
   *
   * @return relation type
   */
  Optional<String> rel();

  /**
   * The "rev" parameter has been used in the past to indicate that the semantics of the
   * relationship are in the reverse direction. That is, a link from A to B with REL="X" expresses
   * the same relationship as a link from B to A with REV="X". "rev" is deprecated by this
   * specification because it often confuses authors and readers; in most cases, using a separate
   * relation type is preferable.
   *
   * @return relation type
   */
  Optional<String> rev();

  /**
   * The "type" parameter, when present, is a hint indicating what the media type of the result of
   * dereferencing the link should be. Note that this is only a hint; for example, it does not
   * override the Content-Type header of a HTTP response obtained by actually following the link.
   * There MUST NOT be more than one type parameter in a link- value.
   *
   * @return type
   */
  Optional<String> type();

  /**
   * The "media" parameter, when present, is used to indicate intended destination medium or media
   * for style information (see [W3C.REC-html401-19991224], Section 6.13). Note that this may be
   * updated by [W3C.CR-css3-mediaqueries-20090915]). Its value MUST be quoted if it contains a
   * semicolon (";") or comma (","), and there MUST NOT be more than one "media" parameter in a
   * link-value.
   *
   * @return media
   */
  Optional<String> media();

  /**
   * The "title" parameter, when present, is used to label the destination of a link such that it
   * can be used as a human-readable identifier (e.g., a menu entry) in the language indicated by
   * the Content- Language header (if present). The "title" parameter MUST NOT appear more than once
   * in a given link-value; occurrences after the first MUST be ignored by parsers.
   *
   * @return title
   */
  Optional<String> title();

  /**
   * When present, the anchor parameter overrides this with another URI, such as a fragment of this
   * resource, or a third resource (i.e., when the anchor value is an absolute URI).
   *
   * @return anchor
   */
  Optional<String> anchor();

  /**
   * Construct a Link object from an array of link header strings.
   *
   * @param linkValues an array of link header strings
   * @return link object
   */
  static Link from(String[] linkValues) {
    final Map<String, String> linkResources =
        Arrays.stream(linkValues)
            .map(
                value -> {
                  final Matcher linkValueMatcher =
                      Pattern.compile("^<([^>]+)>$").matcher(value.trim());
                  final Matcher keyValueMatcher =
                      Pattern.compile("(?<name>\\w+)=\"(?<value>[^\"]+)\"").matcher(value.trim());

                  if (linkValueMatcher.find()) {
                    return new AbstractMap.SimpleEntry<>("url", linkValueMatcher.group(1));
                  } else if (keyValueMatcher.find()) {
                    return new AbstractMap.SimpleEntry<>(
                        keyValueMatcher.group("name"), keyValueMatcher.group("value"));
                  }
                  return (Map.Entry<String, String>) null;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    final ImmutableLink.Builder builder =
        ImmutableLink.builder()
            .url(URI.create(linkResources.get("url")))
            .rel(linkResources.get("rel"));
    if (linkResources.containsKey("rev")) {
      builder.rev(linkResources.get("rev"));
    }
    if (linkResources.containsKey("type")) {
      builder.type(linkResources.get("type"));
    }
    if (linkResources.containsKey("media")) {
      builder.media(linkResources.get("media"));
    }
    if (linkResources.containsKey("title")) {
      builder.title(linkResources.get("title"));
    }
    if (linkResources.containsKey("anchor")) {
      builder.anchor(linkResources.get("anchor"));
    }
    return builder.build();
  }
}
