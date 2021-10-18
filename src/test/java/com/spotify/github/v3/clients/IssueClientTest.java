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

import static com.google.common.io.Resources.getResource;
import static com.spotify.github.FixtureHelper.loadFixture;
import static com.spotify.github.v3.clients.IssueClient.COMMENTS_URI_NUMBER_TEMPLATE;
import static com.spotify.github.v3.clients.MockHelper.createMockResponse;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.spotify.github.async.AsyncPage;
import com.spotify.github.jackson.Json;
import com.spotify.github.v3.comment.Comment;
import java.io.IOException;
import java.util.List;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Headers.class, ResponseBody.class, Response.class})
public class IssueClientTest {

  private GitHubClient github;
  private IssueClient issueClient;

  @Before
  public void setUp() {
    github = mock(GitHubClient.class);
    when(github.json()).thenReturn(Json.create());
    when(github.urlFor("")).thenReturn("https://github.com/api/v3");
    issueClient = new IssueClient(github, "someowner", "somerepo");
  }

  @Test
  public void testCommentPaginationSpliterator() throws IOException {
    final String firstPageLink =
        "<https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments?page=2>; rel=\"next\", <https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments?page=2>; rel=\"last\"";
    final String firstPageBody =
        Resources.toString(getResource(this.getClass(), "comments_page1.json"), defaultCharset());
    final Response firstPageResponse = createMockResponse(firstPageLink, firstPageBody);

    final String lastPageLink =
        "<https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments>; rel=\"first\", <https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments>; rel=\"prev\"";
    final String lastPageBody =
        Resources.toString(getResource(this.getClass(), "comments_page2.json"), defaultCharset());
    final Response lastPageResponse = createMockResponse(lastPageLink, lastPageBody);

    when(github.request(format(COMMENTS_URI_NUMBER_TEMPLATE, "someowner", "somerepo", "123")))
        .thenReturn(completedFuture(firstPageResponse));
    when(github.request(
            format(COMMENTS_URI_NUMBER_TEMPLATE + "?page=2", "someowner", "somerepo", "123")))
        .thenReturn(completedFuture(lastPageResponse));

    final Iterable<AsyncPage<Comment>> pageIterator = () -> issueClient.listComments(123);
    final List<Comment> listComments =
        stream(pageIterator.spliterator(), false)
            .flatMap(page -> stream(page.spliterator(), false))
            .collect(toList());

    assertThat(listComments.size(), is(30));
    assertThat(listComments.get(0).id(), is(1345268));
    assertThat(listComments.get(listComments.size() - 1).id(), is(1356168));
  }

  @Test
  public void testCommentPaginationForeach() throws IOException {
    final String firstPageLink =
        "<https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments?page=2>; rel=\"next\", <https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments?page=2>; rel=\"last\"";
    final String firstPageBody =
        Resources.toString(getResource(this.getClass(), "comments_page1.json"), defaultCharset());
    final Response firstPageResponse = createMockResponse(firstPageLink, firstPageBody);

    final String lastPageLink =
        "<https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments>; rel=\"first\", <https://github.com/api/v3/repos/someowner/somerepo/issues/123/comments>; rel=\"prev\"";
    final String lastPageBody =
        Resources.toString(getResource(this.getClass(), "comments_page2.json"), defaultCharset());
    final Response lastPageResponse = createMockResponse(lastPageLink, lastPageBody);

    when(github.request(format(COMMENTS_URI_NUMBER_TEMPLATE, "someowner", "somerepo", "123")))
        .thenReturn(completedFuture(firstPageResponse));
    when(github.request(
            format(COMMENTS_URI_NUMBER_TEMPLATE + "?page=2", "someowner", "somerepo", "123")))
        .thenReturn(completedFuture(lastPageResponse));

    final List<Comment> listComments = Lists.newArrayList();
    issueClient
        .listComments(123)
        .forEachRemaining(
            page -> {
              page.iterator().forEachRemaining(listComments::add);
            });

    assertThat(listComments.size(), is(30));
    assertThat(listComments.get(0).id(), is(1345268));
    assertThat(listComments.get(listComments.size() - 1).id(), is(1356168));
  }

  @Test
  public void testCommentCreated() throws IOException {
    final String fixture = loadFixture("clients/comment_created.json");
    final Response response = createMockResponse("", fixture);
    final String path = format(COMMENTS_URI_NUMBER_TEMPLATE, "someowner", "somerepo", 10);
    when(github.post(anyString(), anyString(), eq(Comment.class))).thenCallRealMethod();
    when(github.post(eq(path), anyString())).thenReturn(completedFuture(response));
    final Comment comment = issueClient.createComment(10, "Me too").join();

    assertThat(comment.id(), is(114));
  }
}
