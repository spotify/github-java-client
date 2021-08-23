package com.spotify.github.v3.git;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.net.URI;


@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableParentItem.class)
@JsonDeserialize(as = ImmutableParentItem.class)

public interface ParentItem {

  @Nullable
  String sha();

  @Nullable
  URI url();

  @Nullable
  @JsonProperty("html_url")
  URI htmlUrl();
}
