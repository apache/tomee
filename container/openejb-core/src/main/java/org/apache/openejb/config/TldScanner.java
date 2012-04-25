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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.filter.Filters;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.apache.openejb.config.NewLoaderLogic.applyBuiltinExcludes;
import static org.apache.openejb.util.URLs.toFile;

/**
 * TLD file urls cached on a per classloader basis.  Helps with sharing TLD
 * files between webapps by placing them in a parent classloader.
 *
 * Each webapp will be able to retrieve the cached version of the URLs and
 * therefore only needs to scan its own libraries, the parent libraries will
 * already have been scanned.
 *
 * For a tiny bit of performance, we will scan the StandardClassloader at boot
 * in a separate thread so it should be primed in advance of any deployment.
 *
 * @version $Rev$ $Date$
 */
public class TldScanner {

    private static Map<ClassLoader, Set<URL>> cache = new WeakHashMap<ClassLoader, Set<URL>>();

    public static Set<URL> scan(final ClassLoader classLoader) throws OpenEJBException {
        if (classLoader == null) return Collections.EMPTY_SET;

        final Set<URL> urls = cache.get(classLoader);
        if (urls != null) return urls;

        final Set<URL> result = scanClassLoaderForTagLibs(classLoader);
        cache.put(classLoader, result);

        return result;
    }

    public static Set<URL> scanClassLoaderForTagLibs(final ClassLoader classLoader) throws OpenEJBException {

        final Set<URL> tldUrls = new HashSet<URL>();

        if (classLoader == null) return tldUrls;
        if (classLoader == Object.class.getClassLoader()) return tldUrls;

        tldUrls.addAll(scan(classLoader.getParent()));


        UrlSet urlSet = new UrlSet();

        if (classLoader instanceof URLClassLoader) {

            final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            urlSet = new UrlSet(urlClassLoader.getURLs());

        } else {
            try {
                urlSet = new UrlSet(classLoader);
            } catch (IOException e) {
                DeploymentLoader.logger.warning("Error scanning class loader for JSP tag libraries", e);
            }
        }

        try {
            urlSet = URLs.cullSystemJars(urlSet);
            urlSet = applyBuiltinExcludes(urlSet, Filters.tokens("jstl-1.2", "myfaces-impl"));
        } catch (IOException e) {
            DeploymentLoader.logger.warning("Error scanning class loader for JSP tag libraries", e);
        }


        for (URL url : urlSet.getUrls()) {
            if (url.getProtocol().equals("jar")) {
                try {
                    String path = url.getPath();
                    if (path.endsWith("!/")) {
                        path = path.substring(0, path.length() - 2);
                    }
                    url = new URL(path);
                } catch (MalformedURLException e) {
                    DeploymentLoader.logger.warning("JSP tag library location bad: " + url.toExternalForm(), e);
                    continue;
                }
            }

            if (!url.getProtocol().equals("file")) {
                continue;
            }

            File file = toFile(url);
            try {
                file = file.getCanonicalFile().getAbsoluteFile();
            } catch (IOException e) {
                DeploymentLoader.logger.warning("JSP tag library location bad: " + file.getAbsolutePath(), e);
                continue;
            }

            tldUrls.addAll(scanForTagLibs(file));
        }

        return tldUrls;
    }

    static Set<URL> scanWarForTagLibs(final File war) {
        final Set<URL> urls = new HashSet<URL>();

        final File webInfDir = new File(war, "WEB-INF");
        if (!webInfDir.isDirectory()) return urls;


        // skip the lib and classes dir in WEB-INF
        final LinkedList<File> files = new LinkedList<File>();
        final File[] list = webInfDir.listFiles();
        if (list != null) {
            for (final File file : list) {
                if ("lib".equals(file.getName()) || "classes".equals(file.getName())) {
                    continue;
                }
                files.add(file);
            }
        }

        if (files.isEmpty()) return urls;

        // recursively scan the directories
        while (!files.isEmpty()) {
            File file = files.removeFirst();
            if (file.isDirectory()) {
                final File[] a = file.listFiles();
                if (a != null) {
                    files.addAll(Arrays.asList(a));
                }
            } else if (file.getName().endsWith(".tld")) {
                try {
                    file = file.getCanonicalFile().getAbsoluteFile();
                    urls.add(file.toURI().toURL());
                } catch (IOException e) {
                    DeploymentLoader.logger.warning("JSP tag library location bad: " + file.getAbsolutePath(), e);
                }
            }
        }

        return urls;
    }

    static Set<URL> scanForTagLibs(final File file) {
        final Set<URL> tldLocations = new HashSet<URL>();
        try {
            final String location = file.toURI().toURL().toExternalForm();

            if (location.endsWith(".jar")) {
                final Set<URL> urls = scanJarForTagLibs(file);
                tldLocations.addAll(urls);
            } else if (file.getName().endsWith(".tld")) {
                final URL url = file.toURI().toURL();
                tldLocations.add(url);
            }
        } catch (IOException e) {
            DeploymentLoader.logger.warning("Error scanning for JSP tag libraries: " + file.getAbsolutePath(), e);
        }

        return tldLocations;
    }

    static Set<URL> scanJarForTagLibs(final File file) {
        final Set<URL> urls = new HashSet<URL>();

        if (!file.isFile()) return urls;

        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file);

            final URL jarFileUrl = new URL("jar", "", -1, file.toURI().toURL().toExternalForm() + "!/");
            for (final JarEntry entry : Collections.list(jarFile.entries())) {
                final String name = entry.getName();
                if (!name.startsWith("META-INF/") || !name.endsWith(".tld")) {
                    continue;
                }
                final URL url = new URL(jarFileUrl, name);
                urls.add(url);
            }
        } catch (IOException e) {
            DeploymentLoader.logger.warning("Error scanning jar for JSP tag libraries: " + file.getAbsolutePath(), e);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    // exception ignored
                }
            }
        }

        return urls;
    }
}
