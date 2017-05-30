/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.transport.util;

import org.apache.openejb.util.classloader.Unwrappable;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class CxfContainerClassLoader extends ClassLoader implements Unwrappable {
    private static final ClassLoader CONTAINER_LOADER = CxfUtil.class.getClassLoader();
    public static final String CXF_PACKAGE = "org.apache.cxf.";

    private final ThreadLocal<ClassLoader> tccl = new ThreadLocal<ClassLoader>();

    public CxfContainerClassLoader() {
        super(CONTAINER_LOADER);
    }

    public void tccl(final ClassLoader loader) {
        if (loader != this) { // otherwise it will end up with infinite loops
            tccl.set(loader);
        }
    }

    public void clear() {
        tccl.remove();
    }

    public boolean hasTccl() {
        final ClassLoader current = tccl.get();
        if (current != null) {
            return true;
        }

        tccl.remove();
        return false;
    }

    private ClassLoader tccl() {
        final ClassLoader current = tccl.get();
        if (current != null) {
            return current;
        } else {
            tccl.remove();
        }
        return CONTAINER_LOADER;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        if (name.startsWith(CXF_PACKAGE)) {
            Throwable th = new ClassNotFoundException(name);
            try {
                return CONTAINER_LOADER.loadClass(name);
            } catch (final Exception e) {
                th = e;
            } catch (final Error err) {
                th = err;
            }

            // some additional cxf classes can be provided by apps
            try {
                return tccl().loadClass(name);
            } catch (final Exception | Error e) {
                // no-op: try tccl
            }

            // if we are here we were not able to load the class from the container
            // so throw the first exception
            if (Error.class.isInstance(th)) {
                throw Error.class.cast(th);
            }
            if (ClassNotFoundException.class.isInstance(th)) {
                throw ClassNotFoundException.class.cast(th);
            }
            throw new ClassNotFoundException(th.getMessage(), th);
        }

        return tccl().loadClass(name);
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        final Class<?> clazz = findClass(name);
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    @Override
    protected URL findResource(final String name) {
        return tccl().getResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(final String name) throws IOException {
        return tccl().getResources(name);
    }

    @Override
    public boolean equals(final Object o) {
        final ClassLoader classLoader = tccl();
        if (classLoader == null || CxfContainerClassLoader.class.isInstance(classLoader)) { // avoid loop
            return CONTAINER_LOADER.equals(o);
        }
        return classLoader.equals(o);
    }

    @Override
    public int hashCode() {
        final ClassLoader classLoader = tccl();
        if (classLoader == null || CxfContainerClassLoader.class.isInstance(classLoader)) { // avoid loop
            return CONTAINER_LOADER.hashCode();
        }
        return classLoader.hashCode();
    }

    @Override
    public ClassLoader unwrap() {
        return tccl();
    }
}
