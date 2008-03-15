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

import junit.framework.TestCase;

public class TempClassLoaderTest extends TestCase {
    public void test() throws Exception {
        ClassLoader tempCL = new TempClassLoader(getClass().getClassLoader());
        Class<?> clazz;

        // normal classes should be loaded by the temp class loader
        clazz = tempCL.loadClass(TempClassLoaderTest.class.getName());
        assertSame(tempCL, clazz.getClassLoader());

        // classes in java.* should not be loaded by the temp class loader
        clazz = tempCL.loadClass(javax.persistence.EntityManager.class.getName());
        assertNotSame(tempCL, clazz.getClassLoader());

        // classes in javax.* should not be loaded by the temp class loader
        clazz = tempCL.loadClass(java.lang.String.class.getName());
        assertNotSame(tempCL, clazz.getClassLoader());

        // annotations should not be loaded by the temp class loader
        clazz = tempCL.loadClass(SampleAnnotation.class.getName());
        assertNotSame(tempCL, clazz.getClassLoader());
    }
}
