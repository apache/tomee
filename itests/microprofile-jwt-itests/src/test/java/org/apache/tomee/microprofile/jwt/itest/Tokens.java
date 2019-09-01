/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.itest;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Requirement;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.*;

/**
 * Utilities for generating a JWT for testing
 */
public class Tokens {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final int hashSize;

    public Tokens(final PrivateKey privateKey, final PublicKey publicKey, final int hashSize) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.hashSize = hashSize;
    }

    public static Tokens rsa(int keyLength, int hashSize) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keyLength);
            final KeyPair pair = keyGen.generateKeyPair();
            return new Tokens(pair.getPrivate(), pair.getPublic(), hashSize);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String asToken(final String claims) throws Exception {
        try {
            final JWSHeader header = new JWSHeader.Builder(new JWSAlgorithm("RS"+hashSize, Requirement.OPTIONAL))
                    .type(JOSEObjectType.JWT)
                    .build();

            final JWTClaimsSet claimsSet = JWTClaimsSet.parse(claims);

            final SignedJWT jwt = new SignedJWT(header, claimsSet);

            jwt.sign(new RSASSASigner(privateKey));

            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Could not sign JWT");
        }
    }
}
