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
import java.util.function.Supplier;

/** The helper Jwt token issuer. */
public class JwtTokenIssuer {

  private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.RS256;
  private static final long TOKEN_TTL = 600_000L;
  private static final long TOKEN_ISSUED = 60_000L;

  private final PrivateKey signingKey;
  private final Supplier<Date> issuedAt;

  private JwtTokenIssuer(final PrivateKey signingKey, final Supplier<Date> issuedAt) {
    this.signingKey = signingKey;
    this.issuedAt = issuedAt;
  }

  /**
   * Instantiates a new Jwt token issuer.
   *
   * @param privateKey the private key to use
   * @param issuedAt the way to determine when the jwt is assumed to be issued at. Used to fix drift
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws InvalidKeySpecException the invalid key spec exception
   */
  public static JwtTokenIssuer fromPrivateKey(final byte[] privateKey, final Supplier<Date> issuedAt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {

    KeySpec keySpec = PKCS1PEMKey.loadKeySpec(privateKey)
        .orElseGet(() -> new PKCS8EncodedKeySpec(privateKey));

    KeyFactory kf = KeyFactory.getInstance("RSA");
    PrivateKey signingKey = kf.generatePrivate(keySpec);
    return new JwtTokenIssuer(signingKey, issuedAt);
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

    Supplier<Date> defaultIssuedAt = () -> new Date(System.currentTimeMillis() - TOKEN_ISSUED);
    return fromPrivateKey(privateKey, defaultIssuedAt);
  }
  /**
   * Generates a JWT token for the given APP ID.
   *
   * @param appId the app id
   * @return the token content
   */
  public String getToken(final Integer appId) {
    
    Date now = issuedAt.get();
    return Jwts.builder()
        .setId("github-auth")
        .setSubject("authenticating via private key")
        .setIssuer(String.valueOf(appId))
        .signWith(signingKey, SIGNATURE_ALGORITHM)
        .setExpiration(new Date(now.getTime() + TOKEN_TTL))
        .setIssuedAt(now)
        .compact();
  }
}
