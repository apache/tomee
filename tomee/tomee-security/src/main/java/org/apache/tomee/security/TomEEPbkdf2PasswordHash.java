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

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import jakarta.enterprise.context.Dependent;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Dependent // important because it's tight to the identity store it's injected into
public class TomEEPbkdf2PasswordHash implements Pbkdf2PasswordHash {


    // These constants may be changed without breaking existing hashes.
    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    public static final int SALT_BYTE_SIZE = 24;
    public static final int HASH_BYTE_SIZE = 18;
    public static final int PBKDF2_ITERATIONS = 64000;

    // algorithms allowed by the spec, plus SHA1 which is the historical default of this class
    private static final Set<String> SUPPORTED_ALGORITHMS = Set.of(
            "PBKDF2WithHmacSHA1",
            "PBKDF2WithHmacSHA224",
            "PBKDF2WithHmacSHA256",
            "PBKDF2WithHmacSHA384",
            "PBKDF2WithHmacSHA512");

    private static final int MIN_ITERATIONS = 1024;

    // opt-out to accept hashes (and configuration) with weak parameters, e.g. legacy hashes
    public static final String ALLOW_WEAK_PARAMETERS = "tomee.security.pbkdf2.allow-weak-parameters";

    private static final Logger LOGGER = Logger.getInstance(LogCategory.TOMEE_SECURITY, TomEEPbkdf2PasswordHash.class);

    // warn once per JVM when rejecting, once per distinct algorithm/iterations combination when accepting
    private static final int MAX_WARNED_PARAMETERS = 64;
    private static final AtomicBoolean WARNED_REJECTED = new AtomicBoolean();
    private static final Set<String> WARNED_PARAMETERS = ConcurrentHashMap.newKeySet();

    private SecureRandom random = new SecureRandom();

    // These are configured by default to constants above, but can be overridden with parameters in initialize()
    private String configuredAlgorithm = PBKDF2_ALGORITHM;
    private int configuredIterations = PBKDF2_ITERATIONS;
    private int configuredSaltSize = SALT_BYTE_SIZE;
    private int configuredHashSize = HASH_BYTE_SIZE;

    @Override
    public void initialize(final Map<String, String> parameters) {
        final String algorithmParameter = parameters.get("Pbkdf2PasswordHash.Algorithm");
        if (algorithmParameter != null) {
            if (!SUPPORTED_ALGORITHMS.contains(algorithmParameter)) {
                final String message = "Unsupported algorithm " + algorithmParameter
                                       + ". Supported algorithms are " + SUPPORTED_ALGORITHMS;
                if (!allowWeakParameters()) {
                    throw new IllegalArgumentException(message);
                }
                LOGGER.warning(message + ". Accepted because " + ALLOW_WEAK_PARAMETERS + " is true");
            }
            configuredAlgorithm = algorithmParameter;
        }

        final String iterationsParameter = parameters.get("Pbkdf2PasswordHash.Iterations");
        if (iterationsParameter != null) {
            final int iterations = Integer.parseInt(iterationsParameter);
            if (iterations < MIN_ITERATIONS) {
                final String message = "Invalid number of iterations " + iterations + ". Must be >= " + MIN_ITERATIONS;
                if (iterations < 1 || !allowWeakParameters()) {
                    throw new IllegalArgumentException(message);
                }
                LOGGER.warning(message + ". Accepted because " + ALLOW_WEAK_PARAMETERS + " is true");
            }
            configuredIterations = iterations;
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
        if (password == null || hashedPassword == null) {
            return false;
        }

        // format: algorithm:iterations:salt:hash, every part comes from storage so validate it
        final String[] params = hashedPassword.split(":", -1);
        if (params.length != 4) {
            return false;
        }

        final String algorithm = params[0];
        final int iterations;
        try {
            iterations = Integer.parseInt(params[1]);
        } catch (final NumberFormatException ex) {
            return false;
        }
        if (algorithm.isEmpty() || iterations < 1) {
            return false;
        }

        final boolean weak = !SUPPORTED_ALGORITHMS.contains(algorithm) || iterations < MIN_ITERATIONS;
        if (weak && !allowWeakParameters()) {
            if (WARNED_REJECTED.compareAndSet(false, true)) {
                LOGGER.warning("Rejected a stored PBKDF2 hash with an unsupported algorithm or fewer than "
                               + MIN_ITERATIONS + " iterations. Set " + ALLOW_WEAK_PARAMETERS
                               + "=true to accept legacy hashes");
            }
            return false;
        }

        final byte[] salt;
        final byte[] expectedHash;
        try {
            salt = fromBase64(params[2]);
            expectedHash = fromBase64(params[3]);
        } catch (final IllegalArgumentException ex) {
            return false;
        }
        if (salt.length == 0 || expectedHash.length == 0) {
            return false;
        }

        final byte[] actual;
        try {
            actual = pbkdf2(password, salt, iterations, expectedHash.length, algorithm);
        } catch (final RuntimeException ex) {
            return false;
        }
        if (weak) {
            warnWeak(algorithm, iterations);
        }
        return slowEquals(expectedHash, actual);
    }

    private static boolean allowWeakParameters() {
        return Boolean.parseBoolean(SystemInstance.get().getProperty(ALLOW_WEAK_PARAMETERS, "false"));
    }

    private static void warnWeak(final String algorithm, final int iterations) {
        final String key = algorithm + ":" + iterations;
        if (WARNED_PARAMETERS.size() < MAX_WARNED_PARAMETERS && WARNED_PARAMETERS.add(key)) {
            LOGGER.warning("Accepted a stored PBKDF2 hash with weak parameters (algorithm " + algorithm
                           + ", " + iterations + " iterations) because " + ALLOW_WEAK_PARAMETERS
                           + " is true. The hash should be regenerated");
        }
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
