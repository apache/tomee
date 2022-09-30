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
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.churchkey.Key;
import io.churchkey.Keys;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

/**
 * Utilities for generating a JWT for testing
 */
public class Tokens {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final int hashSize;
    private final String id;
    private final String prefix;

    public Tokens(final PrivateKey privateKey, final PublicKey publicKey, final int hashSize, final String id, final String prefix) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.hashSize = hashSize;
        this.id = id;
        this.prefix = prefix;

    }

    public static Tokens ec(final String curveName, int hashSize) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec spec = new ECGenParameterSpec(curveName);
            keyGen.initialize(spec);
            final KeyPair pair = keyGen.generateKeyPair();
            return new Tokens(pair.getPrivate(), pair.getPublic(), hashSize, null, "ES");
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e);
        }
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
            return new Tokens(pair.getPrivate(), pair.getPublic(), hashSize, id, "RS");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getPemPrivateKey() {
        return Keys.of(privateKey).toPem();
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

    public String asToken(final String claims) {
        try {
            final JWSHeader.Builder builder = new JWSHeader.Builder(new JWSAlgorithm(prefix + hashSize, Requirement.OPTIONAL))
                    .type(JOSEObjectType.JWT);

            if (id != null) {
                builder.keyID(id);
            }

            final JWSHeader header = builder.build();

            final JWTClaimsSet claimsSet = JWTClaimsSet.parse(claims);

            final SignedJWT jwt = new SignedJWT(header, claimsSet);

            if ("RS".equals(prefix)) {
                jwt.sign(new RSASSASigner(privateKey));
            } else if ("ES".equals(prefix)) {
                jwt.sign(new ECDSASigner((ECPrivateKey) privateKey));
            } else {
                throw new IllegalStateException("Unsupported prefix: " + prefix);
            }

            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Could not sign JWT", e);
        }
    }

    public static Tokens fromPrivateKey(final String contents) {
        final Key key;
        try {
            key = Keys.decode(contents);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (!key.getType().equals(Key.Type.PRIVATE)) {
            throw new IllegalArgumentException("Not a private key: " + key.getType());
        }

        final String prefix;
        switch (key.getAlgorithm()) {
            case EC: prefix = "EC";
                break;
            case RSA: prefix = "RS";
                break;
            default: throw new IllegalStateException("Unsupported Algorithm: " + key.getAlgorithm());
        }
        return new Tokens((PrivateKey) key.getKey(), (PublicKey) key.getPublicKey().getKey(), 256, null, prefix);
    }
}
