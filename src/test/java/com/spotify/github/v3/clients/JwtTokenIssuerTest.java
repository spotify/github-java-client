/*-
 * -\-\-
 * github-api
 * --
 * Copyright (C) 2020 Spotify AB
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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.io.Resources;
import java.net.URL;
import org.junit.Test;

public class JwtTokenIssuerTest {

  private static final URL DER_KEY_RESOURCE =
      Resources.getResource("com/spotify/github/v3/github-private-key");

  // generated using this command: "openssl genrsa -out fake-github-app-key.pem 2048"
  private static final URL PEM_KEY_RESOURCE =
      Resources.getResource("com/spotify/github/v3/fake-github-app-key.pem");

  @Test
  public void loadsDERFileWithPKCS8Key() throws Exception {
    final byte[] key = Resources.toByteArray(DER_KEY_RESOURCE);
    final JwtTokenIssuer tokenIssuer = JwtTokenIssuer.fromPrivateKey(key);

    final String token = tokenIssuer.getToken(42);
    assertThat(token, not(nullValue()));
  }

  @Test
  public void loadsPEMFile() throws Exception {
    final byte[] key = Resources.toByteArray(PEM_KEY_RESOURCE);
    final JwtTokenIssuer tokenIssuer = JwtTokenIssuer.fromPrivateKey(key);

    final String token = tokenIssuer.getToken(42);
    assertThat(token, not(nullValue()));
  }

}
