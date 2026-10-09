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
package org.apache.openejb.core.stateful;

import org.apache.openejb.core.EnvProps;
import org.apache.openejb.loader.SystemInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SimplePassivaterTest {

    @Before
    @After
    public void reset() {
        SystemInstance.reset();
    }

    @Test
    public void defaultDirectoryIsPrivate() throws Exception {
        final File dir = sessionDirectory(new SimplePassivater());

        assertTrue(dir.isDirectory());
        assertNotEquals(tmpdir(), dir.getCanonicalFile());
        assertEquals(tmpdir(), dir.getCanonicalFile().getParentFile());
        if (Files.getFileStore(dir.toPath()).supportsFileAttributeView(PosixFileAttributeView.class)) {
            assertEquals(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE),
                    Files.getPosixFilePermissions(dir.toPath()));
        }
    }

    @Test
    public void sharedTmpdirOptOut() throws Exception {
        SystemInstance.get().setProperty(SimplePassivater.SHARED_TMPDIR, "true");

        assertEquals(tmpdir(), sessionDirectory(new SimplePassivater()).getCanonicalFile());
    }

    @Test
    public void configuredDirectoryIsUsed() throws Exception {
        final Properties props = new Properties();
        props.setProperty(EnvProps.IM_PASSIVATOR_PATH_PREFIX, "passivation-test");
        final SimplePassivater passivater = new SimplePassivater();
        passivater.init(props);

        final File dir = sessionDirectory(passivater);
        try {
            assertTrue(dir.isDirectory());
            assertEquals(SystemInstance.get().getBase().getDirectory("passivation-test").getCanonicalFile(), dir.getCanonicalFile());
        } finally {
            dir.delete();
        }
    }

    @Test
    public void passivateAndActivate() throws Exception {
        final SimplePassivater passivater = new SimplePassivater();
        passivater.passivate("some:key", "state");

        assertTrue(new File(sessionDirectory(passivater), "some=key").isFile());
        assertEquals("state", passivater.activate("some:key"));
    }

    private static File tmpdir() throws Exception {
        return new File(System.getProperty("java.io.tmpdir")).getCanonicalFile();
    }

    private static File sessionDirectory(final SimplePassivater passivater) throws Exception {
        final Field field = SimplePassivater.class.getDeclaredField("sessionDirectory");
        field.setAccessible(true);
        return (File) field.get(passivater);
    }
}
