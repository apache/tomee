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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.JarURLConnection;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.jar.JarFile;

/*-------------------------------------------------------*/
/* Tomcat ClassLoader Support */
/*-------------------------------------------------------*/

public class TomcatClassPath extends BasicURLClassPath {

    private final ClassLoader commonLoader;
    private final ClassLoader serverLoader;

    private Method addRepositoryMethod;
    private Method addURLMethod;

    public TomcatClassPath() {
        this(getCommonLoader(getContextClassLoader()));
    }

    public TomcatClassPath(ClassLoader classLoader) {
        this.commonLoader = classLoader;
        try {
            addRepositoryMethod = getAddRepositoryMethod();
        } catch (Exception tomcat4Exception) {

            try {
                addURLMethod = getAddURLMethod();
            } catch (Exception tomcat5Exception) {
                throw new RuntimeException("Failed accessing classloader for Tomcat 5 or 6", tomcat5Exception);
            }
        }

        ClassLoader serverLoader = getServerLoader(getContextClassLoader());
        if (serverLoader != null && serverLoader != commonLoader){
            this.serverLoader = serverLoader;
        } else this.serverLoader = null;

    }

    private static ClassLoader getCommonLoader(ClassLoader loader) {
        ClassLoader bootstrapCL;
        try {
            bootstrapCL = loader.loadClass("org.apache.catalina.startup.Bootstrap").getClassLoader();
        } catch (ClassNotFoundException e) {
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

    private static ClassLoader getServerLoader(ClassLoader loader) {
        try {
            return loader.loadClass("org.apache.catalina.Container").getClassLoader();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public ClassLoader getClassLoader() {
        return (serverLoader != null)? serverLoader: commonLoader;
//        return commonLoader;
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

        for (String jarName : jarNames) {
            this.addJarToPath(new File(dir, jarName).toURL());
        }
        rebuild();
    }

    public void addJarToPath(URL jar) throws Exception {
        this._addJarToPath(jar);
        rebuild();
    }

    public void _addJarToPath(URL jar) throws Exception {
        ClassLoader classLoader = commonLoader;

        if (serverLoader != null && useServerClassLoader(jar)){
            classLoader = serverLoader;
        }

        if (addRepositoryMethod != null) {
            String path = jar.toExternalForm();
            addRepositoryMethod.invoke(classLoader, path);
        } else {
            addURLMethod.invoke(classLoader, jar);
        }
    }

    private boolean useServerClassLoader(URL jar) {
        try {
            URL url = findResource("META-INF/org.apache.openejb.tomcat/ServerClassLoader", jar);
            return url != null;
        } catch (Exception e) {
            return false;
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

    private Method getAddURLMethod() throws Exception {
        return AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                Method method = null;
                try {
                    Class clazz = URLClassLoader.class;
                    method = clazz.getDeclaredMethod("addURL", URL.class);
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
        return AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                Method method;
                try {
                    Class clazz = getClassLoader().getClass();
                    method = clazz.getDeclaredMethod("addRepository", String.class);
                    method.setAccessible(true);
                    return method;
                } catch (Exception e2) {
                    throw (IllegalStateException) new IllegalStateException("Unable to find or access the addRepository method in StandardClassLoader").initCause(e2);
                }
            }
        });
    }

    private static boolean isDirectory(URL url) {
        String file = url.getFile();
        return (file.length() > 0 && file.charAt(file.length() - 1) == '/');
    }

    private static URL findResource(String resourceName, URL... search) {
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
            } catch (MalformedURLException e) {
            }


            JarFile jarFile;
            try {
                String protocol = currentUrl.getProtocol();
                if (protocol.equals("jar")) {
                    /*
                    * If the connection for currentUrl or resURL is
                    * used, getJarFile() will throw an exception if the
                    * entry doesn't exist.
                    */
                    URL jarURL = ((JarURLConnection) currentUrl.openConnection()).getJarFileURL();
                    try {
                        JarURLConnection juc = (JarURLConnection) new URL("jar", "", jarURL.toExternalForm() + "!/").openConnection();
                        jarFile = juc.getJarFile();
                    } catch (IOException e) {
                        // Don't look for this jar file again
                        search[i] = null;
                        throw e;
                    }

                    String entryName;
                    if (currentUrl.getFile().endsWith("!/")) {
                        entryName = resourceName;
                    } else {
                        String file = currentUrl.getFile();
                        int sepIdx = file.lastIndexOf("!/");
                        if (sepIdx == -1) {
                            // Invalid URL, don't look here again
                            search[i] = null;
                            continue;
                        }
                        sepIdx += 2;
                        StringBuffer sb = new StringBuffer(file.length() - sepIdx + resourceName.length());
                        sb.append(file.substring(sepIdx));
                        sb.append(resourceName);
                        entryName = sb.toString();
                    }
                    if (entryName.equals("META-INF/") && jarFile.getEntry("META-INF/MANIFEST.MF") != null){
                        return targetURL(currentUrl, "META-INF/MANIFEST.MF");
                    }
                    if (jarFile.getEntry(entryName) != null) {
                        return targetURL(currentUrl, resourceName);
                    }
                } else if (protocol.equals("file")) {
                    String baseFile = currentUrl.getFile();
                    String host = currentUrl.getHost();
                    int hostLength = 0;
                    if (host != null) {
                        hostLength = host.length();
                    }
                    StringBuffer buf = new StringBuffer(2 + hostLength + baseFile.length() + resourceName.length());

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
                    String filename = buf.toString();
                    File file = new File(filename);
                    if (file.exists()) {
                        return targetURL(currentUrl, fixedResName);
                    }
                } else {
                    URL resourceURL = targetURL(currentUrl, resourceName);
                    URLConnection urlConnection = resourceURL.openConnection();

                    try {
                        urlConnection.getInputStream().close();
                    } catch (SecurityException e) {
                        return null;
                    }
                    // HTTP can return a stream on a non-existent file
                    // So check for the return code;
                    if (!resourceURL.getProtocol().equals("http")) {
                        return resourceURL;
                    }

                    int code = ((HttpURLConnection) urlConnection).getResponseCode();
                    if (code >= 200 && code < 300) {
                        return resourceURL;
                    }
                }
            } catch (MalformedURLException e) {
                // Keep iterating through the URL list
            } catch (IOException e) {
            } catch (SecurityException e) {
            }
        }
        return null;
    }

    private static URL targetURL(URL base, String name) throws MalformedURLException {
        StringBuffer sb = new StringBuffer(base.getFile().length() + name.length());
        sb.append(base.getFile());
        sb.append(name);
        String file = sb.toString();
        return new URL(base.getProtocol(), base.getHost(), base.getPort(), file, null);
    }

}
