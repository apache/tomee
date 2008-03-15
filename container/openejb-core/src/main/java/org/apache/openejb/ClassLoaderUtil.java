/**
 *
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
package org.apache.openejb;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.jar.JarFile;

import org.apache.openejb.core.TempClassLoader;
import org.apache.openejb.core.AntiJarLockingClassLoader;
import org.apache.openejb.core.TempAntiJarLockingClassLoader;
import org.apache.openejb.util.URLs;
import org.apache.xbean.classloader.JarFileClassLoader;

/**
 * @version $Revision$ $Date$
 */
public class ClassLoaderUtil {
    private static final Map<String,List<ClassLoader>> classLoadersByApp = new HashMap<String,List<ClassLoader>>();
    private static final Map<ClassLoader,List<String>> appsByClassLoader = new HashMap<ClassLoader,List<String>>();

    private static boolean isAntiJarLocking() {
        return Boolean.getBoolean("antiJarLocking");
    }

    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    public static URLClassLoader createClassLoader(String appId, URL[] urls, ClassLoader parent) {
        URLClassLoader classLoader;
        if (isAntiJarLocking()) {
            classLoader = new AntiJarLockingClassLoader(appId, urls, parent);
        } else {
            classLoader = new URLClassLoader(urls, parent);
        }

        List<ClassLoader> classLoaders = classLoadersByApp.get(appId);
        if (classLoaders == null) {
            classLoaders = new ArrayList<ClassLoader>(2);
            classLoadersByApp.put(appId, classLoaders);
        }
        classLoaders.add(classLoader);

        List<String> apps = appsByClassLoader.get(classLoader);
        if (apps == null) {
            apps = new ArrayList<String>(1);
            appsByClassLoader.put(classLoader, apps);
        }
        apps.add(appId);

        return classLoader;
    }

    public static void destroyClassLoader(ClassLoader classLoader) {
        // remove from the indexes
        List<String> apps = appsByClassLoader.remove(classLoader);
        if (apps != null) {
            for (String app : apps) {
                List<ClassLoader> classLoaders = classLoadersByApp.get(app);
                if (classLoaders != null) {
                    classLoaders.remove(classLoader);
                }
            }
        }

        // clear the lame openjpa caches
        cleanOpenJPACache(classLoader);

        // destroy the class loader
        if (classLoader instanceof JarFileClassLoader) {
            JarFileClassLoader jarFileClassLoader = (JarFileClassLoader) classLoader;
            jarFileClassLoader.destroy();
        }

        // clear the sun caches
        clearClassLoaderCaches();
    }

    public static void destroyClassLoader(String appId) {
        List<ClassLoader> classLoaders = classLoadersByApp.remove(appId);
        if (classLoaders != null) {
            for (ClassLoader classLoader : classLoaders) {
                // get the apps using the class loader
                List<String> apps = appsByClassLoader.get(classLoader);
                if (apps == null) apps = Collections.emptyList();

                // this app is no longer using the class loader
                apps.remove(appId);

                // if no apps are using the class loader, destroy it
                if (apps.isEmpty()) {
                    appsByClassLoader.remove(classLoader);
                    destroyClassLoader(classLoader);
                }
            }
        }
    }

    public static URLClassLoader createTempClassLoader(ClassLoader parent) {
        if (isAntiJarLocking()) {
            return new TempAntiJarLockingClassLoader(parent);
        } else {
            return new TempClassLoader(parent);
        }
    }

    public static URLClassLoader createTempClassLoader(URL[] urls) {
        if (isAntiJarLocking()) {
            return new TempAntiJarLockingClassLoader(urls);
        } else {
            return new TempClassLoader(urls);
        }
    }

    public static URLClassLoader createTempClassLoader(URL[] urls, ClassLoader parent) {
        if (isAntiJarLocking()) {
            return new TempAntiJarLockingClassLoader(urls, parent);
        } else {
            return new TempClassLoader(urls, parent);
        }
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

    public static void clearSunJarFileFactoryCache(String jarLocation) {
        try {
            Class jarFileFactory = Class.forName("sun.net.www.protocol.jar.JarFileFactory");

            Field fileCacheField = jarFileFactory.getDeclaredField("fileCache");

            fileCacheField.setAccessible(true);
            Map fileCache = (Map) fileCacheField.get(null);

            Field urlCacheField = jarFileFactory.getDeclaredField("urlCache");
            urlCacheField.setAccessible(true);
            Map urlCache = (Map) urlCacheField.get(null);

            List<URL> urls = new ArrayList<URL>();
            for (Object item : fileCache.keySet()) {
                URL url = (URL) item;
                if (isParent(jarLocation, URLs.toFile(url))) {
                    urls.add(url);                    
                }
            }

            for (URL url : urls) {
                JarFile jarFile = (JarFile) fileCache.remove(url);
                if (jarFile == null) continue;

                urlCache.remove(jarFile);
                jarFile.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
     * Clears the caches maintained by the SunVM object stream implementation.  This method uses reflection and
     * setAccessable to obtain access to the Sun cache.  The cache is locked with a synchronize monitor and cleared.
     * This method completely clears the class loader cache which will impact preformance of object serialization.
     * @param clazz the name of the class containing the cache field
     * @param fieldName the name of the cache field
     */
    public static void clearSunSoftCache(Class clazz, String fieldName) {
        Map cache = null;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            cache = (Map) field.get(null);
        } catch (Throwable ignored) {
            // there is nothing a user could do about this anyway
        }

        if (cache != null) {
            synchronized (cache) {
                cache.clear();
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

}
