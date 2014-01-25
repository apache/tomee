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
import org.apache.openejb.util.DaemonThreadFactory;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.filter.Filters;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    // first cache, it is the faster one but not relevant between temp and runtime phases
    private static Map<ClassLoader, Set<URL>> cache = new WeakHashMap<ClassLoader, Set<URL>>();

    // tld by classloader identified by hash on urls (same hash for temp and runtime classloaders)
    // a bit longer to compute but let scanning be reused over temp and runtime classloaders
    private static Map<Integer, Set<URL>> cacheByhashCode = new WeakHashMap<Integer, Set<URL>>();

    public static Set<URL> scan(final ClassLoader classLoader) throws OpenEJBException {
        if (classLoader == null) return Collections.emptySet();

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

        final List<URL> urls = urls(classLoader);

        int hashCodeForUrls = hash(urls);
        Set<URL> cachedSet = cacheByhashCode.get(hashCodeForUrls);
        if (cachedSet != null) {
            return cachedSet;
        }

        tldUrls.addAll(scan(classLoader.getParent()));

        if (urls.size() > 0) {
            final ExecutorService es = Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors() + 1, new DaemonThreadFactory("OpenEJB-tld-server-scanning"));

            final Collection<Future<Set<URL>>> futures = new ArrayList<Future<Set<URL>>>(urls.size());
            for (URL url : urls) {
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

                final File file;
                try {
                    file = toFile(url).getCanonicalFile().getAbsoluteFile();
                } catch (IOException e) {
                    DeploymentLoader.logger.warning("JSP tag library location bad: " + url.toExternalForm(), e);
                    continue;
                }

                futures.add(es.submit(new Callable<Set<URL>>() {
                    @Override
                    public Set<URL> call() throws Exception {
                        return scanForTagLibs(file);
                    }
                }));
            }

            es.shutdown();

            for (Future<Set<URL>> set : futures) {
                try {
                    tldUrls.addAll(set.get());
                } catch (Exception e) {
                    // no-op
                }
            }
        }

        cacheByhashCode.put(hashCodeForUrls, tldUrls);

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

    // mainly used to forget a classloader (temp one generally) but keep scanning info from classloader urls
    public static void quickClean(final ClassLoader loader) {
        if (loader == null) {
            return;
        }

        cache.remove(loader);
        if (loader.getParent() != TldScanner.class.getClassLoader()) { // for ears
            quickClean(loader.getParent());
        }
    }

    // this method clean the cacheByhash too
    public static void forceCompleteClean(final ClassLoader loader) {
        if (loader == null) {
            return;
        }

        quickClean(loader);
        cacheByhashCode.remove(hash(urls(loader)));

        if (loader.getParent() != TldScanner.class.getClassLoader()) { // for ears
            forceCompleteClean(loader.getParent());
        }
    }

    private static List<URL> urls(final ClassLoader classLoader) {
        UrlSet urlSet = new UrlSet();

        if (classLoader instanceof URLClassLoader) {

            final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            try {
                urlSet = new UrlSet(urlClassLoader.getURLs());
            } catch (NullPointerException npe) { // happen for closeable classloaders like WebappClassLoader when already clean up
                return Collections.emptyList();
            }

        } else {
            try {
                urlSet = new UrlSet(classLoader);
            } catch (IOException e) {
                DeploymentLoader.logger.warning("Error scanning class loader for JSP tag libraries", e);
            }
        }

        try {
            urlSet = URLs.cullSystemJars(urlSet);
            urlSet = applyBuiltinExcludes(urlSet, Filters.tokens("openejb-jstl-1.2", "myfaces-impl", "javax.faces-2.", "spring-security-taglibs", "spring-webmvc"));
        } catch (IOException e) {
            DeploymentLoader.logger.warning("Error scanning class loader for JSP tag libraries", e);
        }

        return urlSet.getUrls();
    }

    private static int hash(final List<URL> urls) {
        int hash = 0;
        for (URL u : urls) {
            hash *= 31;
            if (u != null) {
                hash += u.toExternalForm().hashCode(); // url.hashCode() can be slow offline
            }
        }
        return hash;
    }
}
