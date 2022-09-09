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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.config;

import io.churchkey.Key;
import io.churchkey.Keys;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.tomee.microprofile.jwt.KeyAsserts;
import org.junit.Test;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class PublicKeyResolverTest {

    @Test
    public void publicKeyPemFromFileUrl() throws Exception {
        final File dir = Files.tmpdir();
        final Key privateKey = generate(Key.Algorithm.RSA);
        final Key expected = privateKey.getPublicKey();

        final File file = new File(dir, "publicKey.pem");
        IO.copy(expected.encode(Key.Format.PEM), file);

        final Map<String, java.security.Key> keys = new PublicKeyResolver().resolve(
                Optional.empty(),
                Optional.of(file.toURI().toASCIIString())).get();

        assertEquals(1, keys.size());
        final java.security.Key actual = keys.values().iterator().next();

        KeyAsserts.assertRsaPublicKey((RSAPublicKey) expected.getKey(), (RSAPublicKey) actual);
    }

    @Test
    public void publicKeyPemContents() throws Exception {
        final Key privateKey = generate(Key.Algorithm.RSA);
        final Key expected = privateKey.getPublicKey();

        final Map<String, java.security.Key> keys = new PublicKeyResolver().resolve(
                Optional.of(expected.toPem()),
                Optional.empty()).get();

        assertEquals(1, keys.size());
        final java.security.Key actual = keys.values().iterator().next();

        KeyAsserts.assertRsaPublicKey((RSAPublicKey) expected.getKey(), (RSAPublicKey) actual);
    }

    private Key generate(final Key.Algorithm algorithm) throws NoSuchAlgorithmException {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm.name());
        final KeyPair pair = generator.generateKeyPair();
        return Keys.of(pair);
    }

}
