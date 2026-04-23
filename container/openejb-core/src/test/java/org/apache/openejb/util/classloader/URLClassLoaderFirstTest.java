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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
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

    @Test
    public void jakartaFacesAlwaysDelegatesToContainerEvenWhenWarBundlesApi() throws Exception {
        // Regression for the Security 4.0 TCK app-mem-customform deployment failure: the
        // TCK WAR ships jakarta.faces-api alongside MyFaces in the server lib/, so two
        // copies of jakarta.faces.webapp.FacesServlet are visible to the webapp loader.
        // shouldSkipJsf must still report "delegate to container" so MyFaces' impl
        // classes (linked against the container copy) don't see a foreign FacesServlet
        // and explode the StartupServletContextListener with a LinkageError.
        final URL apiCopy = new URL("file:/tmp/fake-jakarta-faces-api.jar");
        final URL implCopy = new URL("file:/tmp/fake-myfaces-impl.jar");
        final ClassLoader multiCopyLoader = new ClassLoader(getClass().getClassLoader()) {
            @Override
            public Enumeration<URL> getResources(final String name) throws IOException {
                if ("jakarta/faces/webapp/FacesServlet.class".equals(name)
                        || "jakarta/faces/FactoryFinder.class".equals(name)) {
                    return Collections.enumeration(Arrays.asList(apiCopy, implCopy));
                }
                return super.getResources(name);
            }
        };

        assertTrue("FacesServlet must delegate to container even with two copies on the path",
                URLClassLoaderFirst.shouldSkipJsf(multiCopyLoader, "jakarta.faces.webapp.FacesServlet"));
        assertTrue("Other jakarta.faces.* classes must also delegate",
                URLClassLoaderFirst.shouldSkipJsf(multiCopyLoader, "jakarta.faces.application.Application"));
        assertTrue("shouldDelegateToTheContainer must agree",
                URLClassLoaderFirst.shouldDelegateToTheContainer(multiCopyLoader, "jakarta.faces.webapp.FacesServlet"));
        assertFalse("non-jakarta.faces names must be unaffected",
                URLClassLoaderFirst.shouldSkipJsf(multiCopyLoader, "com.example.NotFaces"));
    }

    @Test
    public void jakartaFacesDelegatesToContainerWithSingleCopy() throws Exception {
        // Sanity check the original passing case (only the container copy visible) still holds.
        final URL containerCopy = new URL("file:/tmp/fake-myfaces-impl.jar");
        final ClassLoader singleCopyLoader = new ClassLoader(getClass().getClassLoader()) {
            @Override
            public Enumeration<URL> getResources(final String name) throws IOException {
                if ("jakarta/faces/webapp/FacesServlet.class".equals(name)
                        || "jakarta/faces/FactoryFinder.class".equals(name)) {
                    return Collections.enumeration(Collections.singletonList(containerCopy));
                }
                return super.getResources(name);
            }
        };

        assertTrue(URLClassLoaderFirst.shouldSkipJsf(singleCopyLoader, "jakarta.faces.application.Application"));
    }
}
