package com.spotify.github.v3.git;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import com.spotify.github.v3.User;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;


@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableCommitResponse.class)
@JsonDeserialize(as = ImmutableCommitResponse.class)
public interface CommitResponse {

  /** Commit sha value. */
  String sha();

  /** Commit node_id. */
  @JsonProperty("node_id")
  String nodeId();

  /** Commit object. */
  Commit commit();

  /** Commit API URL. */
  @Nullable
  URI url();

  /** Commit HTML URL. */
  @Nullable
  @JsonProperty("html_url")
  URI htmlUrl();

  /** Commit comments url. */
  @Nullable
  @JsonProperty("comments_url")
  URI commentsUrl();

  /** Commit author object. */
  @Nullable
  User author();

  /** Commit committer object. */
  @Nullable
  User committer();

  /** Commit parents list. */
  @Nullable
  List<ParentItem> parents();

  /** Commit stat object. */
  @Nullable
  StatItem stats();

  /** Commit file list. */
  @Nullable
  List<FileItem> files();
}

