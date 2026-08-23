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
package org.apache.openejb.loader.provisining;

import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.jar.JarFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MavenResolverTest {
    private MavenResolver resolver;

    @Before
    public void init() {
        resolver = new MavenResolver();
        resolver.setResolver(new ProvisioningResolver());
    }

    @Test
    public void local() throws Exception {
        try (InputStream is = resolver.resolve("mvn:junit:junit:4.12:jar")) {
            Assert.assertNotNull(is); // use version of the pom to ensure it is local
        }
    }

    private File getAvailableFile() {
        File file = null;
        for (int i = 0; i < 100; i++) {
            file = new File("target/test/foo_" + i + ".jar");
            if (!file.exists()) {
                Files.mkdirs(file.getParentFile());
                break;
            }
        }
        return file;
    }

    public void resolveCommon(final String path) throws Exception {
        final File file = getAvailableFile();
        final FileOutputStream to = new FileOutputStream(file);
        IO.copy(resolver.resolve(path), to);
        IO.close(to);
        assertTrue(file.exists());
        assertTrue(Collections.list(new JarFile(file).entries()).size() > 300 /* 323 */); // just check it is not an error page
    }

    @Test
    public void resolve() throws Exception {
        resolveCommon("mvn:junit:junit:4.12:jar");
        resolveCommon("mvn:https://repo1.maven.org/maven2/!junit:junit:4.12:jar");
        resolveCommon("mvn:https://repo1.maven.org/maven2/!junit:junit:LATEST:jar");
    }

    @Test
    public void doctypeInMetadataKeepsRequestedVersion() throws Exception {
        final File external = File.createTempFile("metadata", ".txt");
        external.deleteOnExit();
        try (final FileOutputStream out = new FileOutputStream(external)) {
            out.write("1.2.3".getBytes("UTF-8"));
        }

        final String xml = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE metadata [<!ENTITY ext SYSTEM \"" + external.toURI().toURL() + "\">]>\n"
                + "<metadata><versioning><release>&ext;</release><latest>&ext;</latest></versioning></metadata>";
        final ByteArrayOutputStream metadata = new ByteArrayOutputStream();
        metadata.write(xml.getBytes("UTF-8"));

        final Method extract = MavenResolver.class.getDeclaredMethod("extractRealVersion", String.class, ByteArrayOutputStream.class);
        extract.setAccessible(true);
        assertEquals("LATEST", extract.invoke(null, "LATEST", metadata));
    }

    @Test
    public void overrideRepo() throws Exception {
        System.setProperty("openejb.deployer.repository", "https://bob.smith/repo/");
        final String url = resolver.quickMvnUrl("!junit/junit/4.12/jar");
        assertEquals("https://bob.smith/repo/junit/junit/4.12/junit-4.12.jar", url);
        System.clearProperty("openejb.deployer.repository");
    }
}
