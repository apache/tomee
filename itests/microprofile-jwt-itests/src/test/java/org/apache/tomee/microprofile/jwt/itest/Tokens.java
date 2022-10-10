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

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.Requirement;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSAEncrypter;
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
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
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
    private final String alg;

    public Tokens(final PrivateKey privateKey, final PublicKey publicKey, final int hashSize, final String id, final String alg) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.hashSize = hashSize;
        this.id = id;
        this.alg = alg;

    }

    public String getAlg() {
        return alg;
    }

    public static Tokens ec(final String curveName, int hashSize) {
        return ec(curveName, hashSize, "ES");
    }

    public static Tokens ec(final String curveName, final String alg) {
        return ec(curveName, -1, alg);
    }

    public static Tokens ec(final String curveName, int hashSize, final String alg) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec spec = new ECGenParameterSpec(curveName);
            keyGen.initialize(spec);
            final KeyPair pair = keyGen.generateKeyPair();
            return new Tokens(pair.getPrivate(), pair.getPublic(), hashSize, null, alg);
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
        return rsa(keyLength, hashSize, id, "RS");
    }

    public static Tokens rsa(int keyLength, final String alg) {
        return rsa(keyLength, -1, null, alg);
    }

    public static Tokens rsa(int keyLength, int hashSize, final String id, final String rs) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keyLength);
            final KeyPair pair = keyGen.generateKeyPair();
            return new Tokens(pair.getPrivate(), pair.getPublic(), hashSize, id, rs);
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

    public String getJwksPrivateKey() {
        return Keys.of(privateKey).toJwks();
    }

    public String getPemPublicKey() {
        return Keys.of(publicKey).toPem();
    }

    public String asToken(final String claims) {
        return sign(claims).serialize();
    }

    public SignedJWT sign(final String claims) {
        try {
            final JWSHeader.Builder builder = new JWSHeader.Builder(new JWSAlgorithm(alg + hashSize, Requirement.OPTIONAL))
                    .type(JOSEObjectType.JWT);

            if (id != null) {
                builder.keyID(id);
            }

            final JWSHeader header = builder.build();

            final JWTClaimsSet claimsSet = JWTClaimsSet.parse(claims);

            final SignedJWT jwt = new SignedJWT(header, claimsSet);

            if (alg.startsWith("RS")) {
                jwt.sign(new RSASSASigner(privateKey));
            } else if (alg.startsWith("ES")) {
                jwt.sign(new ECDSASigner((ECPrivateKey) privateKey));
            } else {
                throw new IllegalStateException("Unsupported prefix: " + alg);
            }

            return jwt;
        } catch (Exception e) {
            throw new RuntimeException("Could not sign JWT", e);
        }
    }

    public String asEncryptedToken(final String claims, final Tokens signer) {
        try {

            final SignedJWT signedJWT = signer.sign(claims);

            // Create JWE object with signed JWT as payload
            final JWEObject jweObject = new JWEObject(
                    new JWEHeader.Builder(new JWEAlgorithm(alg), EncryptionMethod.A256GCM)
                            .contentType("JWT") // required to indicate nested JWT
                            .build(),
                    new Payload(signedJWT));

            // Encrypt with the recipient's public key
            if (alg.startsWith("RS")) {
                jweObject.encrypt(new RSAEncrypter((RSAPublicKey) this.getPublicKey()));
            } else if (alg.startsWith("EC")) {
                jweObject.encrypt(new ECDHEncrypter((ECPublicKey) this.getPublicKey()));
            } else {
                throw new IllegalStateException("Unsupported prefix: " + alg);
            }

            // Serialise to JWE compact form
            return jweObject.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Could not encrypt JWT", e);
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
