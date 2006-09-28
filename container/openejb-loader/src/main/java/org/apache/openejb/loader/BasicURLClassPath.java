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
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

public abstract class BasicURLClassPath implements ClassPath {
    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    private java.lang.reflect.Field ucpField;

    protected void addJarToPath(final URL jar, final URLClassLoader loader) throws Exception {
        this.getURLClassPath(loader).addURL(jar);
    }

    protected void addJarsToPath(final File dir, final URLClassLoader loader) throws Exception {
        if (dir == null || !dir.exists()) return;

        String[] jarNames = dir.list(new java.io.FilenameFilter() {
            public boolean accept(File dir, String name) {

                return (name.endsWith(".jar") || name.endsWith(".zip"));
            }
        });

        final URL[] jars = new URL[jarNames.length];
        for (int j = 0; j < jarNames.length; j++) {
            jars[j] = new File(dir, jarNames[j]).toURL();
        }

        sun.misc.URLClassPath path = getURLClassPath(loader);
        for (int i = 0; i < jars.length; i++) {

            path.addURL(jars[i]);
        }
    }

    protected sun.misc.URLClassPath getURLClassPath(URLClassLoader loader) throws Exception {
        return (sun.misc.URLClassPath) getUcpField().get(loader);
    }

    private java.lang.reflect.Field getUcpField() throws Exception {
        if (ucpField == null) {

            ucpField = (java.lang.reflect.Field) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
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
