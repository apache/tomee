/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import static java.time.ZoneId.systemDefault;

public class JWTUtil {

    protected static final String MYSHAREDSECRET = "abcdefghijklmnopqrstuvwxyzabcdef"; // at least 256 bits

    private JWTUtil() {
        // prevent direct instantiation
    }

    public static String createValidJwtAccessToken(String... scopes) throws Exception {
        final SecretKey key = new SecretKeySpec(MYSHAREDSECRET.getBytes(), "hmac-sha256");

        // Prepare JWT with claims set
        final JWTClaimsSet.Builder claimsBuilder = createValidJwtClaimsSet();

        if (scopes != null && scopes.length > 0) {
            claimsBuilder.claim("scopes", scopes);
        }

        final JWTClaimsSet claimsSet = claimsBuilder.build();

        final JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        return sign(claimsSet, header, key).serialize();
    }

    public static SignedJWT sign(final JWTClaimsSet jwtClaimsSet, final JWSHeader jwsHeader, final Key key) throws JOSEException {
        JWSSigner signer = null;
        if (RSAPrivateKey.class.isInstance(key)) {
            signer = new RSASSASigner(RSAPrivateKey.class.cast(key));

        } else if (SecretKey.class.isInstance(key)) {
            signer = new MACSigner(SecretKey.class.cast(key).getEncoded());

        } else {
            throw new IllegalArgumentException(String.format("Class %s not supported", key.getClass().getName()));
        }

        SignedJWT signedJWT = new SignedJWT(
                jwsHeader,
                jwtClaimsSet);

        signedJWT.sign(signer);

        return signedJWT;
    }

    public static JWTClaimsSet.Builder createValidJwtClaimsSet() {
        final LocalDate now = LocalDate.now();
        return new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime() + 1800000))
                .issuer("https://apache.org")
                .jwtID(UUID.randomUUID().toString())
                .issueTime(toDate(now))
                .expirationTime(toDate(now.plusDays(30)))
                .notBeforeTime(toDate(now))
                .claim("role", "ruler of the known universe")
                .claim("token-type", "access-token");
    }

    public static Date toDate(final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(systemDefault()).toInstant());
    }
}