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

package org.apache.openejb.cipher;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.util.Base64;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This {@link org.apache.openejb.cipher.PasswordCipher} implementation uses the Triple-DES
 * algorithm with a <b>static key</b> embedded in this class.
 * <p>
 * The key is identical in every distribution and publicly available, so values produced by
 * this cipher are only obfuscated, not encrypted: anyone who obtains a ciphered value (for
 * instance from a configuration file in a source repository or a backup) can decode it.
 * Do not rely on it to keep passwords confidential. Use a {@link PasswordCipher} backed by a
 * secret that is kept outside the configuration (custom implementation or a {@code cdi:}
 * cipher, see {@link CdiPasswordCipher}) instead.
 * <p>
 * A warning is logged once the first time this cipher is used.
 */
public class StaticDESPasswordCipher implements PasswordCipher {

    private static final AtomicBoolean WARNED = new AtomicBoolean();

    private static final byte[] _3desData = {
        (byte) 0x76, (byte) 0x6F, (byte) 0xBA, (byte) 0x39, (byte) 0x31,
        (byte) 0x2F, (byte) 0x0D, (byte) 0x4A, (byte) 0xA3, (byte) 0x90,
        (byte) 0x55, (byte) 0xFE, (byte) 0x55, (byte) 0x65, (byte) 0x61,
        (byte) 0x13, (byte) 0x34, (byte) 0x82, (byte) 0x12, (byte) 0x17,
        (byte) 0xAC, (byte) 0x77, (byte) 0x39, (byte) 0x19};

    private static final SecretKeySpec KEY = new SecretKeySpec(_3desData, "DESede");

    /**
     * The name of the transformation defines Triple-DES encryption
     */
    private static final String TRANSFORMATION = "DESede";

    /**
     * @throws RuntimeException in any case of error.
     * @see org.apache.openejb.cipher.PasswordCipher#encrypt(String)
     */
    public char[] encrypt(final String plainPassword) {
        if (null == plainPassword || plainPassword.length() == 0) {
            throw new IllegalArgumentException("plainPassword cannot be null nor empty.");
        }

        warnOnce();

        final byte[] plaintext = plainPassword.getBytes();
        try {
            // Get a 3DES Cipher object
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            // Set it into encryption mode
            cipher.init(Cipher.ENCRYPT_MODE, KEY);

            // Encrypt data
            final byte[] cipherText = cipher.doFinal(plaintext);
            return new String(Base64.encodeBase64(cipherText)).toCharArray();

        } catch (final Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    /**
     * @throws RuntimeException in any case of error.
     * @see org.apache.openejb.cipher.PasswordCipher#decrypt(char[])
     */
    public String decrypt(final char[] encodedPassword) {
        if (null == encodedPassword || encodedPassword.length == 0) {
            throw new IllegalArgumentException("encodedPassword cannot be null nor empty.");
        }

        warnOnce();

        try {
            final byte[] cipherText = Base64.decodeBase64(
                String.valueOf(encodedPassword).getBytes());

            // Get a 3DES Cipher object
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            // Set it into decryption mode
            cipher.init(Cipher.DECRYPT_MODE, KEY);

            // Decrypt data
            return new String(cipher.doFinal(cipherText));

        } catch (final Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    private static void warnOnce() {
        if (WARNED.compareAndSet(false, true)) {
            Logger.getInstance(LogCategory.OPENEJB, StaticDESPasswordCipher.class)
                .warning("The Static3DES password cipher uses a static key shipped with TomEE: ciphered values "
                    + "are only obfuscated, not protected. Use a PasswordCipher relying on a secret key instead.");
        }
    }

    static void resetWarning() { // for tests
        WARNED.set(false);
    }
}
