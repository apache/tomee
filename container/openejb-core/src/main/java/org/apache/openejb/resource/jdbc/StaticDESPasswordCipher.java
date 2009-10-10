/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.resource.jdbc;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.openejb.util.Base64;

/**
 * This {@link PasswordCipher} implementation uses a the Triple-DES encryption
 * algorithm.
 */
public class StaticDESPasswordCipher implements PasswordCipher {

    private static final byte[] _3desData = { 
            (byte) 0x76, (byte) 0x6F, (byte) 0xBA, (byte) 0x39, (byte) 0x31, 
            (byte) 0x2F, (byte) 0x0D, (byte) 0x4A, (byte) 0xA3, (byte) 0x90, 
            (byte) 0x55, (byte) 0xFE, (byte) 0x55, (byte) 0x65, (byte) 0x61, 
            (byte) 0x13, (byte) 0x34, (byte) 0x82, (byte) 0x12, (byte) 0x17, 
            (byte) 0xAC, (byte) 0x77, (byte) 0x39, (byte) 0x19 };

    private static final SecretKeySpec KEY = new SecretKeySpec(_3desData, "DESede");

    /**
     * The name of the transformation defines Triple-DES encryption
     */
    private static final String TRANSFORMATION = new String("DESede");

    /**
     * @see PasswordCipher#encrypt(java.lang.String)
     * @throws RuntimeException
     *             in any case of error.
     */
    public char[] encrypt(String plainPassword) {
        if ((null == plainPassword) || plainPassword.length() == 0) {
            throw new IllegalArgumentException("plainPassword cannot be null nor empty.");
        }

        byte[] plaintext = plainPassword.getBytes();
        try {
            // Get a 3DES Cipher object
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            // Set it into encryption mode
            cipher.init(Cipher.ENCRYPT_MODE, KEY);

            // Encrypt data
            byte[] cipherText = cipher.doFinal(plaintext);
            return new String(Base64.encodeBase64(cipherText)).toCharArray();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see PasswordCipher#decrypt(char[])
     * @throws RuntimeException
     *             in any case of error.
     */
    public String decrypt(char[] encodedPassword) {
        if ((null == encodedPassword) || encodedPassword.length == 0) {
            throw new IllegalArgumentException("encodedPassword cannot be null nor empty.");
        }

        try {
            byte[] cipherText = Base64.decodeBase64(
                    String.valueOf(encodedPassword).getBytes());

            // Get a 3DES Cipher object
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            // Set it into decryption mode
            cipher.init(Cipher.DECRYPT_MODE, KEY);

            // Decrypt data
            String plainText = new String(cipher.doFinal(cipherText));
            return plainText;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
