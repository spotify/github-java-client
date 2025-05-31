/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2016 - 2025 Spotify AB
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Tests for GitHub with various PEM key formats
 */
public class GitHubClientPEMTest {

    private static final Pattern BEGIN_PATTERN = Pattern.compile("(-----)BEGIN RSA PRIVATE KEY(-----)");
    private static final Pattern END_PATTERN = Pattern.compile("(-----)END RSA PRIVATE KEY(-----)");

    @Test
    public void testJwtWithPoorlyFormattedPEM() throws Exception {
        // Load a PEM key resource
        byte[] originalKey = Resources.toByteArray(
                Resources.getResource("com/spotify/github/v3/fake-github-app-key.pem"));
        String pemContent = new String(originalKey, StandardCharsets.UTF_8);

        // Manipulate just the BEGIN/END delimiters without touching the key content
        String poorlyFormattedPem = pemContent;

        // Add spaces to BEGIN
        Matcher beginMatcher = BEGIN_PATTERN.matcher(poorlyFormattedPem);
        if (beginMatcher.find()) {
            poorlyFormattedPem = poorlyFormattedPem.replace(
                    beginMatcher.group(0),
                    beginMatcher.group(1) + " BEGIN   RSA   PRIVATE   KEY " + beginMatcher.group(2)
            );
        }

        // Add spaces to END
        Matcher endMatcher = END_PATTERN.matcher(poorlyFormattedPem);
        if (endMatcher.find()) {
            poorlyFormattedPem = poorlyFormattedPem.replace(
                    endMatcher.group(0),
                    endMatcher.group(1) + " END   RSA   PRIVATE   KEY " + endMatcher.group(2)
            );
        }

        // Test that we can parse this PEM
        JwtTokenIssuer tokenIssuer = JwtTokenIssuer.fromPrivateKey(
                poorlyFormattedPem.getBytes(StandardCharsets.UTF_8));
        String token = tokenIssuer.getToken(123456);
        assertNotNull(token);
    }

    @Test
    public void testJwtWithExtraDashesPEM() throws Exception {
        // Load a PEM key resource
        byte[] originalKey = Resources.toByteArray(
                Resources.getResource("com/spotify/github/v3/fake-github-app-key.pem"));
        String pemContent = new String(originalKey, StandardCharsets.UTF_8);

        // Manipulate just the BEGIN/END delimiters without touching the key content
        String extraDashesPem = pemContent;

        // Add extra dashes to BEGIN
        Matcher beginMatcher = BEGIN_PATTERN.matcher(extraDashesPem);
        if (beginMatcher.find()) {
            extraDashesPem = extraDashesPem.replace(
                    beginMatcher.group(0),
                    "-------BEGIN RSA PRIVATE KEY-------"
            );
        }

        // Add extra dashes to END
        Matcher endMatcher = END_PATTERN.matcher(extraDashesPem);
        if (endMatcher.find()) {
            extraDashesPem = extraDashesPem.replace(
                    endMatcher.group(0),
                    "-------END RSA PRIVATE KEY-------"
            );
        }

        // Test that we can parse this PEM
        JwtTokenIssuer tokenIssuer = JwtTokenIssuer.fromPrivateKey(
                extraDashesPem.getBytes(StandardCharsets.UTF_8));
        String token = tokenIssuer.getToken(123456);
        assertNotNull(token);
    }
}