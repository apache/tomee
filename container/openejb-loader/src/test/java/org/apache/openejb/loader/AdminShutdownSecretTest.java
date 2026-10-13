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
package org.apache.openejb.loader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AdminShutdownSecretTest {

    private File home;
    private String oldProperty;

    @Before
    public void setUp() {
        home = Files.tmpdir();
        oldProperty = System.clearProperty(AdminShutdownSecret.PROPERTY);
    }

    @After
    public void tearDown() {
        if (oldProperty == null) {
            System.clearProperty(AdminShutdownSecret.PROPERTY);
        } else {
            System.setProperty(AdminShutdownSecret.PROPERTY, oldProperty);
        }
        Files.delete(home);
    }

    @Test
    public void noSecret() {
        assertNull(AdminShutdownSecret.find(home));
        assertNull(AdminShutdownSecret.find(null, null));
        assertNull(AdminShutdownSecret.find("  ", home));
    }

    @Test
    public void secretFromFile() throws Exception {
        writeSecretFile(" abc123\n");
        assertEquals("abc123", AdminShutdownSecret.find(home));
        assertEquals("abc123", AdminShutdownSecret.findInConf(null, new File(home, "conf")));
    }

    @Test
    public void blankFileIsIgnored() throws Exception {
        writeSecretFile("\n");
        assertNull(AdminShutdownSecret.find(home));
    }

    @Test
    public void propertyWinsOverFile() throws Exception {
        writeSecretFile("fromfile");
        assertEquals("configured", AdminShutdownSecret.find(" configured ", home));

        System.setProperty(AdminShutdownSecret.PROPERTY, "fromsystem");
        assertEquals("fromsystem", AdminShutdownSecret.find(home));
        assertEquals("fromsystem", AdminShutdownSecret.find(null));
    }

    @Test
    public void stopMessage() {
        assertArrayEquals(new byte[]{'S', 'a', 'b', 0}, AdminShutdownSecret.stopMessage('S', "ab"));
        assertArrayEquals(new byte[]{'Q', 0}, AdminShutdownSecret.stopMessage('Q', null));
        assertArrayEquals(new byte[]{'q', (byte) 0xC3, (byte) 0xA9, 0}, AdminShutdownSecret.stopMessage('q', "é"));
    }

    private void writeSecretFile(final String content) throws Exception {
        final File conf = Files.mkdir(new File(home, "conf"));
        java.nio.file.Files.write(new File(conf, AdminShutdownSecret.FILE_NAME).toPath(), content.getBytes(StandardCharsets.UTF_8));
    }
}
