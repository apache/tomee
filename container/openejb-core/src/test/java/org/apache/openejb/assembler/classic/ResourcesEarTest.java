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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.URLs;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ResourcesEarTest {
    @Test
    public void checkAppModuleGetResources() throws IOException, OpenEJBException {
        final File root = new File("target/ResourcesEarTest/myear/");
        final File temp = Files.mkdirs(new File(root, "META-INF/"));
        IO.copy(URLs.toFile(Thread.currentThread().getContextClassLoader().getResource("descriptor-resources.xml")), new File(temp, "resources.xml"));
        Files.deleteOnExit(root);

        final AtomicReference<AppModule> moduleAtomicReference = new AtomicReference<>();
        new DeploymentLoader() {{
            moduleAtomicReference.set(createAppModule(root.getAbsoluteFile(), root.getPath()));
        }};

        final AppModule object = moduleAtomicReference.get();
        assertNotNull(object);
        assertEquals(1, object.getResources().size());
        assertEquals("jdbc/descriptors", object.getResources().iterator().next().getId());
    }

    public static class Touch {}
}
