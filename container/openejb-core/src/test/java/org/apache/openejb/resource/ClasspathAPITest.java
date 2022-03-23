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
package org.apache.openejb.resource;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import java.io.File;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Callable;

import static org.apache.openejb.loader.JarLocation.jarLocation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Classes
@RunWith(ApplicationComposer.class)
public class ClasspathAPITest {
    @Configuration
    public Properties config() {
        // using relative path avoids issue between IDE/surefire (. = target for surefire) and URI format
        final Path here = new File(".").getAbsoluteFile().toPath();
        final Path classes = jarLocation(ClasspathAPITest.class).getAbsoluteFile().toPath();
        return new PropertiesBuilder()
                .p("r", "new://Resource?class-name=org.apache.openejb.resource.ClasspathAPITest$MyImpl&" +
                        "classpath-api=java.util.concurrent.Callable&" +
                        "classpath=" + classes.relativize(here).toString().replace(File.separator, "/"))
                .build();
    }

    @Resource(name = "r")
    private Callable<String> impl;

    @Test
    public void check() throws Exception {
        assertTrue(Callable.class.isInstance(impl));
        assertFalse(Runnable.class.isInstance(impl));
        assertEquals("ok", impl.call());
    }

    public static class MyImpl implements Runnable, Callable<String> {
        @Override
        public void run() {
            fail();
        }

        @Override
        public String call() throws Exception {
            return "ok";
        }
    }
}
