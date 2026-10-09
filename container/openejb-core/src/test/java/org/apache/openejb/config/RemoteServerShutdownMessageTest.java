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
package org.apache.openejb.config;

import org.apache.openejb.loader.AdminShutdownSecret;
import org.apache.openejb.loader.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class RemoteServerShutdownMessageTest {

    private File home;
    private String oldSecret;

    @Before
    public void setUp() {
        home = Files.tmpdir();
        oldSecret = System.clearProperty(AdminShutdownSecret.PROPERTY);
    }

    @After
    public void tearDown() {
        if (oldSecret != null) {
            System.setProperty(AdminShutdownSecret.PROPERTY, oldSecret);
        }
        Files.delete(home);
    }

    @Test
    public void standaloneWithoutSecret() {
        assertEquals("Q\0", message("Q"));
        assertEquals("SHUTDOWN\0", message("SHUTDOWN"));
    }

    @Test
    public void standaloneWithSecretFile() throws Exception {
        writeSecret("abc");
        assertEquals("Qabc\0", message("Q"));
        assertEquals("Sabc\0", message("SHUTDOWN"));
        assertEquals("Sabc\0", message("Stop"));
        assertEquals("sabc\0", message("s"));
        assertEquals("qabc\0", message("quit"));
    }

    @Test
    public void standaloneWithConfiguredSecret() throws Exception {
        writeSecret("abc");
        final Properties properties = new Properties();
        properties.setProperty(AdminShutdownSecret.PROPERTY, "configured");
        assertEquals("Qconfigured\0", message("Q", properties));
    }

    @Test
    public void standaloneWithSecretInBase() throws Exception {
        final File base = Files.tmpdir();
        try {
            final File conf = Files.mkdir(new File(base, "conf"));
            java.nio.file.Files.write(new File(conf, AdminShutdownSecret.FILE_NAME).toPath(), "fromBase".getBytes(StandardCharsets.UTF_8));
            final Properties properties = new Properties();
            properties.setProperty("openejb.base", base.getAbsolutePath());
            assertEquals("QfromBase\0", message("Q", properties));
        } finally {
            Files.delete(base);
        }
    }

    @Test
    public void commandAlreadyCarryingTheSecret() throws Exception {
        writeSecret("abc");
        assertEquals("Qmysecret\0", message("Qmysecret"));
        assertEquals("custom\0", message("custom"));
    }

    @Test
    public void tomcatUnchanged() throws Exception {
        writeSecret("abc");
        Files.mkdir(new File(home, "bin"));
        java.nio.file.Files.createFile(new File(new File(home, "bin"), "catalina.sh").toPath());

        assertEquals("SHUTDOWN\0", message("SHUTDOWN"));
        assertEquals("Q\0", message("Q"));
    }

    private String message(final String command) {
        return message(command, new Properties());
    }

    private String message(final String command, final Properties properties) {
        properties.setProperty("openejb.home", home.getAbsolutePath());
        properties.setProperty(RemoteServer.SERVER_SHUTDOWN_COMMAND, command);
        return new String(new RemoteServer(properties, 1, false).shutdownMessage(), StandardCharsets.ISO_8859_1);
    }

    private void writeSecret(final String secret) throws Exception {
        final File conf = Files.mkdir(new File(home, "conf"));
        java.nio.file.Files.write(new File(conf, AdminShutdownSecret.FILE_NAME).toPath(), secret.getBytes(StandardCharsets.UTF_8));
    }
}
