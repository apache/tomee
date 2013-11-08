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
package org.apache.openjpa.lib.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import serp.bytecode.lowlevel.ConstantPoolTable;

/**
 * ClassLoader implementation that allows classes to be temporarily
 * loaded and then thrown away. Useful for the enhancer to be able
 * to run against a class without first loading(and thus polluting)
 * the parent ClassLoader.
 *
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public class TemporaryClassLoader extends ClassLoader {

    public TemporaryClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    protected Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
        // see if we've already loaded it
        Class c = findLoadedClass(name);
        if (c != null)
            return c;

        // bug #283. defer to system if the name is a protected name.
        // "sun." is required for JDK 1.4, which has an access check for
        // sun.reflect.GeneratedSerializationConstructorAccessor1
        if (name.startsWith("java.") || name.startsWith("javax.")
            || name.startsWith("sun."))
            return Class.forName(name, resolve, getClass().getClassLoader());

        String resourceName = name.replace('.', '/') + ".class";
        InputStream resource = getResourceAsStream(resourceName);
        if (resource == null)
            throw new ClassNotFoundException(name);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        try {
            for (int n = 0; (n = resource.read(b, 0, b.length)) != -1;
                bout.write(b, 0, n))
                ;
            byte[] classBytes = bout.toByteArray();
            // To avoid classloader issues with the JVM (Sun and IBM), we
            // will not load Enums via the TemporaryClassLoader either.
            // Reference JIRA Issue OPENJPA-646 for more information.
            if (isAnnotation(classBytes) || isEnum(classBytes)) {
                try {
                    Class<?> frameworkClass = Class.forName(name, resolve,
                            getClass().getClassLoader());
                    return frameworkClass;
                } catch (ClassNotFoundException e) {
                    // OPENJPA-1121 continue, as it must be a user-defined class
                }
            }

            try {
                return defineClass(name, classBytes, 0, classBytes.length);
            } catch (SecurityException e) {
                // possible prohibited package: defer to the parent
                return super.loadClass(name, resolve);
            }
        } catch (IOException ioe) {
            // defer to the parent
            return super.loadClass(name, resolve);
        }
    }

    /**
     * Fast-parse the given class bytecode to determine if it is an
     * annotation class.
     */
    private static boolean isAnnotation(byte[] b) {
        if (JavaVersions.VERSION < 5)
            return false;
        int idx = ConstantPoolTable.getEndIndex(b);
        int access = ConstantPoolTable.readUnsignedShort(b, idx);
        return (access & 0x2000) != 0; // access constant for annotation type
    }

    /**
     * Fast-parse the given class bytecode to determine if it is an
     * enum class.
     */
    private static boolean isEnum(byte[] b) {
        if (JavaVersions.VERSION < 5)
            return false;
        int idx = ConstantPoolTable.getEndIndex(b);
        int access = ConstantPoolTable.readUnsignedShort(b, idx);
        return (access & 0x4000) != 0; // access constant for enum type
    }
}
