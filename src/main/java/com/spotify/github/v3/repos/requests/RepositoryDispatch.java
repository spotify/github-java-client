package com.spotify.github.v3.repos.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableRepositoryDispatch.class)
@JsonDeserialize(as = ImmutableRepositoryDispatch.class)
public interface RepositoryDispatch {

  /** The custom webhook event name */
  String eventType();

  /** JSON payload with extra information about the webhook event
  * that your action or workflow may use. */
  @Nullable
  Optional<JsonNode> clientPayload();

}
