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
import org.junit.After;
import org.junit.Test;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TomEEPbkdf2PasswordHashTest {

    private static final char[] PASSWORD = "s3cret".toCharArray();

    @After
    public void resetProperty() {
        SystemInstance.get().getProperties().remove(TomEEPbkdf2PasswordHash.ALLOW_WEAK_PARAMETERS);
    }

    private static void allowWeakParameters() {
        SystemInstance.get().setProperty(TomEEPbkdf2PasswordHash.ALLOW_WEAK_PARAMETERS, "true");
    }

    @Test
    public void defaultHashVerifies() {
        final TomEEPbkdf2PasswordHash hash = new TomEEPbkdf2PasswordHash();
        final String stored = hash.generate(PASSWORD);

        assertTrue(stored.startsWith("PBKDF2WithHmacSHA1:64000:"));
        assertTrue(hash.verify(PASSWORD, stored));
        assertFalse(hash.verify("wrong".toCharArray(), stored));
    }

    @Test
    public void nonDefaultParametersVerify() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("Pbkdf2PasswordHash.Algorithm", "PBKDF2WithHmacSHA512");
        parameters.put("Pbkdf2PasswordHash.Iterations", "1024");
        parameters.put("Pbkdf2PasswordHash.SaltSizeBytes", "64");
        parameters.put("Pbkdf2PasswordHash.KeySizeBytes", "32");
        final TomEEPbkdf2PasswordHash generator = new TomEEPbkdf2PasswordHash();
        generator.initialize(parameters);

        final String stored = generator.generate(PASSWORD);
        assertTrue(stored.startsWith("PBKDF2WithHmacSHA512:1024:"));

        // verification relies on the stored parameters, not on the configured ones
        assertTrue(new TomEEPbkdf2PasswordHash().verify(PASSWORD, stored));
        assertTrue(generator.verify(PASSWORD, stored));
    }

    @Test
    public void allSpecAlgorithmsVerify() throws Exception {
        final TomEEPbkdf2PasswordHash hash = new TomEEPbkdf2PasswordHash();
        for (final String algorithm : new String[]{
            "PBKDF2WithHmacSHA224", "PBKDF2WithHmacSHA256", "PBKDF2WithHmacSHA384", "PBKDF2WithHmacSHA512"}) {
            assertTrue(algorithm, hash.verify(PASSWORD, stored(algorithm, 2048, PASSWORD)));
        }
    }

    @Test
    public void weakIterationsRejected() throws Exception {
        final TomEEPbkdf2PasswordHash hash = new TomEEPbkdf2PasswordHash();
        // correctly computed hashes, but with an iteration count below the minimum
        assertFalse(hash.verify(PASSWORD, stored("PBKDF2WithHmacSHA256", 1, PASSWORD)));
        assertFalse(hash.verify(PASSWORD, stored("PBKDF2WithHmacSHA256", 1023, PASSWORD)));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:-5:AAAA:AAAA"));
    }

    @Test
    public void unknownAlgorithmRejected() throws Exception {
        final TomEEPbkdf2PasswordHash hash = new TomEEPbkdf2PasswordHash();
        final String valid = stored("PBKDF2WithHmacSHA256", 2048, PASSWORD);
        assertTrue(hash.verify(PASSWORD, valid));
        assertFalse(hash.verify(PASSWORD, valid.replaceFirst("PBKDF2WithHmacSHA256", "PBKDF2WithHmacMD5")));
        assertFalse(hash.verify(PASSWORD, "HmacSHA256:2048:AAAAAAAA:AAAAAAAA"));
        assertFalse(hash.verify(PASSWORD, "DoesNotExist:2048:AAAAAAAA:AAAAAAAA"));
    }

    @Test
    public void malformedRejected() {
        final TomEEPbkdf2PasswordHash hash = new TomEEPbkdf2PasswordHash();
        assertFalse(hash.verify(PASSWORD, null));
        assertFalse(hash.verify(PASSWORD, ""));
        assertFalse(hash.verify(PASSWORD, "garbage"));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:2048:AAAAAAAA"));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:2048:AAAAAAAA:"));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:2048:AAAAAAAA:AAAAAAAA:extra"));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:abc:AAAAAAAA:AAAAAAAA"));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:2048:not*base64:AAAAAAAA"));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:2048::AAAAAAAA"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeRejectsUnknownAlgorithm() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("Pbkdf2PasswordHash.Algorithm", "PBKDF2WithHmacMD5");
        new TomEEPbkdf2PasswordHash().initialize(parameters);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeRejectsWeakIterations() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("Pbkdf2PasswordHash.Iterations", "1000");
        new TomEEPbkdf2PasswordHash().initialize(parameters);
    }

    @Test
    public void weakParametersAcceptedWhenAllowed() throws Exception {
        allowWeakParameters();
        final TomEEPbkdf2PasswordHash hash = new TomEEPbkdf2PasswordHash();
        final String fewIterations = stored("PBKDF2WithHmacSHA256", 1, PASSWORD);
        assertTrue(hash.verify(PASSWORD, fewIterations));
        assertTrue(hash.verify(PASSWORD, stored("PBKDF2WithHmacSHA256", 1023, PASSWORD)));
        assertFalse(hash.verify("wrong".toCharArray(), fewIterations));

        // an algorithm outside the allowed set but available in the JVM
        final String sha512_224 = "PBKDF2WithHmacSHA512/224";
        if (isAvailable(sha512_224)) {
            assertTrue(hash.verify(PASSWORD, stored(sha512_224, 2048, PASSWORD)));
        }
    }

    @Test
    public void weakParametersStillRejectedByDefault() throws Exception {
        final TomEEPbkdf2PasswordHash hash = new TomEEPbkdf2PasswordHash();
        assertFalse(hash.verify(PASSWORD, stored("PBKDF2WithHmacSHA256", 1, PASSWORD)));
        SystemInstance.get().setProperty(TomEEPbkdf2PasswordHash.ALLOW_WEAK_PARAMETERS, "false");
        assertFalse(hash.verify(PASSWORD, stored("PBKDF2WithHmacSHA256", 1, PASSWORD)));
    }

    @Test
    public void malformedAndUnknownRejectedWhenAllowed() {
        allowWeakParameters();
        final TomEEPbkdf2PasswordHash hash = new TomEEPbkdf2PasswordHash();
        assertFalse(hash.verify(PASSWORD, "garbage"));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:2048:AAAAAAAA"));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:abc:AAAAAAAA:AAAAAAAA"));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:2048:not*base64:AAAAAAAA"));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:2048::AAAAAAAA"));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:0:AAAAAAAA:AAAAAAAA"));
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacSHA256:-5:AAAAAAAA:AAAAAAAA"));
        assertFalse(hash.verify(PASSWORD, ":2048:AAAAAAAA:AAAAAAAA"));
        // not supported by the JVM: fails safely
        assertFalse(hash.verify(PASSWORD, "PBKDF2WithHmacMD5:2048:AAAAAAAA:AAAAAAAA"));
        assertFalse(hash.verify(PASSWORD, "DoesNotExist:2048:AAAAAAAA:AAAAAAAA"));
    }

    @Test
    public void initializeAcceptsWeakParametersWhenAllowed() {
        allowWeakParameters();
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("Pbkdf2PasswordHash.Iterations", "1000");
        final TomEEPbkdf2PasswordHash generator = new TomEEPbkdf2PasswordHash();
        generator.initialize(parameters);

        final String stored = generator.generate(PASSWORD);
        assertEquals("PBKDF2WithHmacSHA1:1000", stored.substring(0, stored.indexOf(':', stored.indexOf(':') + 1)));
        assertTrue(generator.verify(PASSWORD, stored));

        parameters.clear();
        parameters.put("Pbkdf2PasswordHash.Algorithm", "PBKDF2WithHmacMD5");
        new TomEEPbkdf2PasswordHash().initialize(parameters);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeRejectsNonPositiveIterationsWhenAllowed() {
        allowWeakParameters();
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("Pbkdf2PasswordHash.Iterations", "0");
        new TomEEPbkdf2PasswordHash().initialize(parameters);
    }

    private static boolean isAvailable(final String algorithm) {
        try {
            SecretKeyFactory.getInstance(algorithm);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    private static String stored(final String algorithm, final int iterations, final char[] password) throws Exception {
        final byte[] salt = new byte[24];
        for (int i = 0; i < salt.length; i++) {
            salt[i] = (byte) i;
        }
        final byte[] derived = SecretKeyFactory.getInstance(algorithm)
            .generateSecret(new PBEKeySpec(password, salt, iterations, 32 * 8))
            .getEncoded();
        final Base64.Encoder encoder = Base64.getEncoder();
        return algorithm + ":" + iterations + ":" + encoder.encodeToString(salt) + ":" + encoder.encodeToString(derived);
    }
}
