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

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * Base interface for parameter types. It's slightly hacky as it relies on reflection. It is however
 * nicer than having to copy and paste the same code in several places.
 *
 * <p>The interface provides one default method {@link #serialize()}.
 */
public interface Parameters {

  /**
   * Goes through all public methods defined in an interface that extends this interface and calls
   * them in the context of the class that called this method, then joins the method name with the
   * result it produced using an ampersand (&amp;) as a delimiter.
   *
   * <p>It works on interfaces with deep inheritance and filters out any methods defined on this
   * interface (with the assumption that they come from the same class loader).
   *
   * @return String of "key=value" joined on &amp;
   */
  default String serialize() {
    return Arrays.stream(this.getClass().getInterfaces())
        .filter(Parameters.class::isAssignableFrom)
        .map(Class::getMethods)
        .flatMap(Arrays::stream)
        // Filter out any method defined in this interface.
        .filter(method -> !method.getDeclaringClass().equals(Parameters.class))
        .collect(
            toMap(
                Method::getName,
                method -> {
                  try {
                    final Object invocationResult = method.invoke(this);
                    /* Wrap everything in an optional, this is safe as we know that auto matter will
                    enforce non nulls for the mandatory parameters. We use ofNullable as we don't
                    want the serialization to crash if a mandatory parameter returns null. All empty
                    optionals will get filtered away later.
                     */
                    return invocationResult instanceof Optional
                        ? (Optional) invocationResult
                        : Optional.ofNullable(invocationResult);
                  } catch (Exception e) {
                    return Optional.empty();
                  }
                }))
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().isPresent())
        // Make it stable.
        .sorted((entry1, entry2) -> entry1.getKey().compareTo(entry2.getKey()))
        .map(entry -> entry.getKey() + "=" + entry.getValue().get())
        .collect(joining("&"));
  }
}
