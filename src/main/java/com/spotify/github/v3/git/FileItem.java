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
@JsonSerialize(as = ImmutableFileItem.class)
@JsonDeserialize(as = ImmutableFileItem.class)

public interface FileItem {

  /** Commit sha value. */
  String sha();

  /** Commit node_id. */
  String filename();

  /** Commit API URL. */
  @Nullable
  String status();

  @Nullable
  Integer additions();

  /** Author commit user. */
  @Nullable
  Integer deletions();

  @Nullable
  Integer changes();

  @Nullable
  @JsonProperty("blob_url")
  URI blobUrl();

  @Nullable
  @JsonProperty("raw_url")
  URI rawUrl();

  @Nullable
  @JsonProperty("contents_url")
  URI contentsUrl();

  @Nullable
  String patch();

}

