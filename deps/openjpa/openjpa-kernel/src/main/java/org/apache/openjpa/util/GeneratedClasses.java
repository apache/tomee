/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.util;

import java.security.AccessController;

import org.apache.openjpa.lib.util.J2DoPrivHelper;
import serp.bytecode.BCClass;
import serp.bytecode.BCClassLoader;

/**
 * Utility methods when generating classes, including at runtime.
 *
 * @since 1.0.0
 */
public class GeneratedClasses {

    /**
     * Return the more derived loader of the class laoders for the given 
     * classes.
     */
    public static ClassLoader getMostDerivedLoader(Class c1, Class c2) {
        ClassLoader l1 = AccessController.doPrivileged(
            J2DoPrivHelper.getClassLoaderAction(c1));
        ClassLoader l2 = AccessController.doPrivileged(
            J2DoPrivHelper.getClassLoaderAction(c2));
        if (l1 == l2)
            return l1;
        if (l1 == null)
            return l2;
        if (l2 == null)
            return l1;

        if(canLoad(l1, c2)) {
            return l1;
        }
        return l2;
    }

    /**
     * Load the class represented by the given bytecode.
     */
    public static Class loadBCClass(BCClass bc, ClassLoader loader) {
        BCClassLoader bcloader = AccessController
                .doPrivileged(J2DoPrivHelper.newBCClassLoaderAction(bc
                        .getProject(), loader));
        try {
            Class c = Class.forName(bc.getName(), true, bcloader);
            bc.getProject().clear();
            return c;
        } catch (Throwable t) {
            throw new GeneralException(bc.getName()).setCause(t);
        }
    }
    
    /**
     * Return true if the given loader will load the same version of a given 
     * class.  
     * 
     * @param loader Classloader to use.
     * @param clazz  Expected class. 
     * @return true if loader.load(clazz.getName()) == clazz. Otherwise false.
     */
    private static boolean canLoad(ClassLoader loader, Class clazz) {
        Class loaded = null;
        try {
            loaded = loader.loadClass(clazz.getName());
        } catch (ClassNotFoundException e) {
            // Rely on caller to handle return value = false.
        }
        return clazz == loaded;
    }
}
