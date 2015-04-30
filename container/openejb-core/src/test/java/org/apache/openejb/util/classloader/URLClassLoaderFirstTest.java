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
package org.apache.openejb.util.classloader;

import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.loader.SystemInstance;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import javax.wsdl.WSDLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class URLClassLoaderFirstTest {
    @Test
    public void loadFromAppIfNotInContainer() throws Exception {
        assertTrue(URLClassLoaderFirst.shouldSkip("javax.wsdl.WSDLException"));

        final URLClassLoader parent = new URLClassLoader(new URL[0]) {
            @Override
            public URL getResource(final String name) {
                if ("javax/wsdl/WSDLException.class".equals(name)) {
                    return null;
                }
                return super.getResource(name);
            }
        };
        final URLClassLoader tmpLoader = new URLClassLoaderFirst(new URL[]{JarLocation.jarLocation(WSDLException.class).toURI().toURL()}, parent);

        SystemInstance.init(new Properties());
        SystemInstance.get().setComponent(ParentClassLoaderFinder.class, new ParentClassLoaderFinder() {
            @Override
            public ClassLoader getParentClassLoader(final ClassLoader fallback) {
                return parent;
            }
        });

        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(tmpLoader);
        try {
            assertFalse(URLClassLoaderFirst.shouldSkip("javax.wsdl.WSDLException"));
        } finally {
            Thread.currentThread().setContextClassLoader(old);
            SystemInstance.reset();
        }

        assertTrue(URLClassLoaderFirst.shouldSkip("javax.wsdl.WSDLException"));
        SystemInstance.reset();
    }
}
