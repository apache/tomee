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
import io.churchkey.Keys;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Utilities for generating a JWT for testing
 */
public class Tokens {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final int hashSize;
    private final String id;

    public Tokens(final PrivateKey privateKey, final PublicKey publicKey, final int hashSize) {
        this(privateKey, publicKey, hashSize, null);
    }

    public Tokens(final PrivateKey privateKey, final PublicKey publicKey, final int hashSize, final String id) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.hashSize = hashSize;
        this.id = id;
    }

    public int getHashSize() {
        return hashSize;
    }

    public String getId() {
        return id;
    }

    public static Tokens rsa(int keyLength, int hashSize) {
        return rsa(keyLength, hashSize, null);
    }

    public static Tokens rsa(int keyLength, int hashSize, final String id) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keyLength);
            final KeyPair pair = keyGen.generateKeyPair();
            return new Tokens(pair.getPrivate(), pair.getPublic(), hashSize, id);
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

    public String getEncodedPublicKey() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public String getJwkPublicKey() {
        return Keys.of(publicKey).toJwk();
    }

    public String getJwksPublicKey() {
        return Keys.of(publicKey).toJwks();
    }

    public String getPemPublicKey() {
        return Keys.of(publicKey).toPem();
    }

    public String asToken(final String claims) throws Exception {
        try {
            final JWSHeader.Builder builder = new JWSHeader.Builder(new JWSAlgorithm("RS" + hashSize, Requirement.OPTIONAL))
                    .type(JOSEObjectType.JWT);

            if (id != null) {
                builder.keyID(id);
            }
            
            final JWSHeader header = builder.build();

            final JWTClaimsSet claimsSet = JWTClaimsSet.parse(claims);

            final SignedJWT jwt = new SignedJWT(header, claimsSet);

            jwt.sign(new RSASSASigner(privateKey));

            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Could not sign JWT");
        }
    }
}
