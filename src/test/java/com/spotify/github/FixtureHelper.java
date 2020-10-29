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

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;

import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;

public class FixtureHelper {

  private static final String FIXTURE_ROOT = "com/spotify/github/v3/";

  public static String loadFixture(final String path) {
    try {
      return Resources.toString(getResource(FIXTURE_ROOT + path), defaultCharset());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** Return a File pointing to the resource on the classpath */
  public static File loadFile(final String path) {
    URL resource = getResource(FIXTURE_ROOT + path);
    try {
      return new File(resource.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
