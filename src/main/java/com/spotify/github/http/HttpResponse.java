/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2021 Spotify AB
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

package com.spotify.github.http;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface HttpResponse {
    // Returns the request that was sent to the server
    HttpRequest request();
    // Returns the HTTP status code
    int statusCode();
    // Returns the HTTP status message
    String statusMessage();
    // Returns the response body as an InputStream
    InputStream body();
    // Returns the response body as a String
    String bodyString();
    // Returns the response headers as a Map
    Map<String, List<String>> headers();
    // Returns the response headers for a specific header name as a list of Strings
    List<String> headers(String headerName);
    // Returns the response headers for a specific header name as a single String
    String header(String headerName);
    // Returns true if the response was successful
    boolean isSuccessful();
    // Closes the response
    void close();
}
