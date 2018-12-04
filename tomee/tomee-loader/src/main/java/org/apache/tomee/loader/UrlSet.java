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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.loader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tomee.loader.filter.Filter;
import static org.apache.tomee.loader.filter.Filters.invert;
import static org.apache.tomee.loader.filter.Filters.patterns;

/**
 * @version $Rev$ $Date$
 */
public class UrlSet implements Iterable<URL> {

    private final Map<String,URL> urls;

    public UrlSet(final ClassLoader classLoader) throws IOException {
        this(getUrls(classLoader));
    }

    public UrlSet(final URL... urls){
        this(Arrays.asList(urls));
    }
    /**
     * Ignores all URLs that are not "jar" or "file"
     * @param urls
     */
    public UrlSet(final Collection<URL> urls){
        this.urls = new HashMap<>();
        for (final URL location : urls) {
            try {
//                if (location.getProtocol().equals("file")) {
//                    try {
//                        // See if it's actually a jar
//                        URL jarUrl = new URL("jar", "", location.toExternalForm() + "!/");
//                        JarURLConnection juc = (JarURLConnection) jarUrl.openConnection();
//                        juc.getJarFile();
//                        location = jarUrl;
//                    } catch (IOException e) {
//                    }
//                    this.urls.put(location.toExternalForm(), location);
//                }
                this.urls.put(location.toExternalForm(), location);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private UrlSet(final Map<String, URL> urls) {
        this.urls = urls;
    }

    public UrlSet include(final UrlSet urlSet){
        final Map<String, URL> urls = new HashMap<>(this.urls);
        urls.putAll(urlSet.urls);
        return new UrlSet(urls);
    }


    public UrlSet include(final URL url){
        final Map<String, URL> urls = new HashMap<>(this.urls);
        urls.put(url.toExternalForm(), url);
        return new UrlSet(urls);
    }

    public UrlSet exclude(final UrlSet urlSet) {
        final Map<String, URL> urls = new HashMap<>(this.urls);
        final Map<String, URL> parentUrls = urlSet.urls;
        for (final String url : parentUrls.keySet()) {
            urls.remove(url);
        }
        return new UrlSet(urls);
    }

    public UrlSet exclude(final URL url) {
        final Map<String, URL> urls = new HashMap<>(this.urls);
        urls.remove(url.toExternalForm());
        return new UrlSet(urls);
    }

    public UrlSet exclude(final ClassLoader parent) throws IOException {
        return exclude(new UrlSet(parent));
    }

    public UrlSet exclude(final File file) throws MalformedURLException {
        return exclude(relative(file));
    }

    public UrlSet exclude(final String pattern) throws MalformedURLException {
        return filter(invert(patterns(pattern)));
    }

    /**
     * Calls excludePaths(System.getProperty("java.ext.dirs"))
     * @return
     * @throws MalformedURLException
     */
    public UrlSet excludeJavaExtDirs() throws MalformedURLException {
        final String extDirs = System.getProperty("java.ext.dirs");
        return extDirs == null ? this : excludePaths(extDirs);
    }

    /**
     * Calls excludePaths(System.getProperty("java.endorsed.dirs"))
     *
     * @return
     * @throws MalformedURLException
     */
    public UrlSet excludeJavaEndorsedDirs() throws MalformedURLException {
        final String endorsedDirs = System.getProperty("java.endorsed.dirs");
        return endorsedDirs == null ? this : excludePaths(endorsedDirs);
    }

    public UrlSet excludeJavaHome() throws MalformedURLException {
        final String path = System.getProperty("java.home");

        File java = new File(path);

        if (path.matches("/System/Library/Frameworks/JavaVM.framework/Versions/[^/]+/Home")){
            java = java.getParentFile();
        }

        return exclude(java);
    }

    public UrlSet excludePaths(final String pathString) throws MalformedURLException {
        final String[] paths = pathString.split(File.pathSeparator);
        UrlSet urlSet = this;
        for (final String path : paths) {
            final File file = new File(path);
            urlSet = urlSet.exclude(file);
        }
        return urlSet;
    }

    public UrlSet filter(final Filter filter) {
        final Map<String, URL> urls = new HashMap<>();
        for (final Map.Entry<String, URL> entry : this.urls.entrySet()) {
            final String url = entry.getKey();
            if (filter.accept(url)){
                urls.put(url, entry.getValue());
            }
        }
        return new UrlSet(urls);
    }

    public UrlSet matching(final String pattern) {
        return filter(patterns(pattern));
    }

    public UrlSet relative(final File file) throws MalformedURLException {
        final String urlPath = file.toURI().toURL().toExternalForm();
        final Map<String, URL> urls = new HashMap<>();
        for (final Map.Entry<String, URL> entry : this.urls.entrySet()) {
            final String url = entry.getKey();
            if (url.startsWith(urlPath) || url.startsWith("jar:"+urlPath)){
                urls.put(url, entry.getValue());
            }
        }
        return new UrlSet(urls);
    }

    public List<URL> getUrls() {
        return new ArrayList<>(urls.values());
    }

    public int size() {
        return urls.size();
    }

    public Iterator<URL> iterator() {
        return getUrls().iterator();
    }

    private static List<URL> getUrls(final ClassLoader classLoader) throws IOException {
        final List<URL> list = new ArrayList<>();
        final ArrayList<URL> urls = Collections.list(classLoader.getResources("META-INF"));
        for (URL url : urls) {
            String externalForm = url.toExternalForm();
            final int i = externalForm.lastIndexOf("META-INF");
            externalForm = externalForm.substring(0, i);
            url = new URL(externalForm);
            list.add(url);
        }
        list.addAll(Collections.list(classLoader.getResources("")));
        return list;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + urls.size() + "]";
    }
}
