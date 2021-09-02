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

package com.spotify.github.v3.helpers.wrappers;

import java.net.URI;

public class CommitWrapper extends Wrapper {
  private String treeSha;
  private URI treeUrl;

  public CommitWrapper(){}

  public String getTreeSha() {
    return treeSha;
  }

  public URI getTreeUrl() {
    return treeUrl;
  }

  public void setTreeSha(final String newTreeSha) {
    treeSha = newTreeSha;
  }

  public void setTreeUrl(final URI newTreeUrl) {
    treeUrl = newTreeUrl;
  }
}
