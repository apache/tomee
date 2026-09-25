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
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.junit.Test;

import java.security.Key;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MultipleKeysWithoutKidTest {

    @Test
    public void tokenWithoutKidIsVerifiedAgainstAllKeys() throws Exception {
        final RsaJsonWebKey first = RsaJwkGenerator.generateJwk(2048);
        final RsaJsonWebKey second = RsaJwkGenerator.generateJwk(2048);

        final JsonWebToken token = MPJWTFilter.ValidateJSonWebToken.parse(sign(second), config(first, second));

        assertEquals("alice", token.getName());
    }

    @Test
    public void tokenSignedWithUnknownKeyIsRejected() throws Exception {
        final RsaJsonWebKey first = RsaJwkGenerator.generateJwk(2048);
        final RsaJsonWebKey second = RsaJwkGenerator.generateJwk(2048);
        final RsaJsonWebKey unknown = RsaJwkGenerator.generateJwk(2048);

        try {
            MPJWTFilter.ValidateJSonWebToken.parse(sign(unknown), config(first, second));
            fail("token signed with an unknown key must not be accepted");
        } catch (final ParseException expected) {
            // ok
        }
    }

    private static String sign(final RsaJsonWebKey key) throws Exception {
        final JwtClaims claims = new JwtClaims();
        claims.setSubject("alice");
        claims.setIssuer("https://server.example.com");
        claims.setExpirationTimeMinutesInTheFuture(5);
        claims.setIssuedAtToNow();

        final JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(key.getPrivateKey());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        // no kid header on purpose
        return jws.getCompactSerialization();
    }

    private static JWTAuthConfiguration config(final RsaJsonWebKey... keys) {
        final Map<String, Key> publicKeys = new LinkedHashMap<>();
        for (int i = 0; i < keys.length; i++) {
            publicKeys.put("key" + i, keys[i].getPublicKey());
        }
        return new JWTAuthConfiguration(() -> publicKeys, "https://server.example.com", false, new String[0],
                LinkedHashMap::new, "Authorization", null, null, null, null, 0);
    }
}
