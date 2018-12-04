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
package org.apache.tomee.installer;

import java.lang.reflect.Method;
import java.util.LinkedList;

public class InstallerTools {

    public static Object invokeStaticNoArgMethod(final String className, final String propertyName) {
        try {
            final Class<?> clazz = loadClass(className, Installer.class.getClassLoader());
            final Method method = clazz.getMethod(propertyName);
            return method.invoke(null, (Object[]) null);
        } catch (final Throwable e) {
            return null;
        }
    }

    public static Class<?> loadClass(final String className, final ClassLoader classLoader) throws ClassNotFoundException {
        final LinkedList<ClassLoader> loaders = new LinkedList<>();
        for (ClassLoader loader = classLoader; loader != null; loader = loader.getParent()) {
            loaders.addFirst(loader);
        }
        for (final ClassLoader loader : loaders) {
            try {
                return Class.forName(className, true, loader);
            } catch (final ClassNotFoundException e) {
                // no-op
            }
        }
        return null;
    }

}
