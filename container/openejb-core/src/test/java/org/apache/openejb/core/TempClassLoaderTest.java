/**
 *
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
package org.apache.openejb.core;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class TempClassLoaderTest {
    @BeforeClass
    public static void init() {
        SystemInstance.get().setProperty("openejb.classloader.forced-load", "org.apache.openejb.core");
        URLClassLoaderFirst.reloadConfig();
    }

    @AfterClass
    public static void reset() {
        SystemInstance.get().getProperties().remove("openejb.classloader.forced-load");
        URLClassLoaderFirst.reloadConfig();
    }

    @Test
    public void test() throws Exception {
        final ClassLoader tempCL = new TempClassLoader(this.getClass().getClassLoader());
        Class<?> clazz;

        // normal classes should be loaded by the temp class loader
        clazz = tempCL.loadClass(TempClassLoaderTest.class.getName());
        assertSame(tempCL, clazz.getClassLoader());

        // classes in java.* should not be loaded by the temp class loader
        clazz = tempCL.loadClass(jakarta.persistence.EntityManager.class.getName());
        assertNotSame(tempCL, clazz.getClassLoader());

        // classes in javax.* should not be loaded by the temp class loader
        clazz = tempCL.loadClass(java.lang.String.class.getName());
        assertNotSame(tempCL, clazz.getClassLoader());

        // annotations should not be loaded by the temp class loader
        clazz = tempCL.loadClass(SampleAnnotation.class.getName());
        assertSame(tempCL, clazz.getClassLoader());
    }

    @Test
    public void testHackEnabled() throws Exception {
        final TempClassLoader tempCL = new TempClassLoader(this.getClass().getClassLoader());
        tempCL.skip(TempClassLoader.Skip.ANNOTATIONS);

        Class<?> clazz;

        // normal classes should be loaded by the temp class loader
        clazz = tempCL.loadClass(TempClassLoaderTest.class.getName());
        assertSame(tempCL, clazz.getClassLoader());

        // classes in java.* should not be loaded by the temp class loader
        clazz = tempCL.loadClass(jakarta.persistence.EntityManager.class.getName());
        assertNotSame(tempCL, clazz.getClassLoader());

        // classes in javax.* should not be loaded by the temp class loader
        clazz = tempCL.loadClass(java.lang.String.class.getName());
        assertNotSame(tempCL, clazz.getClassLoader());

        // annotations should not be loaded by the temp class loader
        clazz = tempCL.loadClass(SampleAnnotation.class.getName());
        assertNotSame(tempCL, clazz.getClassLoader());
    }
}
