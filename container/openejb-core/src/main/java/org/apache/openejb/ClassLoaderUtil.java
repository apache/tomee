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
package org.apache.openejb;

import org.apache.openejb.core.TempClassLoader;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.UrlCache;

import java.beans.Introspector;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.zip.ZipFile;

/**
 * @version $Revision$ $Date$
 */
public class ClassLoaderUtil {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, ClassLoaderUtil.class);
    private static final Map<String, List<ClassLoader>> classLoadersByApp = new HashMap<String, List<ClassLoader>>();
    private static final Map<ClassLoader, Set<String>> appsByClassLoader = new HashMap<ClassLoader, Set<String>>();
    private static final UrlCache localUrlCache = new UrlCache();

    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    public static File getUrlCachedName(String appId, URL url) {
        return localUrlCache.getUrlCachedName(appId, url);
    }

    public static boolean isUrlCached(String appId, URL url) {
        return localUrlCache.isUrlCached(appId, url);
    }

    public static URLClassLoader createClassLoader(String appId, URL[] urls, ClassLoader parent) {
        urls = localUrlCache.cacheUrls(appId, urls);
        URLClassLoader classLoader = new URLClassLoader(urls, parent);

        List<ClassLoader> classLoaders = classLoadersByApp.get(appId);
        if (classLoaders == null) {
            classLoaders = new ArrayList<ClassLoader>(2);
            classLoadersByApp.put(appId, classLoaders);
        }
        classLoaders.add(classLoader);

        Set<String> apps = appsByClassLoader.get(classLoader);
        if (apps == null) {
            apps = new LinkedHashSet<String>(1);
            appsByClassLoader.put(classLoader, apps);
        }
        apps.add(appId);

        return classLoader;
    }

    /**
     * Destroy a classloader as forcefully as possible.
     *
     * @param classLoader ClassLoader to destroy.
     */
    public static void destroyClassLoader(ClassLoader classLoader) {
        logger.debug("Destroying classLoader " + toString(classLoader));

        // remove from the indexes
        Set<String> apps = appsByClassLoader.remove(classLoader);

        if (apps != null) {

            List<ClassLoader> classLoaders;

            for (String appId : apps) {

                classLoaders = classLoadersByApp.get(appId);

                if (classLoaders != null) {
                    classLoaders.remove(classLoader);
                }

                //If this is the last class loader in the app, clean up the app
                if (null == classLoaders || classLoaders.isEmpty()) {
                    destroyClassLoader(appId);
                }
            }
        }

        // Clear OpenJPA caches
        cleanOpenJPACache(classLoader);

        //Clear open jar files belonging to this ClassLoader
        for (final String jar : getClosedJarFiles(classLoader)) {
            clearSunJarFileFactoryCache(jar);
        }

        classLoader = null;
    }

    /**
     * Dirty hack to force closure of file handles in the Oracle VM URLClassLoader
     * Any URLClassLoader passed into this method will be unusable after the method completes.
     *
     * @param cl ClassLoader of expected type URLClassLoader (Silent failure)
     */
    private static List<String> getClosedJarFiles(final ClassLoader cl) {

        final List<String> files = new ArrayList<String>();

        if (null != cl && cl instanceof URLClassLoader) {

            final URLClassLoader ucl = (URLClassLoader) cl;
            Class clazz = java.net.URLClassLoader.class;

            try {

                java.lang.reflect.Field ucp = clazz.getDeclaredField("ucp");
                ucp.setAccessible(true);
                Object cp = ucp.get(ucl);
                java.lang.reflect.Field loaders = cp.getClass().getDeclaredField("loaders");
                loaders.setAccessible(true);
                java.util.Collection c = (java.util.Collection) loaders.get(cp);
                java.lang.reflect.Field loader;
                java.util.jar.JarFile jf;

                for (final Object jl : c.toArray()) {
                    try {
                        loader = jl.getClass().getDeclaredField("jar");
                        loader.setAccessible(true);
                        jf = (java.util.jar.JarFile) loader.get(jl);
                        files.add(jf.getName());
                        jf.close();
                    } catch (Throwable t) {
                        //If we got this far, this is probably not a JAR loader so skip it
                    }
                }
            } catch (Throwable t) {
                //Not an Oracle VM
            }
        }

        return files;
    }

    public boolean finalizeNativeLibs(ClassLoader cl) {

        boolean res = false;
        Class classClassLoader = ClassLoader.class;
        java.lang.reflect.Field nativeLibraries = null;

        try {
            nativeLibraries = classClassLoader.getDeclaredField("nativeLibraries");
        } catch (NoSuchFieldException e1) {
            //Ignore
        }

        if (nativeLibraries == null) {
            return res;
        }

        nativeLibraries.setAccessible(true);
        Object obj = null;

        try {
            obj = nativeLibraries.get(cl);
        } catch (IllegalAccessException e1) {
            //Ignore
        }

        if (!(obj instanceof Vector)) {
            return res;
        }

        res = true;
        Vector java_lang_ClassLoader_NativeLibrary = (Vector) obj;
        java.lang.reflect.Method finalize;

        for (final Object lib : java_lang_ClassLoader_NativeLibrary) {

            finalize = null;

            try {
                finalize = lib.getClass().getDeclaredMethod("finalize", new Class[0]);

                if (finalize != null) {

                    finalize.setAccessible(true);

                    try {
                        finalize.invoke(lib, new Object[0]);
                    } catch (Throwable e) {
                        //Ignore
                    }
                }
            } catch (Throwable e) {
                //Ignore
            }
        }
        return res;
    }

    public static void destroyClassLoader(String appId) {

        logger.debug("Destroying classLoaders for application " + appId);
        List<ClassLoader> classLoaders = classLoadersByApp.remove(appId);

        if (classLoaders != null) {

            final Iterator<ClassLoader> it = classLoaders.iterator();
            Set<String> apps;
            ClassLoader cl;

            while (it.hasNext()) {

                cl = it.next();
                apps = appsByClassLoader.get(cl);

                if (null != apps) {
                    //This app is no longer using the class loader
                    apps.remove(appId);
                }

                //If no apps are using the class loader, destroy it
                if (null == apps || apps.isEmpty()) {
                    it.remove();
                    appsByClassLoader.remove(cl);
                    destroyClassLoader(cl);
                    cl = null;

                    System.gc();
                } else {
                    logger.debug("ClassLoader " + toString(cl) + " held open by the applications: " + apps);
                }
            }
        }

        localUrlCache.releaseUrls(appId);
        clearSunJarFileFactoryCache(appId);
    }

    public static URLClassLoader createTempClassLoader(ClassLoader parent) {
        return new TempClassLoader(parent);
    }

    public static URLClassLoader createTempClassLoader(String appId, URL[] urls, ClassLoader parent) {
        URLClassLoader classLoader = createClassLoader(appId, urls, parent);
        return new TempClassLoader(classLoader);
    }

    /**
     * Cleans well known class loader leaks in VMs and libraries.  There is a lot of bad code out there and this method
     * will clear up the know problems.  This method should only be called when the class loader will no longer be used.
     * It this method is called two often it can have a serious impact on preformance.
     */
    public static void clearClassLoaderCaches() {
        clearSunSoftCache(ObjectInputStream.class, "subclassAudits");
        clearSunSoftCache(ObjectOutputStream.class, "subclassAudits");
        clearSunSoftCache(ObjectStreamClass.class, "localDescs");
        clearSunSoftCache(ObjectStreamClass.class, "reflectors");
        Introspector.flushCaches();
    }

    public static void clearSunJarFileFactoryCache(final String jarLocation) {
        clearSunJarFileFactoryCacheImpl(jarLocation, 5);
    }

    /**
     * Due to several different implementation changes in various JDK releases the code here is not as
     * straight forward as reflecting debug items in your current runtime. There have even been breaking changes
     * between 1.6 runtime builds, let alone 1.5.
     * <p/>
     * If you discover a new issue here please be careful to ensure the existing functionality is 'extended' and not
     * just replaced to match your runtime observations.
     * <p/>
     * If you want to look at the mess that leads up to this then follow the source code changes made to
     * the class sun.net.www.protocol.jar.JarFileFactory over several years.
     *
     * @param jarLocation String
     * @param attempt     int
     */
    @SuppressWarnings({"unchecked"})
    private static synchronized void clearSunJarFileFactoryCacheImpl(final String jarLocation, final int attempt) {
        logger.debug("Clearing Sun JarFileFactory cache for directory " + jarLocation);

        try {
            final Class jarFileFactory = Class.forName("sun.net.www.protocol.jar.JarFileFactory");

            //Do not generify these maps as their contents are NOT stable across runtimes.
            final Field fileCacheField = jarFileFactory.getDeclaredField("fileCache");
            fileCacheField.setAccessible(true);
            final Map fileCache = (Map) fileCacheField.get(null);
            final Map fileCacheCopy = new HashMap(fileCache);

            final Field urlCacheField = jarFileFactory.getDeclaredField("urlCache");
            urlCacheField.setAccessible(true);
            final Map urlCache = (Map) urlCacheField.get(null);
            final Map urlCacheCopy = new HashMap(urlCache);

            //The only stable item we have here is the JarFile/ZipFile in this map
            Iterator iterator = urlCacheCopy.entrySet().iterator();
            final List urlCacheRemoveKeys = new ArrayList();

            while (iterator.hasNext()) {
                final Map.Entry entry = (Map.Entry) iterator.next();
                final Object key = entry.getKey();

                if (key instanceof ZipFile) {
                    final ZipFile zf = (ZipFile) key;
                    final File file = new File(zf.getName());  //getName returns File.getPath()
                    if (isParent(jarLocation, file)) {
                        //Flag for removal
                        urlCacheRemoveKeys.add(key);
                    }
                } else {
                    logger.warning("Unexpected key type: " + key);
                }
            }

            iterator = fileCacheCopy.entrySet().iterator();
            final List fileCacheRemoveKeys = new ArrayList();

            while (iterator.hasNext()) {
                final Map.Entry entry = (Map.Entry) iterator.next();
                final Object value = entry.getValue();

                if (urlCacheRemoveKeys.contains(value)) {
                    fileCacheRemoveKeys.add(entry.getKey());
                }
            }

            //Use these unstable values as the keys for the fileCache values.
            iterator = fileCacheRemoveKeys.iterator();
            while (iterator.hasNext()) {

                final Object next = iterator.next();

                try {
                    final Object remove = fileCache.remove(next);
                    if (null != remove) {
                        logger.debug("Removed item from fileCache: " + remove);
                    }
                } catch (Throwable e) {
                    logger.warning("Failed to remove item from fileCache: " + next);
                }
            }

            iterator = urlCacheRemoveKeys.iterator();
            while (iterator.hasNext()) {

                final Object next = iterator.next();

                try {
                    final Object remove = urlCache.remove(next);

                    try {
                        ((ZipFile) next).close();
                    } catch (Throwable e) {
                        //Ignore
                    }

                    if (null != remove) {
                        logger.debug("Removed item from urlCache: " + remove);
                    }
                } catch (Throwable e) {
                    logger.warning("Failed to remove item from urlCache: " + next);
                }

            }

        } catch (ConcurrentModificationException e) {
            if (attempt > 0) {
                clearSunJarFileFactoryCacheImpl(jarLocation, (attempt - 1));
            } else {
                logger.error("Unable to clear Sun JarFileFactory cache after 5 attempts", e);
            }
        } catch (ClassNotFoundException e) {
            // not a sun vm
        } catch (NoSuchFieldException e) {
            // different version of sun vm?
        } catch (Throwable e) {
            logger.error("Unable to clear Sun JarFileFactory cache", e);
        }
    }

    private static boolean isParent(String jarLocation, File file) {
        File dir = new File(jarLocation);
        while (file != null) {
            if (file.equals(dir)) {
                return true;
            }
            file = file.getParentFile();
        }
        return false;
    }

    /**
     * Clears the caches maintained by the SunVM object stream implementation.
     * This method uses reflection and setAccessable to obtain access to the Sun cache.
     * The cache Class synchronizes upon itself for access to the cache Map.
     * This method completely clears the class loader cache which will impact preformance of object serialization.
     *
     * @param clazz     the name of the class containing the cache field
     * @param fieldName the name of the cache field
     */
    public static void clearSunSoftCache(final Class clazz, String fieldName) {
        synchronized (clazz) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                final Map cache = (Map) field.get(null);
                cache.clear();
            } catch (Throwable ignored) {
                // there is nothing a user could do about this anyway
            }
        }
    }

    public static void cleanOpenJPACache(ClassLoader classLoader) {
        try {
            Class<?> pcRegistryClass = ClassLoaderUtil.class.getClassLoader().loadClass("org.apache.openjpa.enhance.PCRegistry");
            Method deRegisterMethod = pcRegistryClass.getMethod("deRegister", ClassLoader.class);
            deRegisterMethod.invoke(null, classLoader);
        } catch (Throwable ignored) {
            // there is nothing a user could do about this anyway
        }
    }

    private static String toString(ClassLoader classLoader) {
        if (classLoader == null) {
            return "null";
        } else {
            return classLoader.getClass().getSimpleName() + "@" + System.identityHashCode(classLoader);
        }
    }
}
