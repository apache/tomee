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

import org.apache.openejb.classloader.ClassLoaderConfigurer;
import org.apache.openejb.classloader.CompositeClassLoaderConfigurer;
import org.apache.openejb.config.QuickJarsTxtParser;
import org.apache.openejb.core.TempClassLoader;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.UrlCache;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.apache.xbean.recipe.ObjectRecipe;

import java.beans.Introspector;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

/**
 * @version $Revision$ $Date$
 */
public class ClassLoaderUtil {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, ClassLoaderUtil.class);
    private static final Map<String, List<ClassLoader>> classLoadersByApp = new HashMap<String, List<ClassLoader>>();
    private static final Map<ClassLoader, Set<String>> appsByClassLoader = new HashMap<ClassLoader, Set<String>>();
    private static final UrlCache localUrlCache = new UrlCache();
    private static final AtomicBoolean skipClearSunJarFile = new AtomicBoolean();

    public static void destroyClassLoader(final String appId, final String appPath) {
        destroyClassLoader(appId);
        destroyClassLoader(appPath);
    }

    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    public static File getUrlCachedName(final String appId, final URL url) {
        return localUrlCache.getUrlCachedName(appId, url);
    }

    public static boolean isUrlCached(final String appId, final URL url) {
        return localUrlCache.isUrlCached(appId, url);
    }

    public static URL getUrlKeyCached(final String appId, final File file) {
        return localUrlCache.getUrlKeyCached(appId, file);
    }

    public static URLClassLoader createClassLoaderFirst(final String appId, final URL[] urls, final ClassLoader parent) {
        return cacheClassLoader(appId, new URLClassLoaderFirst(localUrlCache.cacheUrls(appId, urls), parent));
    }

    public static URLClassLoader createClassLoader(final String appId, final URL[] urls, final ClassLoader parent) {
        return cacheClassLoader(appId, new URLClassLoader(localUrlCache.cacheUrls(appId, urls), parent));
    }

    private static URLClassLoader cacheClassLoader(final String appId, final URLClassLoader classLoader) {
        List<ClassLoader> classLoaders = classLoadersByApp.get(appId);
        if (classLoaders == null) {
            classLoaders = new ArrayList<>(2);
            classLoadersByApp.put(appId, classLoaders);
        }
        classLoaders.add(classLoader);

        Set<String> apps = appsByClassLoader.get(classLoader);
        if (apps == null) {
            apps = new LinkedHashSet<>(1);
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
    public static void destroyClassLoader(final ClassLoader classLoader) {

        // remove from the indexes
        final Set<String> apps = appsByClassLoader.remove(classLoader);

        logger.debug("Destroying classLoader '" + toString(classLoader) + "' for apps: " + apps);

        if (apps != null) {

            List<ClassLoader> classLoaders;

            for (final String appId : apps) {

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

        if (Closeable.class.isInstance(classLoader)) {
            try {
                Closeable.class.cast(classLoader).close();
            } catch (final IOException e) {
                // no-op
            }
        }
    }

    /**
     * Dirty hack to force closure of file handles in the Oracle VM URLClassLoader
     * Any URLClassLoader passed into this method will be unusable after the method completes.
     *
     * @param cl ClassLoader of expected type URLClassLoader (Silent failure)
     */
    private static List<String> getClosedJarFiles(final ClassLoader cl) {

        final List<String> files = new ArrayList<>();

        if (null != cl && cl instanceof URLClassLoader) {

            final URLClassLoader ucl = (URLClassLoader) cl;
            final Class clazz = URLClassLoader.class;

            try {

                final Field ucp = clazz.getDeclaredField("ucp");
                ucp.setAccessible(true);
                final Object cp = ucp.get(ucl);
                final Field loaders = cp.getClass().getDeclaredField("loaders");
                loaders.setAccessible(true);
                final Collection c = (Collection) loaders.get(cp);
                Field loader;
                JarFile jf;

                for (final Object jl : c.toArray()) {
                    try {
                        loader = jl.getClass().getDeclaredField("jar");
                        loader.setAccessible(true);
                        jf = (JarFile) loader.get(jl);
                        files.add(jf.getName());
                        jf.close();
                    } catch (final Throwable t) {
                        //If we got this far, this is probably not a JAR loader so skip it
                    }
                }
            } catch (final Throwable t) {
                //Not an Oracle VM
            }
        }

        return files;
    }

    @SuppressWarnings({"UseOfObsoleteCollectionType", "PMD.AvoidCallingFinalize"})
    public boolean finalizeNativeLibs(final ClassLoader cl) {
        final Class classClassLoader = ClassLoader.class;
        Field nativeLibraries = null;

        try {
            nativeLibraries = classClassLoader.getDeclaredField("nativeLibraries");
        } catch (final NoSuchFieldException e1) {
            //Ignore
        }

        if (nativeLibraries == null) {
            return false;
        }

        nativeLibraries.setAccessible(true);
        Object obj = null;

        try {
            obj = nativeLibraries.get(cl);
        } catch (final IllegalAccessException e1) {
            //Ignore
        }

        if (!(obj instanceof Vector)) {
            return false;
        }

        final Vector javaLangClassLoaderNativeLibrary = (Vector) obj;
        Method finalize;

        for (final Object lib : javaLangClassLoaderNativeLibrary) {

            try {
                finalize = lib.getClass().getDeclaredMethod("finalize", new Class[0]);

                if (finalize != null) {

                    finalize.setAccessible(true);

                    try {
                        finalize.invoke(lib);
                    } catch (final Throwable e) {
                        //Ignore
                    }
                }
            } catch (final Throwable e) {
                //Ignore
            }
        }
        return true;
    }

    public static void destroyClassLoader(final String appId) {

        logger.debug("Destroying classLoaders for application " + appId);
        final List<ClassLoader> classLoaders = classLoadersByApp.remove(appId);

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
                    System.gc(); //NOPMD
                } else {
                    logger.debug("ClassLoader " + toString(cl) + " held open by the applications: " + apps);
                }
            }
        }

        localUrlCache.releaseUrls(appId);
        clearSunJarFileFactoryCache(appId);
    }

    public static URLClassLoader createTempClassLoader(final ClassLoader parent) {
        return new TempClassLoader(parent);
    }

    public static URLClassLoader createTempClassLoader(final String appId, final URL[] rawUrls, final ClassLoader parent) {
        String updatedAppId = appId;
        if (appId != null) { // here we often get the full path of the app as id where later it is simply the name of the file/dir
            final File file = new File(appId);
            if (file.exists()) {
                updatedAppId = file.getName();
                if (updatedAppId.endsWith(".war") || updatedAppId.endsWith(".ear")) {
                    updatedAppId = updatedAppId.substring(0, updatedAppId.length() - ".war".length());
                }
            }
        }

        // from the app
        final ClassLoaderConfigurer configurer1 = QuickJarsTxtParser.parse(new File(appId, "META-INF/" + QuickJarsTxtParser.FILE_NAME));
        final ClassLoaderConfigurer configurer2 = QuickJarsTxtParser.parse(new File(appId, "WEB-INF/" + QuickJarsTxtParser.FILE_NAME));

        // external config
        ClassLoaderConfigurer configurer3 = ClassLoaderUtil.configurer(updatedAppId);
        if (configurer3 == null) { // try the complete path
            configurer3 = ClassLoaderUtil.configurer(appId);
        }

        final URL[] urls;
        if (configurer1 == null && configurer2 == null && configurer3 == null) {
            urls = rawUrls;
        } else {
            final CompositeClassLoaderConfigurer configurer = new CompositeClassLoaderConfigurer(configurer1, configurer2, configurer3);
            final Collection<URL> list = new ArrayList<>(Arrays.asList(rawUrls));
            ClassLoaderConfigurer.Helper.configure(list, configurer);
            urls = list.toArray(new URL[list.size()]);
        }

        return new TempClassLoader(createClassLoader(appId, urls, parent));
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
        if (skipClearSunJarFile.get()) {
            return;
        }
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
                } catch (final Throwable e) {
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
                    } catch (final Throwable e) {
                        //Ignore
                    }

                    if (null != remove) {
                        logger.debug("Removed item from urlCache: " + remove);
                    }
                } catch (final Throwable e) {
                    logger.warning("Failed to remove item from urlCache: " + next);
                }

            }
        } catch (final ConcurrentModificationException e) {
            if (attempt > 0) {
                clearSunJarFileFactoryCacheImpl(jarLocation, attempt - 1);
            } else {
                logger.error("Unable to clear Sun JarFileFactory cache after 5 attempts", e);
            }
        } catch (final ClassNotFoundException | NoSuchFieldException e) {
            // not a sun vm
        } catch (final RuntimeException re) {
            if ("java.lang.reflect.InaccessibleObjectException".equals(re.getClass().getName())) {
                skipClearSunJarFile.compareAndSet(false, true);
                return;
            }
            throw re;
        } catch (final Throwable e) {
            if (Boolean.getBoolean("openejb.java9.hack")) {
                return; // reflection fails cause internals are not exported, close() is called and should be fine
            }
            logger.error("Unable to clear Sun JarFileFactory cache", e);
        }
    }

    private static boolean isParent(final String jarLocation, File file) {
        final File dir = new File(jarLocation);
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
    public static void clearSunSoftCache(final Class clazz, final String fieldName) {

        try {
            final Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            final Map cache = (Map) field.get(null);
            cache.clear();
        } catch (final Throwable ignored) {
            // there is nothing a user could do about this anyway
        }
    }

    public static void cleanOpenJPACache(final ClassLoader classLoader) {
        if (classLoader != ClassLoader.getSystemClassLoader()) {
            try {
                final Class<?> pcRegistryClass = ClassLoaderUtil.class.getClassLoader().loadClass("org.apache.openjpa.enhance.PCRegistry");
                final Method deRegisterMethod = pcRegistryClass.getMethod("deRegister", ClassLoader.class);
                deRegisterMethod.invoke(null, classLoader);
            } catch (final Throwable ignored) {
                // there is nothing a user could do about this anyway
            }
        } // else keep it since OpenJPA uses static block to init meta we can't clean it for each app
    }

    private static String toString(final ClassLoader classLoader) {
        if (classLoader == null) {
            return "null";
        } else {
            return classLoader.getClass().getSimpleName() + "@" + System.identityHashCode(classLoader);
        }
    }

    public static String resourceName(final String s) {
        return s.replace(".", "/") + ".class";
    }

    public static ClassLoaderConfigurer configurer(final String rawId) {
        String id = rawId;
        if (id != null && (id.startsWith("/") || id.startsWith("\\")) && !new File(id).exists() && id.length() > 1) {
            id = id.substring(1);
        }
        if (id == null) {
            id = "";
        }

        // TODO: see how to manage tomee/openejb prefix
        String key = "tomee.classloader.configurer." + id + ".clazz";
        String impl = SystemInstance.get().getProperty(key);
        if (impl == null) {
            key = "tomee.classloader.configurer.clazz";
            impl = SystemInstance.get().getProperty(key);
            if (impl == null) {
                key = "openejb.classloader.configurer." + id + ".clazz";
                impl = SystemInstance.get().getProperty(key);
                if (impl == null) {
                    key = "openejb.classloader.configurer.clazz";
                    impl = SystemInstance.get().getProperty(key);
                }

            }
        }

        if (impl != null) {
            key = key.substring(0, key.length() - "clazz".length());

            boolean list = false;
            try {
                ClassLoaderUtil.class.getClassLoader().loadClass(impl);
            } catch (final ClassNotFoundException e) {
                list = true;
            }

            if (!list) {
                return createConfigurer(key, impl);
            } else {
                final String[] names = impl.split(",");
                final ClassLoaderConfigurer[] configurers = new ClassLoaderConfigurer[names.length];
                for (int i = 0; i < names.length; i++) {
                    configurers[i] = createConfigurer(names[i], SystemInstance.get().getProperty(names[i] + ".clazz"));
                }
                return new CompositeClassLoaderConfigurer(configurers);
            }
        }
        return null;
    }

    private static ClassLoaderConfigurer createConfigurer(final String key, final String impl) {
        try {
            final ObjectRecipe recipe = new ObjectRecipe(impl);
            for (final Map.Entry<Object, Object> entry : SystemInstance.get().getProperties().entrySet()) {
                final String entryKey = entry.getKey().toString();
                if (entryKey.startsWith(key)) {
                    final String newKey = entryKey.substring(key.length());
                    if (!"clazz".equals(newKey)) {
                        recipe.setProperty(newKey, entry.getValue());
                    }
                }
            }

            final Object instance = recipe.create();
            if (instance instanceof ClassLoaderConfigurer) {
                return (ClassLoaderConfigurer) instance;
            } else {
                logger.error(impl + " is not a classlaoder configurer, using default behavior");
            }
        } catch (final Exception e) {
            logger.error("Can't create classloader configurer " + impl + ", using default behavior");
        }
        return null;
    }
}
