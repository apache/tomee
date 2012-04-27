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
package org.apache.openejb.util;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class UrlCache {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, UrlCache.class);
    public static final boolean antiJarLocking;
    public static final File cacheDir;

    static {        
        antiJarLocking = SystemInstance.get().getOptions().get("antiJarLocking", false);
     
        if (antiJarLocking) {
            cacheDir = createCacheDir();
            logger.info("AntiJarLocking enabled. Using URL cache dir " + cacheDir);
        } else {
            cacheDir = null;
        }
    }


    
    private final Map<String, Map<URL, File>> cache = new TreeMap<String, Map<URL, File>>();

    public synchronized URL[] cacheUrls(final String appId, final URL[] urls) {
        if (!antiJarLocking) {
            return urls;
        }

        // the final cached urls
        final LinkedHashSet<URL> cachedUrls = new LinkedHashSet<URL>();

        // this stack contains the urls to be processed... when manifest class path entries
        // are added they are added to the top (front) of the stack so manifest order is maintained
        final LinkedList<URL> locationStack = new LinkedList<URL>(Arrays.asList(urls));
        while (!locationStack.isEmpty()) {
            final URL url = locationStack.removeFirst();

            // Skip any duplicate urls in the claspath
            if (cachedUrls.contains(url)) {
                continue;
            }

            // cache the URL
            final File file = cacheUrl(appId, url);

            // if the url was successfully cached, process it's manifest classpath
            if (file != null) {
                try {
                    cachedUrls.add(file.toURI().toURL());

                    // push the manifest classpath on the stack (make sure to maintain the order)
                    final List<URL> manifestClassPath = getManifestClassPath(url, file);
                    locationStack.addAll(0, manifestClassPath);
                } catch (MalformedURLException e) {
                    // invalid cache file - this should never happen
                    logger.error("Error caching url. Original jar file will be used which may result in a file lock: url=" + url, e);
                    cachedUrls.add(url);
                }
            } else {
                // URL was not cached - simply pass through the url
                cachedUrls.add(url);
            }
        }

        return cachedUrls.toArray(new URL[cachedUrls.size()]);
    }

    public synchronized void releaseUrls(final String appId) {
        logger.debug("Releasing URLs for application " + appId);

        final Map<URL, File> urlFileMap = cache.remove(appId);
        if (urlFileMap != null) {
            for (final File file : urlFileMap.values()) {
                if (file.delete()) {
                    logger.debug("Deleted cached file " + file);
                } else {
                    logger.debug("Unable to delete cached file " + file);
                }
            }
        }
    }

    public File getUrlCachedName(final String appId, final URL url) {
        final Map<URL, File> appCache = getAppCache(appId);
        if (appCache.containsKey(url)) {
            return appCache.get(url);
        }
        return null;
    }

    public boolean isUrlCached(final String appId, final URL url) {
        final Map<URL, File> appCache = getAppCache(appId);
        return appCache.containsKey(url);
    }

    public URL getUrlKeyCached(final String appId, final File file) {
    	if (file == null) {
    		return null;
    	}
        final Map<URL, File> appCache = getAppCache(appId);
        for (final Map.Entry<URL, File> entry : appCache.entrySet()) {
        	if (entry.getValue().equals(file)) {
        		return entry.getKey();
        	}
        }

        final URL keyUrl;
		try {
			keyUrl = file.toURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
        if (appCache.containsKey(keyUrl)) {
        	return keyUrl;
        }
        return null;
    }

    private synchronized File cacheUrl(final String appId, URL url) {
        File sourceFile;
        if (!"file".equals(url.getProtocol())) {
            // todo: download the jar ourselves?
            // for now return null which means we did not cache
            return null;
        } else {
            // verify file
            sourceFile = URLs.toFile(url);
            if (!sourceFile.exists()) {
                return null;
            }
            if (!sourceFile.canRead()) {
                return null;
            }

            // if file is a directory, there is no need to cache
            if (sourceFile.isDirectory()) {
                return sourceFile;
            }

            // Create absolute file URL
            sourceFile = sourceFile.getAbsoluteFile();
            try {
                url = sourceFile.toURI().toURL();
            } catch (MalformedURLException ignored) {
            }
        }

        // check if file is already cached
        final Map<URL, File> appCache = getAppCache(appId);
        if (appCache.containsKey(url)) {
            return appCache.get(url);
        }

        // if the file is already in the cache, don't recopy it to the cache dir
        if (sourceFile.getParentFile().equals(cacheDir)) {
            // mark it as part of the application, so it cleaned up when the application is undeployed
            appCache.put(url, sourceFile);
            return sourceFile;
        }

        // generate a nice cache file name
        final String name = sourceFile.getName();
        final int dot = name.lastIndexOf(".");
        String prefix = name;
        String suffix = "";
        if (dot > 0) {
            prefix = name.substring(0, dot) + "-";
            suffix = name.substring(dot, name.length());
        }

        // copy the file to the cache dir to avoid file locks
        File cacheFile = null;
        boolean success;
        try {
            cacheFile = File.createTempFile(prefix, suffix, cacheDir);
            cacheFile.deleteOnExit();
            success = JarExtractor.copyRecursively(sourceFile, cacheFile);
        } catch (IOException e) {
            success = false;
        }

        if (success) {
            // add cache file to cache
            appCache.put(url, cacheFile);
            logger.debug("Coppied jar file to " + cacheFile);
            return cacheFile;
        } else {
            // clean up failed copy
            JarExtractor.delete(cacheFile);
            logger.error("Unable to copy jar into URL cache directory. Original jar file will be used which may result in a file lock: file=" + sourceFile);
            return null;
        }
    }

    private synchronized Map<URL, File> getAppCache(final String appId) {
        Map<URL, File> urlFileMap = cache.get(appId);
        if (urlFileMap == null) {
            urlFileMap = new LinkedHashMap<URL, File>();
            cache.put(appId, urlFileMap);
        }
        return urlFileMap;
    }

    private List<URL> getManifestClassPath(final URL codeSource, final File location) {
        try {
            // get the manifest, if possible
            final Manifest manifest = loadManifest(location);
            if (manifest == null) {
                // some locations don't have a manifest
                return Collections.emptyList();
            }

            // get the class-path attribute, if possible
            final String manifestClassPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
            if (manifestClassPath == null) {
                return Collections.emptyList();
            }

            // build the urls...
            // the class-path attribute is space delimited
            final LinkedList<URL> classPathUrls = new LinkedList<URL>();
            for (StringTokenizer tokenizer = new StringTokenizer(manifestClassPath, " "); tokenizer.hasMoreTokens();) {
                final String entry = tokenizer.nextToken();
                try {
                    // the class path entry is relative to the resource location code source
                    final URL entryUrl = new URL(codeSource, entry);
                    classPathUrls.addLast(entryUrl);
                } catch (MalformedURLException ignored) {
                    // most likely a poorly named entry
                }
            }
            return classPathUrls;
        } catch (IOException ignored) {
            // error opening the manifest
            return Collections.emptyList();
        }
    }

    private Manifest loadManifest(final File location) throws IOException {
        if (location.isDirectory()) {
            final File manifestFile = new File(location, "META-INF/MANIFEST.MF");

            if (manifestFile.isFile() && manifestFile.canRead()) {
                InputStream in = null;
                try {
                    in = IO.read(manifestFile);
                    final Manifest manifest = new Manifest(in);
                    return manifest;
                } finally {
                    close(in);
                }
            }
        } else {
            final JarFile jarFile = new JarFile(location);
            try {
                final Manifest manifest = jarFile.getManifest();
                return manifest;
            } finally {
                close(jarFile);
            }
        }
        return null;
    }

    private static File createCacheDir() {
        try {
            final FileUtils openejbBase = SystemInstance.get().getBase();

            File dir = null;
            // if we are not embedded, cache (temp) dir is under base dir
            if (openejbBase.getDirectory("conf").exists()) {
                try {
                    dir = openejbBase.getDirectory("temp");
                } catch (IOException e) {
                    //Ignore
                }
            }

            // if we are embedded, tmp dir is in the system tmp dir
            if (dir == null) {                
                dir = Files.tmpdir();
            }

            // If the cache dir already exists then empty its contents
            if (dir.exists()) {
                final File[] files = dir.listFiles();
                if (null != files) {
                    for (final File f : files) {
                        deleteDir(f);
                    }
                }
            } else {
                dir = createCacheDir(new File(dir.getAbsolutePath()));
            }

            return dir;

        } catch (IOException e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    private static File createCacheDir(final File dir) throws IOException {
        
        if(dir.exists() && dir.isDirectory()){
            return dir;
        }
        
        if (dir.exists() && !dir.isDirectory()) {
            throw new IOException("Cache temp directory held by file: " + dir);
        }
        
        if (!dir.mkdirs()) {
            throw new IOException("Unable to create cache temp directory: " + dir);
        }
        
        Thread.yield();

        return dir;
    }

    /**
     * Delete the specified directory, including all of its contents and
     * subdirectories recursively.
     *
     * @param dir File object representing the directory to be deleted
     */
    public static void deleteDir(final File dir) {
        if (dir == null) {
            return;
        }

        final File[] fileNames = dir.listFiles();
        if (fileNames != null) {
            for (final File file : fileNames) {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    if (file.delete()) {
                        logger.debug("Deleted file " + file);
                    } else {
                        logger.debug("Unable to delete file " + file);
                    }

                }
            }
        }
        if (dir.delete()) {
            logger.debug("Deleted file " + dir);
        } else {
            logger.debug("Unable to delete file " + dir);
        }
    }

    private static void close(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void close(final JarFile closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }
}
