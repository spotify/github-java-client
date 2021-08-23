package com.spotify.github.v3.git;

import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import org.immutables.value.Value;

@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableStatItem.class)
@JsonDeserialize(as = ImmutableStatItem.class)

public interface StatItem {

  @Nullable
  Integer total();

  @Nullable
  Integer additions();

  @Nullable
  Integer deletions();
}
