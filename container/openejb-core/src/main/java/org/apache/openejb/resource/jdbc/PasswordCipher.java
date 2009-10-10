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

/**
 * Implementations of {@link PasswordCipher} allow to encode and decode passwords
 * used to connect to a database.
 * <p/>
 * Several implementations may exist, as several encryption algorithms may be
 * supported. One-way encryption algorithm (hash) can't be used as we need to
 * give a plain password to the database. {@link #encrypt(String)} method is not
 * mandatory as we don't need to encode a password, but it's useful to get the
 * encrypted value for a given plain text password. In the case you have
 * implemented both methods, you can use the PasswordCodec command line tool to
 * encode/decode a password.
 */
public interface PasswordCipher {

    /**
     * Encodes a given plain text password and returns the encoded password.
     * 
     * @param plainPassword
     *            The password to encode. May not be <code>null</code>, nor empty.
     * @return The encoded password.
     */
    public char[] encrypt(String plainPassword);

    /**
     * Decodes an encoded password and returns a plain text password.
     * 
     * @param encryptedPassword
     *            The ciphered password to decode. May not be <code>null</code>,
     *            nor empty.
     * @return The plain text password.
     */
    public String decrypt(char[] encryptedPassword);

}
