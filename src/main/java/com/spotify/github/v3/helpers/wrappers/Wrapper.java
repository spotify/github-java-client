package com.spotify.github.v3.helpers.wrappers;

public class Wrapper {

  protected String sha;

  public Wrapper(){}

  public String getSha() {
    return sha;
  }

  public void setSha(final String newSha) {
    sha = newSha;
  }
}
