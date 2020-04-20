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

package com.spotify.github.v3.checks;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.spotify.github.GithubStyle;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * The Annotation object.
 *
 * <p>Adds information from your analysis to specific lines of code. Annotations are visible on
 * GitHub in the Checks and Files changed tab of the pull request. The Checks API limits the number
 * of annotations to a maximum of 50 per API request. To create more than 50 annotations, you have
 * to make multiple requests to the Update a check run endpoint. Each time you update the check run,
 * annotations are appended to the list of annotations that already exist for the check run. For
 * details about how you can view annotations on GitHub, see "About status checks". See the
 * annotations object description for details about how to use this parameter.
 *
 * @see "https://developer.github.com/v3/checks/runs/#annotations-object"
 */
@Value.Immutable
@GithubStyle
@JsonDeserialize(as = ImmutableAnnotation.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface Annotation {

  /**
   * The path of the file to add an annotation to. For example, assets/css/main.css.
   *
   * @return the string
   */
  String path();

  /**
   * Blob href optional.
   *
   * @return the optional
   */
  Optional<String> blobHref();

  /**
   * Annotation level. Can be one of notice, warning, or failure.
   *
   * @return the annotation level
   */
  AnnotationLevel annotationLevel();

  /**
   * A short description of the feedback for these lines of code. The maximum size is 64 KB.
   *
   * @return the string
   */
  String message();

  /**
   * The title that represents the annotation. The maximum size is 255 characters.
   *
   * @return the optional
   */
  Optional<String> title();

  /**
   * Details about this annotation. The maximum size is 64 KB.
   *
   * @return the optional string
   */
  Optional<String> rawDetails();

  /**
   * The start line of the annotation.
   *
   * @return the int
   */
  int startLine();

  /**
   * The end line of the annotation.
   *
   * @return the int
   */
  int endLine();

  /**
   * Start column optional.
   *
   * @return the optional
   */
  Optional<Integer> startColumn();

  /**
   * End column optional.
   *
   * @return the optional
   */
  Optional<Integer> endColumn();
}
