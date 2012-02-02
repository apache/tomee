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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.loader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

public abstract class BasicURLClassPath implements ClassPath {
    public static ClassLoader getContextClassLoader() {
        return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    private java.lang.reflect.Field ucpField;

    protected void addJarToPath(final URL jar, final URLClassLoader loader) throws Exception {
        final Object cp = getURLClassPath(loader);
        final Method addURLMethod = getAddURLMethod(loader);
        addURLMethod.invoke(cp, new URL[]{jar});
    }

    private Method getAddURLMethod(final URLClassLoader loader) {
        return AccessController.doPrivileged(new PrivilegedAction<Method>() {
            @Override
            public Method run() {
                final Object cp;
                try {
                    cp = getURLClassPath(loader);
                    final Class<?> clazz = cp.getClass();
                    return clazz.getDeclaredMethod("addURL", URL.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

        });
    }

    protected synchronized void addJarsToPath(final File dir, final URLClassLoader loader) throws Exception {
        if (dir == null || !dir.exists()) return;

        final String[] jarNames = dir.list(new java.io.FilenameFilter() {
            @Override
            public boolean accept(final File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jar") || name.endsWith(".zip");
            }
        });

        final URL[] jars = new URL[jarNames.length];
        final boolean isWindows = System.getProperty("os.name", "unknown").toLowerCase().startsWith("windows");

        for (int j = 0; j < jarNames.length; j++) {
            final String name = isWindows ? jarNames[j].toLowerCase() : jarNames[j];
            try {
                jars[j] = new URL(new File(dir, name).getCanonicalFile().getAbsoluteFile().toURI().toURL().toExternalForm());
            } catch (IOException e) {
                jars[j] = new URL(new File(dir, name).getAbsoluteFile().toURI().toURL().toExternalForm());
            }
        }

        final Object cp = getURLClassPath(loader);
        final Method addURLMethod = getAddURLMethod(loader);
        for (final URL jar : jars) {
            addURLMethod.invoke(cp, new URL[]{jar});
        }
    }

    protected Object getURLClassPath(final URLClassLoader loader) throws Exception {
        return getUcpField().get(loader);
    }

    private java.lang.reflect.Field getUcpField() throws Exception {
        if (ucpField == null) {
            ucpField = AccessController.doPrivileged(new PrivilegedAction<Field>() {
                @Override
                public Field run() {
                    java.lang.reflect.Field ucp = null;
                    try {
                        ucp = URLClassLoader.class.getDeclaredField("ucp");
                        ucp.setAccessible(true);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    return ucp;
                }
            });
        }

        return ucpField;
    }

}
