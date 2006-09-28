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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

/*-------------------------------------------------------*/
/* Tomcat ClassLoader Support */
/*-------------------------------------------------------*/

public class TomcatClassPath extends BasicURLClassPath {

    private final ClassLoader classLoader;

    private Method addRepositoryMethod;
    private Method addURLMethod;

    public TomcatClassPath() {
        this(getCommonLoader(getContextClassLoader()).getParent());
    }

    public TomcatClassPath(ClassLoader classLoader) {
        this.classLoader = classLoader;
        try {
            addRepositoryMethod = getAddRepositoryMethod();
        } catch (Exception tomcat4Exception) {

            try {
                addURLMethod = getAddURLMethod();
            } catch (Exception tomcat5Exception) {
                throw new RuntimeException("Failed accessing classloader for Tomcat 4 or 5", tomcat5Exception);
            }
        }
    }

    private static ClassLoader getCommonLoader(ClassLoader loader) {
        if (loader.getClass().getName().equals("org.apache.catalina.loader.StandardClassLoader")) {
            return loader;
        } else {
            return getCommonLoader(loader.getParent());
        }
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void addJarsToPath(File dir) throws Exception {
        String[] jarNames = dir.list(new java.io.FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jar") || name.endsWith(".zip"));
            }
        });

        if (jarNames == null) {
            return;
        }

        for (int j = 0; j < jarNames.length; j++) {
            this.addJarToPath(new File(dir, jarNames[j]).toURL());
        }
        rebuild();
    }

    public void addJarToPath(URL jar) throws Exception {
        this._addJarToPath(jar);
        rebuild();
    }

    public void _addJarToPath(URL jar) throws Exception {
        if (addRepositoryMethod != null) {
            String path = jar.toExternalForm();
            addRepositoryMethod.invoke(getClassLoader(), new Object[]{path});
        } else {
            addURLMethod.invoke(getClassLoader(), new Object[]{jar});
        }
    }

    protected void rebuild() {
        try {
            sun.misc.URLClassPath cp = getURLClassPath((URLClassLoader) getClassLoader());
            URL[] urls = cp.getURLs();

            if (urls.length < 1)
                return;

            StringBuffer path = new StringBuffer(urls.length * 32);

            File s = new File(urls[0].getFile());
            path.append(s.getPath());

            for (int i = 1; i < urls.length; i++) {
                path.append(File.pathSeparator);

                s = new File(urls[i].getFile());

                path.append(s.getPath());
            }
            System.setProperty("java.class.path", path.toString());
        } catch (Exception e) {
        }

    }

    private java.lang.reflect.Method getAddURLMethod() throws Exception {
        return (java.lang.reflect.Method) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                java.lang.reflect.Method method = null;
                try {
                    Class clazz = URLClassLoader.class;
                    method = clazz.getDeclaredMethod("addURL", new Class[]{URL.class});
                    method.setAccessible(true);
                    return method;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                return method;
            }
        });
    }

    private Method getAddRepositoryMethod() throws Exception {
        return (Method) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Method method = null;
                try {
                    Class clazz = getClassLoader().getClass();
                    method = clazz.getDeclaredMethod("addRepository", new Class[]{String.class});
                    method.setAccessible(true);
                    return method;
                } catch (Exception e2) {
                    throw (IllegalStateException) new IllegalStateException("Unable to find or access the addRepository method in StandardClassLoader").initCause(e2);
                }
            }
        });
    }

}
