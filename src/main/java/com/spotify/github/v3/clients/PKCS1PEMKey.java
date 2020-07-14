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

import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads PEM key files as issued by the Github apps page.
 */
final class PKCS1PEMKey {

  private static final Pattern PKCS1_PEM_KEY_PATTERN =
      Pattern.compile("(?m)(?s)^---*BEGIN RSA PRIVATE KEY.*---*$(.*)^---*END.*---*$.*");

  private PKCS1PEMKey() {}

  /**
   * Try to interpret the supplied key as a PKCS#1 PEM file.
   *
   * @param privateKey the private key to use
   */
  public static Optional<KeySpec> loadKeySpec(final byte[] privateKey) {
    final Matcher isPEM = PKCS1_PEM_KEY_PATTERN.matcher(new String(privateKey));
    if (!isPEM.matches()) {
      return Optional.empty();
    }

    byte[] pkcs1Key = Base64.getMimeDecoder().decode(isPEM.group(1));
    byte[] pkcs8Key = toPkcs8(pkcs1Key);
    final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8Key);
    return Optional.of(keySpec);
  }

  /**
   * Convert a PKCS#1 key to a PKCS#8 key.
   *
   * <p>The Github app key comes in PKCS#1 format, while the Java security utilities only natively
   * understand PKCS#8. Fortunately, we can convert between the two by adding the PKCS#8 headers
   * manually.
   *
   * <p>Adapted from code in https://github.com/Mastercard/client-encryption-java
   */
  @SuppressWarnings("checkstyle:magicnumber")
  private static byte[] toPkcs8(final byte[] pkcs1Bytes) {
    final int pkcs1Length = pkcs1Bytes.length;
    final int totalLength = pkcs1Length + 22;
    byte[] pkcs8Header = new byte[] {
        0x30, (byte) 0x82, (byte) ((totalLength >> 8) & 0xff), (byte) (totalLength & 0xff), // Sequence + total length
        0x2, 0x1, 0x0, // Integer (0)
        0x30, 0xD, 0x6, 0x9, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0xD, 0x1, 0x1, 0x1, 0x5, 0x0, // Sequence: 1.2.840.113549.1.1.1, NULL
        0x4, (byte) 0x82, (byte) ((pkcs1Length >> 8) & 0xff), (byte) (pkcs1Length & 0xff) // Octet string + length
    };

    byte[] pkcs8bytes = new byte[pkcs8Header.length + pkcs1Bytes.length];
    System.arraycopy(pkcs8Header, 0, pkcs8bytes, 0, pkcs8Header.length);
    System.arraycopy(pkcs1Bytes, 0, pkcs8bytes, pkcs8Header.length, pkcs1Bytes.length);
    return pkcs8bytes;
  }
}
