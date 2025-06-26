/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2016 - 2020 Spotify AB
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

package com.spotify.github.v3.clients;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.spotify.github.async.AsyncPage;
import com.spotify.github.http.ImmutablePagination;
import com.spotify.github.http.Link;
import com.spotify.github.http.Pagination;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.client.utils.URIBuilder;

/**
 * Async page implementation for github resources
 *
 * @param <T> resource type
 */
public class GithubPage<T> implements AsyncPage<T> {

  static final int ITEM_PER_PAGE_DEFAULT = 30;
  private final GitHubClient github;
  private final String path;
  private final TypeReference<List<T>> typeReference;
  private final int itemsPerPage;

  protected static String formatPath(final String path, final int itemsPerPage) {
    try {
      URIBuilder uriBuilder = new URIBuilder(path);
      if (uriBuilder.getQueryParams().stream().anyMatch(p -> p.getName().equals("per_page"))) {
        return path;
      }
      uriBuilder.addParameter("per_page", Integer.toString(itemsPerPage));
      return uriBuilder.toString();
    } catch (Exception e) {
      return path;
    }
  }

  /**
   * Constructor.
   *
   * @param github github client
   * @param path resource page path
   * @param typeReference type reference for deserialization
   */
  GithubPage(
      final GitHubClient github, final String path, final TypeReference<List<T>> typeReference) {
    this.itemsPerPage = ITEM_PER_PAGE_DEFAULT;
    this.github = github;
    this.path = formatPath(path, ITEM_PER_PAGE_DEFAULT);
    this.typeReference = typeReference;
  }

  /**
   * Constructor.
   *
   * @param github github client
   * @param path resource page path
   * @param typeReference type reference for deserialization
   */
  GithubPage(
      final GitHubClient github,
      final String path,
      final TypeReference<List<T>> typeReference,
      final int itemsPerPage) {
    this.itemsPerPage = itemsPerPage;
    this.github = github;
    this.path = formatPath(path, itemsPerPage);
    this.typeReference = typeReference;
  }

  /** {@inheritDoc} */
  @Override
  public CompletableFuture<Pagination> pagination() {
    return linkMapAsync()
        .thenApply(
            linkMap -> {
              final Optional<Integer> maybePreviousPageNumber =
                  Optional.ofNullable(linkMap.get("prev"))
                      .map(prevLink -> pageNumberFromUri(prevLink.url().toString() + 1).orElse(1));

              final Optional<Integer> maybeNextPageNumber =
                  Optional.ofNullable(linkMap.get("next"))
                      .map(
                          prevLink ->
                              pageNumberFromUri(prevLink.url().toString())
                                  .orElseThrow(
                                      () ->
                                          new RuntimeException(
                                              "Could not parse page number from Link header with rel=\"next\"")));

              final Integer lastPageNumber =
                  maybePreviousPageNumber
                      .map(pageNumber -> pageNumber + 1)
                      .orElseGet(
                          () ->
                              Optional.ofNullable(linkMap.get("last"))
                                  .map(
                                      lastLink ->
                                          pageNumberFromUri(lastLink.url().toString())
                                              .orElseThrow(
                                                  () ->
                                                      new RuntimeException(
                                                          "Could not parse page number from Link "
                                                              + "header with rel=\"last\"")))
                                  .orElse(1));

              final Integer currentPageNumber =
                  maybeNextPageNumber.map(pageNumber -> pageNumber - 1).orElse(lastPageNumber);

              final ImmutablePagination.Builder builder =
                  ImmutablePagination.builder().current(currentPageNumber).last(lastPageNumber);

              maybePreviousPageNumber.ifPresent(builder::previous);
              maybeNextPageNumber.ifPresent(builder::next);

              return builder.build();
            });
  }

  /** {@inheritDoc} */
  @Override
  public CompletableFuture<AsyncPage<T>> nextPage() {
    return linkMapAsync()
        .thenApply(
            linkMap -> {
              final String nextPath =
                  Optional.ofNullable(linkMap.get("next"))
                      .map(nextLink -> nextLink.url().toString().replaceAll(github.urlFor(""), ""))
                      .orElseThrow(() -> new NoSuchElementException("Page iteration exhausted"));
              return new GithubPage<>(github, nextPath, typeReference, itemsPerPage);
            });
  }

  /** {@inheritDoc} */
  @Override
  public CompletableFuture<Boolean> hasNextPage() {
    return linkMapAsync().thenApply(linkMap -> nonNull(linkMap.get("next")));
  }

  /** {@inheritDoc} */
  @Override
  public AsyncPage<T> clone() {
    return new GithubPage<>(github, path, typeReference, itemsPerPage);
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<T> iterator() {
    return github
        .request(path)
        .thenApply(
            response ->
                github.json().fromJsonUncheckedNotNull(response.bodyString(), typeReference))
        .join()
        .iterator();
  }

  private CompletableFuture<Map<String, Link>> linkMapAsync() {
    return github
        .request(path)
        .thenApply(
            response ->
                Optional.ofNullable(response.header("Link")).stream()
                    .flatMap(linkHeader -> stream(linkHeader.split(",")))
                    .map(linkString -> Link.from(linkString.split(";")))
                    .filter(link -> link.rel().isPresent())
                    .collect(toMap(link -> link.rel().get(), identity())));
  }

  protected static Optional<Integer> pageNumberFromUri(final String uri) {
    Pattern pageInQueryPattern = Pattern.compile("(^|\\?|&)page=(?<page>\\d+)", Pattern.CASE_INSENSITIVE);
    try {
      String query = new URIBuilder(uri).build().getQuery();
      Matcher matcher = pageInQueryPattern.matcher(query);
      return matcher.find() ? Optional.of(Integer.parseInt(matcher.group("page"))) : Optional.empty();
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
