package com.spotify.github.v3.git;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import org.immutables.value.Value;

import javax.annotation.Nullable;



@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableVerification.class)
@JsonDeserialize(as = ImmutableVerification.class)
public interface Verification {

  @Nullable
  Boolean verified();

  @Nullable
  String reason();

  @Nullable
  String signature();

  @Nullable
  String payload();

}

