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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

/** The helper Jwt token issuer. */
public class JwtTokenIssuer {

  private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.RS256;
  private static final long TOKEN_TTL = 600000;

  private final PrivateKey signingKey;

  private JwtTokenIssuer(final PrivateKey signingKey) {
    this.signingKey = signingKey;
  }

  /**
   * Instantiates a new Jwt token issuer.
   *
   * @param privateKey the private key to use
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws InvalidKeySpecException the invalid key spec exception
   */
  public static JwtTokenIssuer fromPrivateKey(final byte[] privateKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException {

    KeySpec keySpec = PKCS1PEMKey.loadKeySpec(privateKey)
        .orElseGet(() -> new PKCS8EncodedKeySpec(privateKey));

    KeyFactory kf = KeyFactory.getInstance("RSA");
    PrivateKey signingKey = kf.generatePrivate(keySpec);
    return new JwtTokenIssuer(signingKey);
  }

  /**
   * Generates a JWT token for the given APP ID.
   *
   * @param appId the app id
   * @return the token content
   */
  public String getToken(final Integer appId) {
    return Jwts.builder()
        .setId("github-auth")
        .setSubject("authenticating via private key")
        .setIssuer(String.valueOf(appId))
        .signWith(signingKey, SIGNATURE_ALGORITHM)
        .setExpiration(new Date(System.currentTimeMillis() + TOKEN_TTL))
        .setIssuedAt(new Date())
        .compact();
  }
}
