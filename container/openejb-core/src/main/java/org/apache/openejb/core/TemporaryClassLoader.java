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
package org.apache.openejb.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * ClassLoader implementation that allows classes to be temporarily
 * loaded and then thrown away. Useful for verifying and inspecting
 * a class without first loading(and thus polluting) the parent
 * ClassLoader.
 * </p>
 * This class is a proper subclass of URLClassLoader.  This class
 * will locally load any class except for those defined in the
 * java.*, javax.* and sun.* packages and annotations all of which
 * are loaded by with
 * <code>Class.forName(name, resolve, getClass().getClassLoader())</code>
 * @author Marc Prud'hommeaux
 */
// Note: this class is a fork from OpenJPA
public class TemporaryClassLoader extends URLClassLoader {
    public TemporaryClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public TemporaryClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public TemporaryClassLoader(URL[] urls) {
        super(urls);
    }

    public TemporaryClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // see if we've already loaded it
        Class c = findLoadedClass(name);
        if (c != null) {
            return c;
        }

        // bug #283. defer to system if the name is a protected name.
        // "sun." is required for JDK 1.4, which has an access check for
        // sun.reflect.GeneratedSerializationConstructorAccessor1
        if (name.startsWith("java.") ||
                name.startsWith("javax.") ||
                name.startsWith("sun.")) {
            return Class.forName(name, resolve, getClass().getClassLoader());
        }

        String resourceName = name.replace('.', '/') + ".class";
        InputStream in = getResourceAsStream(resourceName);
        if (in == null) {
            throw new ClassNotFoundException(name);
        }

        // 80% of class files are smaller then 6k
        ByteArrayOutputStream bout = new ByteArrayOutputStream(8 * 1024);

        // copy the input stream into a byte array
        byte[] bytes = new byte[0];
        try {
            byte[] buf = new byte[4 * 1024];
            for (int count = -1; (count = in.read(buf)) >= 0;) {
                bout.write(buf, 0, count);
            }
            bytes = bout.toByteArray();
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }

        // Annotation classes must be loaded by the normal classloader
        if (isAnnotationClass(bytes)) {
            return Class.forName(name, resolve, getClass().getClassLoader());
        }

        // define the package
        int packageEndIndex = name.lastIndexOf('.');
        if (packageEndIndex != -1) {
            String packageName = name.substring(0, packageEndIndex);
            if (getPackage(packageName) == null) {
                definePackage(packageName, null, null, null, null, null, null, null);
            }
        }

        // define the class
        try {
            return defineClass(name, bytes, 0, bytes.length);
        } catch (SecurityException e) {
            // possible prohibited package: defer to the parent
            return super.loadClass(name, resolve);
        }
    }

    /**
     * Fast-parse the given class bytecode to determine if it is an
     * annotation class.
     */
    private static boolean isAnnotationClass(byte[] bytes) {
        IsAnnotationVisitor isAnnotationVisitor = new IsAnnotationVisitor();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(isAnnotationVisitor, true);
        return isAnnotationVisitor.isAnnotation;
    }

    public static class IsAnnotationVisitor extends EmptyVisitor {
        public boolean isAnnotation = false;

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            isAnnotation = (access & Opcodes.ACC_ANNOTATION) != 0;
        }

    }
}
