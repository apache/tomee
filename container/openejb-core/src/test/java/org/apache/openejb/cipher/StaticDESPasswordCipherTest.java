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

import org.apache.openejb.util.LogCategory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StaticDESPasswordCipherTest {
    private static final String MARKER = "StaticDESPasswordCipherTest-done";

    private final List<String> warnings = new CopyOnWriteArrayList<>();
    private final CountDownLatch done = new CountDownLatch(1);
    private final Handler handler = new Handler() {
        @Override
        public void publish(final LogRecord record) {
            final String message = record.getMessage();
            if (message == null) {
                return;
            }
            if (message.contains("Static3DES")) {
                warnings.add(message);
            } else if (message.contains(MARKER)) {
                done.countDown();
            }
        }

        @Override
        public void flush() {
            // no-op
        }

        @Override
        public void close() {
            // no-op
        }
    };

    @Before
    public void captureLogs() {
        StaticDESPasswordCipher.resetWarning();
        Logger.getLogger(LogCategory.OPENEJB.getName()).addHandler(handler);
    }

    @After
    public void releaseLogs() {
        Logger.getLogger(LogCategory.OPENEJB.getName()).removeHandler(handler);
    }

    @Test
    public void existingValuesStillDecrypt() {
        assertEquals("Passw0rd", new StaticDESPasswordCipher().decrypt("xMH5uM1V9vQzVUv5LG7YLA==".toCharArray()));
        assertEquals("xMH5uM1V9vQzVUv5LG7YLA==", new String(new StaticDESPasswordCipher().encrypt("Passw0rd")));
    }

    @Test
    public void warnsOnlyOnce() throws InterruptedException {
        final StaticDESPasswordCipher cipher = new StaticDESPasswordCipher();
        for (int i = 0; i < 3; i++) {
            assertEquals("secret", cipher.decrypt(cipher.encrypt("secret")));
        }
        new StaticDESPasswordCipher().decrypt("xMH5uM1V9vQzVUv5LG7YLA==".toCharArray());

        // logging can be asynchronous, wait until everything logged before the marker is published
        org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, StaticDESPasswordCipherTest.class).warning(MARKER);
        assertTrue(done.await(1, TimeUnit.MINUTES));

        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("obfuscated"));
    }
}
