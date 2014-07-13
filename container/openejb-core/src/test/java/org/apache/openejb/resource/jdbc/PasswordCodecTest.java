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

import junit.framework.TestCase;
import org.apache.openejb.cipher.PasswordCipherFactory;
import org.apache.openejb.cipher.PasswordCipher;
import org.apache.openejb.cipher.PlainTextPasswordCipher;
import org.apache.openejb.cipher.StaticDESPasswordCipher;

import java.sql.SQLException;

public class PasswordCodecTest extends TestCase {

    private static final String PLAIN_PWD = "david";

    public void testPlainCodec() {
        final PasswordCipher cipher = new PlainTextPasswordCipher();
        assertEquals(PLAIN_PWD, new String(cipher.encrypt(PLAIN_PWD)));
        assertEquals(PLAIN_PWD, cipher.decrypt(PLAIN_PWD.toCharArray()));
    }

    public void testStaticDesCodec() {
        final PasswordCipher cipher = new StaticDESPasswordCipher();
        final char[] tmp = cipher.encrypt(PLAIN_PWD);
        assertEquals(PLAIN_PWD, cipher.decrypt(tmp));
    }

    public void testGetDataSourcePlugin() throws Exception {
        // all current known plugins
        assertPluginClass("PlainText", PlainTextPasswordCipher.class);
        assertPluginClass("Static3DES", StaticDESPasswordCipher.class);

        // null
        try {
            PasswordCipherFactory.getPasswordCipher(null);
            fail("Should throw an exception when no codec is found.");
        } catch (final Exception e) {
            // OK
        }

        // empty string
        try {
            PasswordCipherFactory.getPasswordCipher("");
            fail("Should throw an exception when no codec is found.");
        } catch (final Exception e) {
            // OK
        }

        // try the FQN of the target codec
        assertNotNull(PasswordCipherFactory.getPasswordCipher(PlainTextPasswordCipher.class.getName()));
    }

    private void assertPluginClass(final String pluginName, final Class<? extends PasswordCipher> pluginClass) throws SQLException {
        final PasswordCipher plugin = PasswordCipherFactory.getPasswordCipher(pluginName);
        assertNotNull(plugin);
        assertTrue(pluginClass.isInstance(plugin));
    }

}
