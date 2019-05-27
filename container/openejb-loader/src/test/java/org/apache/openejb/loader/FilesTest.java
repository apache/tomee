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
package org.apache.openejb.loader;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilesTest {

    private final static File FILE = new File("target/test/foo.jar");

    @Test
    public void testDelete() throws Exception {
        doDelete(false);
    }

    @Test
    public void testRemove() throws Exception {
        doDelete(true);
    }

    private void doDelete(final boolean remove) throws IOException {

        final long start = System.nanoTime();

        for (int i = 0; i < 20; i++) {

            if (remove) {
                Files.remove(FILE);
            } else {
                Files.delete(FILE);
            }

            Files.mkdirs(FILE.getParentFile());
            assertTrue(FILE.createNewFile());
            assertTrue(FILE.exists());
        }

        assertTrue(FILE.getParentFile().exists());

        if (remove) {
            Files.remove(FILE.getParentFile());
        } else {
            Files.delete(FILE.getParentFile());
        }

        assertFalse(FILE.exists());
        assertFalse(FILE.getParentFile().exists());

        final long time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, String.format("Completed File.%1$s in %2$sms" , remove ? "remove" : "delete", String.valueOf(time)));
    }
}