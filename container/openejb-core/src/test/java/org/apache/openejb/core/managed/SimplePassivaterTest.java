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
package org.apache.openejb.core.managed;

import org.apache.openejb.loader.SystemInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SimplePassivaterTest {

    @Before
    @After
    public void reset() {
        SystemInstance.reset();
    }

    @Test
    public void passivatesToPrivateDirectoryByDefault() throws Exception {
        final String key = "managed-" + UUID.randomUUID();
        final SimplePassivater passivater = new SimplePassivater();
        passivater.passivate(key, "state");
        try {
            assertFalse(new File(tmpdir(), key).exists());

            final File[] dirs = tmpdir().listFiles((d, name) -> name.startsWith("openejb-passivation-") && new File(d, name + File.separator + key).isFile());
            assertEquals(1, dirs.length);
            if (Files.getFileStore(dirs[0].toPath()).supportsFileAttributeView(PosixFileAttributeView.class)) {
                assertEquals(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE),
                        Files.getPosixFilePermissions(dirs[0].toPath()));
            }
        } finally {
            assertEquals("state", passivater.activate(key));
        }
    }

    @Test
    public void sharedTmpdirOptOut() throws Exception {
        SystemInstance.get().setProperty(org.apache.openejb.core.stateful.SimplePassivater.SHARED_TMPDIR, "true");

        final String key = "managed-" + UUID.randomUUID();
        final SimplePassivater passivater = new SimplePassivater();
        passivater.passivate(key, "state");
        try {
            assertTrue(new File(tmpdir(), key).isFile());
        } finally {
            assertEquals("state", passivater.activate(key));
        }
    }

    private static File tmpdir() throws Exception {
        return new File(System.getProperty("java.io.tmpdir")).getCanonicalFile();
    }
}
