/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import jakarta.enterprise.context.Dependent;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;

@Dependent // important because it's tight to the identity store it's injected into
public class TomEEPbkdf2PasswordHash implements Pbkdf2PasswordHash {


    // These constants may be changed without breaking existing hashes.
    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    public static final int SALT_BYTE_SIZE = 24;
    public static final int HASH_BYTE_SIZE = 18;
    public static final int PBKDF2_ITERATIONS = 64000;

    private SecureRandom random = new SecureRandom();

    // These are configured by default to constants above, but can be overridden with parameters in initialize()
    private String configuredAlgorithm = PBKDF2_ALGORITHM;
    private int configuredIterations = PBKDF2_ITERATIONS;
    private int configuredSaltSize = SALT_BYTE_SIZE;
    private int configuredHashSize = HASH_BYTE_SIZE;

    @Override
    public void initialize(final Map<String, String> parameters) {
        // todo read from parameters and set fields
        final String algorithmParameter = parameters.get("Pbkdf2PasswordHash.Algorithm");
        if (algorithmParameter != null) { // todo also check withing a list of supported algorithm maybe
            configuredAlgorithm = algorithmParameter;
        }

        final String iterationsParameter = parameters.get("Pbkdf2PasswordHash.Iterations");
        if (iterationsParameter != null) {
            configuredIterations = Integer.parseInt(iterationsParameter);
        }

        final String saltSizeParameter = parameters.get("Pbkdf2PasswordHash.SaltSizeBytes");
        if (saltSizeParameter != null) {
            configuredSaltSize = Integer.parseInt(saltSizeParameter);
        }

        final String keySizeParameter = parameters.get("Pbkdf2PasswordHash.KeySizeBytes");
        if (keySizeParameter != null) {
            configuredHashSize = Integer.parseInt(keySizeParameter);
        }

    }

    @Override
    public String generate(final char[] password) {
        final byte[] salt = new byte[configuredSaltSize];
        random.nextBytes(salt);

        final byte[] hash = pbkdf2(password, salt, configuredIterations, configuredHashSize, configuredAlgorithm);
        return toString(configuredAlgorithm, configuredIterations, salt, hash);
    }

    @Override
    public boolean verify(final char[] password, final String hashedPassword) {

        // todo introduce a pojo that has all values and can serialize and deserialize from/to a String
        // todo introduce strongly typed exceptions

        final String[] params = hashedPassword.split(":");
        if (params.length != 4) {
            throw new RuntimeException("Missing fields in hashed password.");
        }

        final String algorithm = params[0];
        // todo check supported algorithms in a finite list?

        int iterations = 0;
        try {
            iterations = Integer.parseInt(params[1]);
        } catch (final NumberFormatException ex) {
            throw new RuntimeException("Could not parse the iteration as an integer.", ex);
        }

        if (iterations < 1) {
            throw new RuntimeException("Invalid number of iterations. Must be >= 1.");
        }

        byte[] salt = null;
        try {
            salt = fromBase64(params[2]);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Base64 decoding of salt failed.", ex );
        }

        byte[] expectedHash = null;
        try {
            expectedHash = fromBase64(params[3]);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Base64 decoding of pbkdf2 output failed.", ex);
        }

        final byte[] actual = pbkdf2(password, salt, iterations, expectedHash.length, algorithm);
        return slowEquals(expectedHash, actual);
    }

    private byte[] pbkdf2(final char[] password, final byte[] salt, final int iterations, final int length, final String algorithm) {
        try {
            final PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, length * 8);
            final SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
            return skf.generateSecret(spec).getEncoded();

        } catch (final NoSuchAlgorithmException ex) {
            throw new RuntimeException("Hash algorithm not supported.", ex);

        } catch (final InvalidKeySpecException ex) {
            throw new RuntimeException("Invalid key spec.", ex);
        }
    }

    // format: algorithm:iterations:salt:hash
    private static  String toString(final String algorithm, final int iterations, final byte[] salt, final byte[] hash) {
        return algorithm + ":" + iterations + ":" + toBase64(salt) + ":" + toBase64(hash);
    }

    private static byte[] fromBase64(String hex)
        throws IllegalArgumentException {
        return Base64.getDecoder().decode(hex);
    }

    private static String toBase64(byte[] array) {
        return Base64.getEncoder().encodeToString(array);
    }

    /**
     * Idea is to to compute the equals in a constant time. The password is correct if both matches.
     * We don't want to fail fast because we don't want to give any indication to a hacker to know when it failed
     *
     * @param expected password to compare too
     * @param actual the computed password to check
     *
     * @return true if they match
     */
    private static boolean slowEquals(byte[] expected, byte[] actual) {
        int diff = expected.length ^ actual.length;
        for (int i = 0; i < expected.length && i < actual.length; i++) {
            diff |= expected[i] ^ actual[i];
        }
        return diff == 0;
    }
}
