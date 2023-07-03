package com.spotify.github.v3.orgs.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spotify.github.GithubStyle;
import java.util.Optional;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@GithubStyle
@JsonSerialize(as = ImmutableTeamCreate.class)
@JsonDeserialize(as = ImmutableTeamCreate.class)
public interface TeamCreate {

  /** The name of the team. */
  @Nullable
  String name();

  /** The description of the team. */
  Optional<String> description();

  /**
   * List GitHub IDs for organization members who will
   * become team maintainers.
   */
  Optional<String> maintainers();

  /** The full name (e.g., "organization-name/repository-name")
   * of repositories to add the team to.
   */
  Optional<String> repo_names();

  /** The ID of a team to set as the parent team. */
  Optional<String> parent_team_id();
}
