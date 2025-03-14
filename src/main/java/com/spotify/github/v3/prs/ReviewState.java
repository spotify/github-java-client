/*-
 * -\-\-
 * github-client
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

package com.spotify.github.v3.prs;

/**
 * Helpful constants for common Review states in reviews.
 *
 * @see "https://developer.github.com/v3/pulls/reviews/#list-reviews-on-a-pull-request"
 */
public class ReviewState {

  public static final String PENDING = "PENDING";
  public static final String COMMENTED = "COMMENTED";
  public static final String APPROVED = "APPROVED";
  public static final String REJECTED = "REJECTED";
  public static final String CHANGES_REQUESTED = "CHANGES_REQUESTED";
  public static final String DISMISSED = "DISMISSED";

  private ReviewState() {}
}
