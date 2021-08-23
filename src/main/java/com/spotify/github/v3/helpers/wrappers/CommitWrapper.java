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
