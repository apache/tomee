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
package org.apache.openejb.loader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

public abstract class BasicURLClassPath implements ClassPath {
    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    private Field ucpField;
    private boolean ucpFieldErrorLogged;

    protected void addJarToPath(final URL jar, final URLClassLoader loader) throws Exception {
        final Object cp = getURLClassPath(loader);
        if (cp == null && CustomizableURLClassLoader.class.isInstance(loader)) {
            CustomizableURLClassLoader.class.cast(loader).add(jar);
        } else {
            getAddURLMethod(loader).invoke(cp, jar);
        }
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
                } catch (final Exception e) {
                    System.err.println("Can't access addURL from URLClassPath");
                }

                return null;
            }

        });
    }

    protected synchronized void addJarsToPath(final File dir, final URLClassLoader loader) throws Exception {
        if (dir == null || !dir.exists()) {
            return;
        }

        final String[] jarNames = dir.list(new java.io.FilenameFilter() {
            @Override
            public boolean accept(final File dir, String name) {
                name = name.toLowerCase(Locale.ENGLISH);
                return name.endsWith(".jar") || name.endsWith(".zip");
            }
        });

        final URL[] jars = new URL[jarNames.length];
        final boolean isWindows = System.getProperty("os.name", "unknown").toLowerCase(Locale.ENGLISH).startsWith("win");

        for (int j = 0; j < jarNames.length; j++) {
            final String name = isWindows ? jarNames[j].toLowerCase() : jarNames[j];
            try {
                jars[j] = new URL(new File(dir, name).getCanonicalFile().getAbsoluteFile().toURI().toURL().toExternalForm());
            } catch (final IOException e) {
                jars[j] = new URL(new File(dir, name).getAbsoluteFile().toURI().toURL().toExternalForm());
            }
        }

        final Object cp = getURLClassPath(loader);
        if (cp == null && CustomizableURLClassLoader.class.isInstance(loader)) {
            final CustomizableURLClassLoader customizableURLClassLoader = CustomizableURLClassLoader.class.cast(loader);
            for (final URL jar : jars) {
                customizableURLClassLoader.add(jar);
            }
        } else if (cp == null && loader != null && CustomizableURLClassLoader.class.getName().equals(loader.getClass().getName())) {
            final Method add = loader.getClass().getMethod("add", URL.class);
            for (final URL jar : jars) {
                add.invoke(loader, jar);
            }
        } else {
            final Method addURLMethod = getAddURLMethod(loader);
            for (final URL jar : jars) {
                addURLMethod.invoke(cp, jar);
            }
        }
    }

    protected Object getURLClassPath(final URLClassLoader loader) throws Exception {
        final Field ucpField = this.getUcpField();
        if (ucpField == null) {
            return null;
        }
        return ucpField.get(loader);
    }

    private Field getUcpField() throws Exception {
        if (ucpField == null) {
            ucpField = AccessController.doPrivileged(new PrivilegedAction<Field>() {
                @Override
                public Field run() {
                    try {
                        final Field ucp = URLClassLoader.class.getDeclaredField("ucp");
                        ucp.setAccessible(true);
                        return ucp;
                    } catch (final Exception e2) {
                        if (!ucpFieldErrorLogged) {
                            System.err.println("Can't get ucp field of URLClassLoader");
                            ucpFieldErrorLogged = true;
                        }
                    }
                    return null;
                }
            });
        }

        return ucpField;
    }

    public static class CustomizableURLClassLoader extends URLClassLoader {
        static {
            ClassLoader.registerAsParallelCapable();
        }

        public CustomizableURLClassLoader(final ClassLoader parent) {
            super(new URL[0], parent);
        }

        public void add(final URL url) {
            super.addURL(url);
        }

        @Override
        protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> clazz = findLoadedClass(name);
                if (clazz != null) {
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }

                if (name != null && !name.startsWith("org.apache.openejb")) {
                    try {
                        return getSystemClassLoader().loadClass(name);
                    } catch (final ClassNotFoundException ignored) {
                        // no-op
                    }
                }

                clazz = loadInternal(name, resolve);
                if (clazz != null) {
                    return clazz;
                }

                clazz = loadFromParent(name, resolve);
                if (clazz != null) {
                    return clazz;
                }

                throw new ClassNotFoundException(name);
            }
        }

        private Class<?> loadFromParent(final String name, final boolean resolve) {
            ClassLoader parent = getParent();
            if (parent == null) {
                parent = getSystemClassLoader();
            }
            try {
                final Class<?> clazz = Class.forName(name, false, parent);
                if (clazz != null) {
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            } catch (final ClassNotFoundException ignored) {
                // no-op
            }
            return null;
        }

        private Class<?> loadInternal(final String name, final boolean resolve) {
            try {
                final Class<?> clazz = findClass(name);
                if (clazz != null) {
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            } catch (final ClassNotFoundException ignored) {
                // no-op
            }
            return null;
        }
    }
}
