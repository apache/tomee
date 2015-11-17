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
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
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
        assertEquals(ProvisioningResolver.LocalInputStream.class.getName(), resolver.resolve("mvn:junit:junit:4.12:jar").getClass().getName()); // use version of the pom to ensure it is local
    }

    @Test
    public void resolve() throws Exception {
        final File file = new File("target/test/foo.jar");
        Files.remove(file);
        Files.mkdirs(file.getParentFile());
        final FileOutputStream to = new FileOutputStream(file);
        IO.copy(resolver.resolve("mvn:junit:junit:4.12:jar"), to);
        IO.close(to);
        assertTrue(file.exists());
        assertTrue(Collections.list(new JarFile(file).entries()).size() > 300 /* 323 */); // just check it is not an error page
    }

    @Test
    public void customRepo() throws Exception {
        final File file = new File("target/test/foo.jar");
        Files.remove(file);
        Files.mkdirs(file.getParentFile());
        final FileOutputStream to = new FileOutputStream(file);
        IO.copy(resolver.resolve("mvn:http://repo1.maven.org/maven2/!junit:junit:4.12:jar"), to);
        IO.close(to);
        assertTrue(file.exists());
        assertTrue(Collections.list(new JarFile(file).entries()).size() > 300 /* 323 */); // just check it is not an error page
    }

    @Test
    public void latest() throws Exception {
        final File file = new File("target/test/foo.jar");
        Files.remove(file);
        Files.mkdirs(file.getParentFile());
        final FileOutputStream to = new FileOutputStream(file);
        IO.copy(resolver.resolve("mvn:http://repo1.maven.org/maven2/!junit:junit:LATEST:jar"), to);
        IO.close(to);
        assertTrue(file.exists());
        assertTrue(Collections.list(new JarFile(file).entries()).size() > 10 /* 323 */); // just check it is not an error page
    }
}
