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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class ClassLoaderUtil {

    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    /**
     * Cleans well known class loader leaks in VMs and libraries.  There is a lot of bad code out there and this method
     * will clear up the know problems.  This method should only be called when the class loader will no longer be used.
     * It this method is called two often it can have a serious impact on preformance.
     * @param classLoader the class loader to destroy
     */
    public static void clearClassLoaderCaches() {
        clearSunSoftCache(ObjectInputStream.class, "subclassAudits");
        clearSunSoftCache(ObjectOutputStream.class, "subclassAudits");
        clearSunSoftCache(ObjectStreamClass.class, "localDescs");
        clearSunSoftCache(ObjectStreamClass.class, "reflectors");
        Introspector.flushCaches();
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
            Class<?> pcRegistryClass = classLoader.loadClass("org.apache.openjpa.enhance.PCRegistry");
            Method deRegisterMethod = pcRegistryClass.getMethod("deRegister", ClassLoader.class);
            deRegisterMethod.invoke(null, classLoader);
        } catch (Throwable ignored) {
            // there is nothing a user could do about this anyway
        }
    }
}
