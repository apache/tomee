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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;

import static org.apache.openejb.loader.JarLocation.jarLocation;

/*-------------------------------------------------------*/
/* System ClassLoader Support */
/*-------------------------------------------------------*/
// TODO: we should get rid of it in favor of a custom root loader or flat classpath (ContextClassPath) and update Bootstrap to handle it
public class SystemClassPath extends BasicURLClassPath {

    private URLClassLoader sysLoader;

    @Override
    public void addJarsToPath(final File dir) throws Exception {
        this.addJarsToPath(dir, getSystemLoader());
        if (getSystemLoader() == ClassLoader.getSystemClassLoader()) {
            this.rebuildJavaClassPathVariable();
        }
    }

    @Override
    public void addJarToPath(final URL jar) throws Exception {
        this.addJarToPath(jar, getSystemLoader());
        if (getSystemLoader() == ClassLoader.getSystemClassLoader()) {
            this.rebuildJavaClassPathVariable();
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        try {
            return getSystemLoader();
        } catch (final Exception e) {
            throw new LoaderRuntimeException(e);
        }
    }

    private URLClassLoader getSystemLoader() throws Exception {
        if (sysLoader == null) {
            final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            sysLoader = URLClassLoader.class.isInstance(systemClassLoader) ?
                    URLClassLoader.class.cast(systemClassLoader) : createCustomizableURLClassLoader(systemClassLoader);
        }
        return sysLoader;
    }

    private CustomizableURLClassLoader createCustomizableURLClassLoader(final ClassLoader systemClassLoader) {
        final CustomizableURLClassLoader customizableURLClassLoader = new CustomizableURLClassLoader(systemClassLoader);
        try {
            customizableURLClassLoader.add(jarLocation(SystemClassPath.class).toURI().toURL());
        } catch (final MalformedURLException e) {
            // no-op
        }
        return customizableURLClassLoader;
    }

    private void rebuildJavaClassPathVariable() throws Exception {
        final URLClassLoader loader = getSystemLoader();
        final Object cp = getURLClassPath(loader);
        final Method getURLsMethod = getGetURLsMethod();
        final URL[] urls = (URL[]) getURLsMethod.invoke(cp);

        if (urls.length < 1) {
            return;
        }

        final StringBuilder path = new StringBuilder(urls.length * 32);

        File s = new File(URLDecoder.decode(urls[0].getFile(), "UTF-8"));
        path.append(s.getPath());

        for (int i = 1; i < urls.length; i++) {
            path.append(File.pathSeparator);

            s = new File(URLDecoder.decode(urls[i].getFile(), "UTF-8"));

            path.append(s.getPath());
        }
        try {
            System.setProperty("java.class.path", path.toString());
        } catch (final Exception e) {
            // no-op
        }
    }

    private Method getGetURLsMethod() {
        return AccessController.doPrivileged(new PrivilegedAction<Method>() {
            @Override
            public Method run() {
                try {
                    final URLClassLoader loader = getSystemLoader();
                    final Object cp = getURLClassPath(loader);
                    final Class<?> clazz = cp.getClass();

                    try {
                        return clazz.getDeclaredMethod("getURLs", URL.class);
                    } catch (final NoSuchMethodException e) {
                        return clazz.getDeclaredMethod("getURLs");
                    }

                } catch (final Exception e) {
                    throw new LoaderRuntimeException(e);
                }

            }

        });
    }
}
