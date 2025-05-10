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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.security.spec.KeySpec;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class PKCS1PEMKeyTest {

    @Test
    public void testStandardPEMFormat() {
        String key = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIEpAIBAAKCAQEA0Gjl7EWZZez7\n" +
                "VnrMsLr8P0SQJ1gPzuiTnrMsLrbn\n" +
                "-----END RSA PRIVATE KEY-----";

        Optional<KeySpec> result = PKCS1PEMKey.loadKeySpec(key.getBytes());
        assertTrue(result.isPresent());
    }

    @Test
    public void testPEMWithExtraWhitespace() {
        String key = "  -----BEGIN   RSA  PRIVATE   KEY-----  \n" +
                "MIIEpAIBAAKCAQEA0Gjl7EWZZez7\n   " +
                "    VnrMsLr8P0SQJ1gPzuiTnrMsLrbn    \n" +
                "  -----END   RSA   PRIVATE   KEY-----  ";

        Optional<KeySpec> result = PKCS1PEMKey.loadKeySpec(key.getBytes());
        assertTrue(result.isPresent());
    }

    @Test
    public void testPEMWithManyDashes() {
        String key = "--------BEGIN RSA PRIVATE KEY--------\n" +
                "MIIEpAIBAAKCAQEA0Gjl7EWZZez7\n" +
                "VnrMsLr8P0SQJ1gPzuiTnrMsLrbn\n" +
                "--------END RSA PRIVATE KEY--------";

        Optional<KeySpec> result = PKCS1PEMKey.loadKeySpec(key.getBytes());
        assertTrue(result.isPresent());
    }

    @Test
    public void testPKCS8Format() {
        String key = "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEpAIBAAKCAQEA0Gjl7EWZZez7\n" +
                "VnrMsLr8P0SQJ1gPzuiTnrMsLrbn\n" +
                "-----END PRIVATE KEY-----";

        Optional<KeySpec> result = PKCS1PEMKey.loadKeySpec(key.getBytes());
        assertTrue(result.isPresent());
    }

    @Test
    public void testMultilinePEMFormat() {
        String key = "-----BEGIN RSA PRIVATE KEY-----\r\n" +
                "MIIEpAIBAAKCAQEA0Gjl7EWZZez7\r\n" +
                "VnrMsLr8P0SQJ1gPzuiTnrMsLrbn\r\n" +
                "-----END RSA PRIVATE KEY-----";

        Optional<KeySpec> result = PKCS1PEMKey.loadKeySpec(key.getBytes());
        assertTrue(result.isPresent());
    }

    @Test
    public void testInvalidBase64Content() {
        String key = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "This is not valid base64 content!\n" +
                "-----END RSA PRIVATE KEY-----";

        Optional<KeySpec> result = PKCS1PEMKey.loadKeySpec(key.getBytes());
        assertFalse(result.isPresent());
    }

    @Test
    public void testEmptyContent() {
        String key = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "\n" +
                "-----END RSA PRIVATE KEY-----";

        Optional<KeySpec> result = PKCS1PEMKey.loadKeySpec(key.getBytes());
        assertFalse(result.isPresent());
    }

    @Test
    public void testNonPEMFormat() {
        String key = "This is not a PEM file at all.";

        Optional<KeySpec> result = PKCS1PEMKey.loadKeySpec(key.getBytes());
        assertFalse(result.isPresent());
    }
}