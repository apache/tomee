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
package org.apache.openejb.core;

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.apache.xbean.asm.ClassReader;
import org.apache.xbean.asm.Opcodes;
import org.apache.xbean.asm.commons.EmptyVisitor;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Set;

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
 */
// Note: this class is a fork from OpenJPA
public class TempClassLoader extends URLClassLoader {

    private final Set<Skip> skip;
    private final ClassLoader system;
    private final boolean embedded;

    // 80% of class files are smaller then 6k
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(6 * 1024);

    public TempClassLoader(final ClassLoader parent) {
        super(new URL[0], parent);
        this.skip = SystemInstance.get().getOptions().getAll("openejb.tempclassloader.skip", Skip.NONE);
        this.system = ClassLoader.getSystemClassLoader();
        this.embedded = this.getClass().getClassLoader() == this.system;
    }

    /*
     * Needed for testing
     */
    public void skip(final Skip s) {
        this.skip.add(s);
    }

    @Override
    public Class loadClass(final String name) throws ClassNotFoundException {
        return this.loadClass(name, false);
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        return URLClassLoaderFirst.filterResources(name, super.getResources(name));
    }

    @Override
    protected synchronized Class loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        if (name == null) throw new NullPointerException("name cannot be null");

        // see if we've already loaded it
        Class c = this.findLoadedClass(name);
        if (c != null) {
            return c;
        }

        // bug #283. defer to system if the name is a protected name.
        // "sun." is required for JDK 1.4, which has an access check for
        // sun.reflect.GeneratedSerializationConstructorAccessor1
        /*
         * FIX for openejb-tomcat JSF support . Added the following to the if statement below: !name.startsWith("javax.faces")
         *We want to use this TempClassLoader to also load the classes in the javax.faces package. 
         *If not, then our AnnotationDeployer will not be able to load the javax.faces.FacesServlet class if this class is in a jar which 
         *is in the WEB-INF/lib directory of a web app. 
         * see AnnotationDeployer$ProcessAnnotatedBeans.deploy(WebModule webModule) 
         * Here is what happened  before this fix was applied:
         * 1. The AnnotationDeployer tries to load the javax.faces.FacesServlet using this classloader (TempClassLoader)
         * 2. Since this class loader uses Class.forName to load classes starting with java, javax or sun, it cannot load javax.faces.FacesServlet
         * 3. Result is , AnnotationDeployer throws a ClassNotFoundException
         */
        if (this.skip(name)) {
            return Class.forName(name, resolve, this.getClass().getClassLoader());
        }

        // don't load classes from app classloader
        // we do it after the previous one since it will probably result to the same
        // Class and the previous one is faster than this one
        if (!this.embedded && URLClassLoaderFirst.canBeLoadedFromSystem(name)) {
            try {
                c = this.system.loadClass(name);
                if (c != null) {
                    return c;
                }
            } catch (ClassNotFoundException ignored) {
                // no-op
            }
        }

//        ( && !name.startsWith("javax.faces.") )||
        final String resourceName = name.replace('.', '/') + ".class";

        //Copy the input stream into a byte array
        final byte[] bytes;
        this.bout.reset();
        InputStream in = null;

        try {

            in = this.getResourceAsStream(resourceName);

            if (in != null && !(in instanceof BufferedInputStream)) {
                in = new BufferedInputStream(in);
            }

            if (in == null) {
                throw new ClassNotFoundException(name);
            }

            IO.copy(in, this.bout);
            bytes = this.bout.toByteArray();

        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        } finally {
            IO.close(in);
        }

        // Annotation classes must be loaded by the normal classloader
        // So must Enum classes to prevent problems with the sun jdk.
        if (this.skip.contains(Skip.ANNOTATIONS) && isAnnotationClass(bytes)) {
            return Class.forName(name, resolve, this.getClass().getClassLoader());
        }

        if (this.skip.contains(Skip.ENUMS) && isEnum(bytes)) {
            return Class.forName(name, resolve, this.getClass().getClassLoader());
        }

        // define the package
        final int packageEndIndex = name.lastIndexOf('.');
        if (packageEndIndex != -1) {
            final String packageName = name.substring(0, packageEndIndex);
            if (this.getPackage(packageName) == null) {
                this.definePackage(packageName, null, null, null, null, null, null, null);
            }
        }

        // define the class
        try {
            return this.defineClass(name, bytes, 0, bytes.length);
        } catch (SecurityException e) {
            // possible prohibited package: defer to the parent
            return super.loadClass(name, resolve);
        } catch (LinkageError le) {
            // fallback
            return super.loadClass(name, resolve);
        }
    }

    // TODO: for jsf it can be useful to include commons-logging and openwebbeans...
    private boolean skip(final String name) {
        return this.skip.contains(Skip.ALL) || URLClassLoaderFirst.shouldSkip(name);
    }

    public static enum Skip {
        NONE, ANNOTATIONS, ENUMS, ALL
    }

    /**
     * Fast-parse the given class bytecode to determine if it is an
     * enum class.
     */
    private static boolean isEnum(final byte[] bytes) {
        final IsEnumVisitor isEnumVisitor = new IsEnumVisitor();
        final ClassReader classReader = new ClassReader(bytes);
        classReader.accept(isEnumVisitor, ClassReader.SKIP_DEBUG);
        return isEnumVisitor.isEnum;
    }

    /**
     * Fast-parse the given class bytecode to determine if it is an
     * annotation class.
     */
    private static boolean isAnnotationClass(final byte[] bytes) {
        final IsAnnotationVisitor isAnnotationVisitor = new IsAnnotationVisitor();
        final ClassReader classReader = new ClassReader(bytes);
        classReader.accept(isAnnotationVisitor, ClassReader.SKIP_DEBUG);
        return isAnnotationVisitor.isAnnotation;
    }

    public static class IsAnnotationVisitor extends EmptyVisitor {
        public boolean isAnnotation = false;

        @Override
        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
            this.isAnnotation = (access & Opcodes.ACC_ANNOTATION) != 0;
        }

    }

    public static class IsEnumVisitor extends EmptyVisitor {
        public boolean isEnum = false;

        @Override
        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
            this.isEnum = (access & Opcodes.ACC_ENUM) != 0;
        }

    }
}
