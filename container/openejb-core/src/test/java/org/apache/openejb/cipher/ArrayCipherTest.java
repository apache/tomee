/**
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
package org.apache.openejb.cipher;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SimpleLog
@RunWith(ApplicationComposer.class)
@Classes
@ContainerProperties({
    @ContainerProperties.Property(name = "foo", value = "new://Resource?class-name=org.apache.openejb.cipher.ArrayCipherTest$Foo"),
    @ContainerProperties.Property(name = "foo.chars", value = "cipher:org.apache.openejb.cipher.ArrayCipherTest$MySafePasswordCipher:ca"),
    @ContainerProperties.Property(name = "foo.string", value = "cipher:org.apache.openejb.cipher.ArrayCipherTest$MySafePasswordCipher:string")
})
public class ArrayCipherTest {
    @Resource
    private Foo foo;

    @Test
    public void run() {
        assertNotNull(foo);
        assertNotNull(foo.chars);
        assertNotNull(foo.string);
        assertEquals("stringdaca", foo.string);
        assertEquals("cadaca", new String(foo.chars));
    }

    public static class Foo {
        private char[] chars;
        private String string;
    }

    public static class MySafePasswordCipher extends SafePasswordCipherBase {
        @Override
        public char[] encrypt(final String plainPassword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public char[] decryptAsCharArray(char[] encryptedPassword) {
            return (new String(encryptedPassword) + "daca").toCharArray();
        }
    }
}
