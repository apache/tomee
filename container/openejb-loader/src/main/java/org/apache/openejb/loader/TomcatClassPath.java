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
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/*-------------------------------------------------------*/
/* Tomcat ClassLoader Support */
/*-------------------------------------------------------*/

@SuppressWarnings("unchecked")
public class TomcatClassPath extends BasicURLClassPath {

    private final ClassLoader commonLoader;
    private final ClassLoader serverLoader;

    private Method addRepositoryMethod;

    public TomcatClassPath() {
        this(getCommonLoader(getContextClassLoader()));
    }

    public TomcatClassPath(final ClassLoader classLoader) {
        this.commonLoader = classLoader;

        final ClassLoader serverLoader = getServerLoader(getContextClassLoader());
        if (serverLoader != null && serverLoader != commonLoader) {
            this.serverLoader = serverLoader;
        } else {
            this.serverLoader = null;
        }

    }

    private static ClassLoader getCommonLoader(ClassLoader loader) {
        ClassLoader bootstrapCL;
        try {
            bootstrapCL = loader.loadClass("org.apache.catalina.startup.Bootstrap").getClassLoader();
        } catch (final ClassNotFoundException e) {
            bootstrapCL = ClassLoader.getSystemClassLoader();
        }

        if (loader == bootstrapCL) {
            // this shouldn't happen...
            // means all the tomcat classes are on the system classpath
            // maybe we are in a junit test case?
            return loader;
        }

        while (loader.getParent() != bootstrapCL && loader.getParent() != null) {
            loader = loader.getParent();
        }
        return loader;
    }

    private static ClassLoader getServerLoader(final ClassLoader loader) {
        try {
            return loader.loadClass("org.apache.catalina.Container").getClassLoader();
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return (serverLoader != null) ? serverLoader : commonLoader;
        //        return commonLoader;
    }

    @SuppressWarnings("UnusedDeclaration")
    public ClassLoader getCommonLoader() {
        return commonLoader;
    }

    @Override
    public void addJarsToPath(final File dir) throws Exception {
        final String[] jarNames = dir.list(new java.io.FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return (name.endsWith(".jar") || name.endsWith(".zip"));
            }
        });

        if (jarNames == null) {
            return;
        }

        for (final String jarName : jarNames) {
            this.addJarToPath(new File(dir, jarName).toURI().toURL());
        }
        rebuild();
    }

    @Override
    public void addJarToPath(final URL jar) throws Exception {
        this._addJarToPath(jar);
        rebuild();
    }

    public void _addJarToPath(final URL jar) throws Exception {
        ClassLoader classLoader = commonLoader;

        if (serverLoader != null && useServerClassLoader(jar)) {
            classLoader = serverLoader;
        }

        getAddRepositoryMethod().invoke(classLoader, jar);
    }

    private boolean useServerClassLoader(final URL jar) {
        try {
            final URL url = findResource("META-INF/org.apache.openejb.tomcat/ServerClassLoader", jar);
            return url != null;
        } catch (final Exception e) {
            return false;
        }
    }

    private Method getGetURLsMethod() {

        return AccessController.doPrivileged(new PrivilegedAction<Method>() {

            @Override
            public Method run() {
                try {
                    final Object cp = getURLClassPath((URLClassLoader) getClassLoader());
                    final Class<?> clazz = cp.getClass();
                    return clazz.getDeclaredMethod("addURL", URL.class);
                } catch (final Exception e) {
                    throw new LoaderRuntimeException(e);
                }

            }

        });
    }

    protected void rebuild() {
        try {
            final Object cp = getURLClassPath((URLClassLoader) getClassLoader());
            final Method getURLsMethod = getGetURLsMethod();
            //noinspection NullArgumentToVariableArgMethod
            final URL[] urls = (URL[]) getURLsMethod.invoke(cp, (Object) null);

            if (urls.length < 1) {
                return;
            }

            final StringBuilder path = new StringBuilder(urls.length * 32);

            File s;
            try {
                s = new File(URLDecoder.decode(urls[0].getFile(), "UTF-8"));
            } catch (final Exception e) {
                //noinspection deprecation
                s = new File(URLDecoder.decode(urls[0].getFile()));
            }

            path.append(s.getPath());

            for (int i = 1; i < urls.length; i++) {
                path.append(File.pathSeparator);

                try {
                    s = new File(URLDecoder.decode(urls[i].getFile(), "UTF-8"));
                } catch (final Exception e) {
                    //noinspection deprecation
                    s = new File(URLDecoder.decode(urls[i].getFile()));
                }

                path.append(s.getPath());
            }
            System.setProperty("java.class.path", path.toString());
        } catch (final Exception e) {
            Logger.getLogger(TomcatClassPath.class.getName()).log(Level.FINE, "rebuild", e);
        }

    }

    private Method getAddRepositoryMethod() throws Exception {
        if (addRepositoryMethod == null) {
            try {
                addRepositoryMethod = AccessController.doPrivileged(new PrivilegedAction<Method>() {
                    @Override
                    public Method run() {
                        try {
                            final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                            if (!method.isAccessible()) {
                                method.setAccessible(true);
                            }
                            return method;
                        } catch (final Exception e2) {
                            throw new IllegalStateException("Unable to find or access the addRepository method in StandardClassLoader", e2);
                        }
                    }
                });
            } catch (final Exception e) {
                throw new IllegalStateException("Ensure you use the right tomcat version (" + e.getMessage() + ")", e);
            }
        }
        return addRepositoryMethod;
    }

    private static boolean isDirectory(final URL url) {
        final String file = url.getFile();
        return (file.length() > 0 && file.charAt(file.length() - 1) == '/');
    }

    @SuppressWarnings("ConstantConditions")
    private static URL findResource(final String resourceName, final URL... search) {

        for (int i = 0; i < search.length; i++) {
            URL currentUrl = search[i];
            if (currentUrl == null) {
                continue;
            }

            if (currentUrl == null || isDirectory(currentUrl) || currentUrl.getProtocol().equals("jar")) {
                continue;
            }
            try {
                currentUrl = new URL("jar", "", -1, currentUrl.toString() + "!/");
            } catch (final MalformedURLException e) {
                Logger.getLogger(TomcatClassPath.class.getName()).log(Level.FINE, "findResource", e);
            }

            final JarFile jarFile;
            try {
                final String protocol = currentUrl.getProtocol();
                if (protocol.equals("jar")) {
                    /*
                    * If the connection for currentUrl or resURL is
                    * used, getJarFile() will throw an exception if the
                    * entry doesn't exist.
                    */
                    final URL jarURL = ((JarURLConnection) currentUrl.openConnection()).getJarFileURL();
                    try {
                        final JarURLConnection juc = (JarURLConnection) new URL("jar", "", jarURL.toExternalForm() + "!/").openConnection();
                        jarFile = juc.getJarFile();
                    } catch (final IOException e) {
                        // Don't look for this jar file again
                        search[i] = null;
                        throw e;
                    }

                    final String entryName;
                    if (currentUrl.getFile().endsWith("!/")) {
                        entryName = resourceName;
                    } else {
                        final String file = currentUrl.getFile();
                        int sepIdx = file.lastIndexOf("!/");
                        if (sepIdx == -1) {
                            // Invalid URL, don't look here again
                            search[i] = null;
                            continue;
                        }
                        sepIdx += 2;
                        final StringBuilder sb = new StringBuilder(file.length() - sepIdx + resourceName.length());
                        sb.append(file.substring(sepIdx));
                        sb.append(resourceName);
                        entryName = sb.toString();
                    }
                    if (entryName.equals("META-INF/") && jarFile.getEntry("META-INF/MANIFEST.MF") != null) {
                        return targetURL(currentUrl, "META-INF/MANIFEST.MF");
                    }
                    if (jarFile.getEntry(entryName) != null) {
                        return targetURL(currentUrl, resourceName);
                    }
                } else if (protocol.equals("file")) {
                    final String baseFile = currentUrl.getFile();
                    final String host = currentUrl.getHost();
                    int hostLength = 0;
                    if (host != null) {
                        hostLength = host.length();
                    }
                    final StringBuilder buf = new StringBuilder(2 + hostLength + baseFile.length() + resourceName.length());

                    if (hostLength > 0) {
                        buf.append("//").append(host);
                    }
                    // baseFile always ends with '/'
                    buf.append(baseFile);
                    String fixedResName = resourceName;
                    // Do not create a UNC path, i.e. \\host
                    while (fixedResName.startsWith("/") || fixedResName.startsWith("\\")) {
                        fixedResName = fixedResName.substring(1);
                    }
                    buf.append(fixedResName);
                    final String filename = buf.toString();
                    final File file = new File(filename);
                    File file2;
                    try {
                        file2 = new File(URLDecoder.decode(filename, "UTF-8"));
                    } catch (final Exception e) {
                        //noinspection deprecation
                        file2 = new File(URLDecoder.decode(filename));
                    }
                    if (file.exists() || file2.exists()) {
                        return targetURL(currentUrl, fixedResName);
                    }
                } else {
                    final URL resourceURL = targetURL(currentUrl, resourceName);
                    final URLConnection urlConnection = resourceURL.openConnection();

                    try {
                        urlConnection.getInputStream().close();
                    } catch (final SecurityException e) {
                        return null;
                    }
                    // HTTP can return a stream on a non-existent file
                    // So check for the return code;
                    if (!resourceURL.getProtocol().equals("http")) {
                        return resourceURL;
                    }

                    final int code = ((HttpURLConnection) urlConnection).getResponseCode();
                    if (code >= 200 && code < 300) {
                        return resourceURL;
                    }
                }
            } catch (final MalformedURLException e) {
                // Keep iterating through the URL list
            } catch (final Exception e) {
                Logger.getLogger(TomcatClassPath.class.getName()).log(Level.FINE, "findResource", e);
            }
        }
        return null;
    }

    private static URL targetURL(final URL base, final String name) throws MalformedURLException {
        final StringBuilder sb = new StringBuilder(base.getFile().length() + name.length());
        sb.append(base.getFile());
        sb.append(name);
        final String file = sb.toString();
        return new URL(base.getProtocol(), base.getHost(), base.getPort(), file, null);
    }

}
