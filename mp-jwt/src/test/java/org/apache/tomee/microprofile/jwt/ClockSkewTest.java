/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.microprofile.jwt;

import org.apache.tomee.microprofile.jwt.config.JWTAuthConfiguration;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.junit.Test;

import java.security.Key;
import java.util.Collections;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ClockSkewTest {

    @Test
    public void expiredTokenIsRejectedWithNegativeClockSkew() throws Exception {
        final RsaJsonWebKey key = RsaJwkGenerator.generateJwk(2048);
        final long now = NumericDate.now().getValue();
        final String expired = sign(key, now - 7200, now - 3600);

        assertRejected(expired, config(key, -1));
        assertRejected(expired, config(key, Integer.MIN_VALUE));
    }

    @Test
    public void expiredTokenIsRejectedWithNullClockSkew() throws Exception {
        final RsaJsonWebKey key = RsaJwkGenerator.generateJwk(2048);
        final long now = NumericDate.now().getValue();

        assertRejected(sign(key, now - 7200, now - 3600), config(key, null));
    }

    @Test
    public void validTokenIsAcceptedWithNegativeClockSkew() throws Exception {
        final RsaJsonWebKey key = RsaJwkGenerator.generateJwk(2048);
        final long now = NumericDate.now().getValue();

        assertEquals("alice", MPJWTFilter.ValidateJSonWebToken.parse(sign(key, now - 60, now + 3600), config(key, -1)).getName());
    }

    @Test
    public void positiveClockSkewIsHonoured() throws Exception {
        final RsaJsonWebKey key = RsaJwkGenerator.generateJwk(2048);
        final long now = NumericDate.now().getValue();
        final String recentlyExpired = sign(key, now - 600, now - 30);

        assertEquals("alice", MPJWTFilter.ValidateJSonWebToken.parse(recentlyExpired, config(key, 300)).getName());
        assertRejected(recentlyExpired, config(key, 0));
    }

    private static void assertRejected(final String token, final JWTAuthConfiguration config) {
        try {
            MPJWTFilter.ValidateJSonWebToken.parse(token, config);
            fail("expired token must be rejected");
        } catch (final ParseException expected) {
            // ok
        }
    }

    private static String sign(final RsaJsonWebKey key, final long issuedAt, final long expiresAt) throws Exception {
        final JwtClaims claims = new JwtClaims();
        claims.setSubject("alice");
        claims.setIssuer("https://server.example.com");
        claims.setIssuedAt(NumericDate.fromSeconds(issuedAt));
        claims.setExpirationTime(NumericDate.fromSeconds(expiresAt));

        final JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(key.getPrivateKey());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        return jws.getCompactSerialization();
    }

    private static JWTAuthConfiguration config(final RsaJsonWebKey key, final Integer clockSkew) {
        return new JWTAuthConfiguration(
                () -> Collections.<String, Key>singletonMap(JWTAuthConfiguration.DEFAULT_KEY, key.getPublicKey()),
                "https://server.example.com", false, new String[0],
                LinkedHashMap::new, "Authorization", null, null, null, null, clockSkew);
    }
}
