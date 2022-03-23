/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.tomee.gradle.embedded.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;

import static java.util.Collections.emptyEnumeration;

public class FilterGradleClassLoader extends ClassLoader {
    private final ClassLoader delegate;
    private final Collection<String> filtered;

    public FilterGradleClassLoader(final ClassLoader gradle, final Collection<String> filtered) {
        super(gradle.getParent());
        this.delegate = gradle;
        this.filtered = filtered;
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        checkClass(name);
        return delegate.loadClass(name);
    }

    @Override
    public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        return loadClass(name);
    }

    @Override
    public URL getResource(final String name) {
        if (!checkResource(name)) {
            return null;
        }
        return delegate.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        final Enumeration<URL> resources = delegate.getResources(name);
        if (!checkResource(name)) {
            return emptyEnumeration();
        }
        return resources;
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        if (!checkResource(name)) {
            return null;
        }
        return delegate.getResourceAsStream(name);
    }

    private void checkClass(final String name) throws ClassNotFoundException { // let slf4j+gradle go to reuse it
        if (name != null && (
                name.startsWith("aQute") ||
                        name.startsWith("bsh") ||
                        name.startsWith("com.amazon") ||
                        name.startsWith("com.beust") ||
                        name.startsWith("com.esot") ||
                        name.startsWith("com.google") ||
                        name.startsWith("com.jackson") ||
                        name.startsWith("com.jcraft") ||
                        name.startsWith("com.tonixsystems") ||
                        name.startsWith("jakarta.el") ||
                        name.startsWith("jakarta.inject") ||
                        name.startsWith("jakarta.servlet") ||
                        name.startsWith("jcifs.") ||
                        name.startsWith("junit.") ||
                        name.startsWith("groovy") ||
                        name.startsWith("mozilla.") ||
                        name.startsWith("net.jcip.") ||
                        name.startsWith("net.ruby") ||
                        name.startsWith("org.apache.") ||
                        name.startsWith("org.bouncycastle.") ||
                        name.startsWith("org.codehaus.") ||
                        name.startsWith("org.cyber") ||
                        name.startsWith("org.dom4j.") ||
                        name.startsWith("org.eclipse.") ||
                        name.startsWith("org.fusesource.") ||
                        name.startsWith("org.hamcrest.") ||
                        name.startsWith("org.jaxen.") ||
                        name.startsWith("org.mortbay.") ||
                        name.startsWith("org.mozilla.") ||
                        name.startsWith("org.objectweb.") ||
                        name.startsWith("org.objenesis.") ||
                        name.startsWith("org.osgi.") ||
                        name.startsWith("org.simpleframework.") ||
                        name.startsWith("org.sonar.") ||
                        name.startsWith("org.sonatype.") ||
                        name.startsWith("org.testng.") ||
                        name.startsWith("org.yaml.") ||
                        isForbiddenGradleClass(name) ||
                        isFiltered(name)
        )) {
            throw new ClassNotFoundException();
        }
    }

    private boolean isFiltered(final String name) {
        if (filtered == null || name == null) {
            return false;
        }
        for (final String pck : filtered) {
            if (name.startsWith(pck)) {
                return true;
            }
        }
        return false;
    }

    private boolean isForbiddenGradleClass(final String name) { // we need logging classes but we don't want to scan gradle
        return name.startsWith("org.gradle.initialization") || name.startsWith("org.gradle.launcher")
                || name.startsWith("org.gradle.execution") || name.startsWith("org.gradle.internal")
                || name.startsWith("org.gradle.tooling") || name.startsWith("org.gradle.api.internal.tasks")
                || name.startsWith("org.gradle.util") || name.startsWith("org.gradle.wrapper");
    }

    private boolean checkResource(final String name) {
        return name != null && !name.startsWith("META-INF/services/com.fasterxml");
    }
}
