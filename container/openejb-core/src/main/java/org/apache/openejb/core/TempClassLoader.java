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
import org.apache.xbean.asm9.ClassReader;
import org.apache.xbean.asm9.Opcodes;
import org.apache.xbean.asm9.shade.commons.EmptyVisitor;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
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
    private static final ClassLoader PARENT_LOADER = ParentClassLoaderFinder.Helper.get();
    private static final URL[] EMPTY_URLS = new URL[0];

    private final Set<Skip> skip;
    private final ClassLoader system;
    private final boolean embedded;
    private final boolean parentURLClassLoader;

    // 80% of class files are smaller then 6k
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(6 * 1024);

    public TempClassLoader(final ClassLoader parent) {
        super(EMPTY_URLS, parent);
        this.skip = SystemInstance.get().getOptions().getAll("openejb.tempclassloader.skip", Skip.NONE);
        this.system = ClassLoader.getSystemClassLoader();
        this.embedded = this.getClass().getClassLoader() == this.system;
        this.parentURLClassLoader = URLClassLoader.class.isInstance(parent);
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
    public URL getResource(final String name) {
        // try specific url first
        final URL url = parentURLClassLoader ? URLClassLoader.class.cast(getParent()).findResource(name) : null;
        if (url != null) {
            return url;
        }
        return super.getResource(name);
    }

    public URL getInternalResource(final String name) {
        if (!name.startsWith("java/") && !name.startsWith("javax/") && !name.startsWith("jakarta/") && name.endsWith(".class")) {
            try {
                final Enumeration<URL> resources = getResources(name);
                if (!resources.hasMoreElements()) {
                    return null;
                }
                final URL url = resources.nextElement();
                if (resources.hasMoreElements()) { // avoid useless allocations
                    final List<URL> l = new ArrayList<>(2);
                    l.add(url);
                    while (resources.hasMoreElements()) {
                        l.add(resources.nextElement());
                    }
                    l.sort(new ResourceComparator(getParent(), name));
                    return l.iterator().next();
                }
                return url;
            } catch (final IOException e) {
                return super.getResource(name);
            }
        }
        return super.getResource(name);
    }

    public Enumeration<URL> getResources(final String name) throws IOException {
        return URLClassLoaderFirst.filterResources(name, super.getResources(name));
    }

    @Override
    protected synchronized Class loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }

        // see if we've already loaded it
        Class c = this.findLoadedClass(name);
        if (c != null) {
            return c;
        }

        // bug #283. defer to system if the name is a protected name.
        // "sun." is required for JDK 1.4, which has an access check for
        // sun.reflect.GeneratedSerializationConstructorAccessor1
        /*
         * FIX for openejb-tomcat JSF support . Added the following to the if statement below: !name.startsWith("jakarta.faces")
         *We want to use this TempClassLoader to also load the classes in the jakarta.faces package. 
         *If not, then our AnnotationDeployer will not be able to load the jakarta.faces.FacesServlet class if this class is in a jar which 
         *is in the WEB-INF/lib directory of a web app. 
         * see AnnotationDeployer$ProcessAnnotatedBeans.deploy(WebModule webModule) 
         * Here is what happened  before this fix was applied:
         * 1. The AnnotationDeployer tries to load the jakarta.faces.FacesServlet using this classloader (TempClassLoader)
         * 2. Since this class loader uses Class.forName to load classes starting with java, javax or sun, it cannot load jakarta.faces.FacesServlet
         * 3. Result is , AnnotationDeployer throws a ClassNotFoundException
         */
        if (this.skip(name) || name.startsWith("jakarta.faces.") && URLClassLoaderFirst.shouldSkipJsf(getParent(), name)) {
            return Class.forName(name, resolve, PARENT_LOADER);
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
            } catch (final ClassNotFoundException | NoClassDefFoundError ignored) {
                // no-op
            }
        }

//        ( && !name.startsWith("jakarta.faces.") )||
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

        } catch (final IOException e) {
            throw new ClassNotFoundException(name, e);
        } finally {
            IO.close(in);
        }

        // Annotation classes must be loaded by the normal classloader
        // So must Enum classes to prevent problems with the sun jdk.
        if (this.skip.contains(Skip.ANNOTATIONS) && isAnnotationClass(bytes)) {
            return Class.forName(name, resolve, PARENT_LOADER);
        }

        if (this.skip.contains(Skip.ENUMS) && isEnum(bytes)) {
            return Class.forName(name, resolve, PARENT_LOADER);
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
        } catch (final SecurityException | LinkageError e) {
            // possible prohibited package: defer to the parent
            return super.loadClass(name, resolve);
        } // fallback

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
        public boolean isAnnotation;

        @Override
        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
            this.isAnnotation = (access & Opcodes.ACC_ANNOTATION) != 0;
        }

    }

    public static class IsEnumVisitor extends EmptyVisitor {
        public boolean isEnum;

        @Override
        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
            this.isEnum = (access & Opcodes.ACC_ENUM) != 0;
        }

    }

    // let maven resources go after other ones (arquillian tomee embedded and @WebXXX needs it absolutely)
    private static final class ResourceComparator implements Comparator<URL> {
        private static final boolean FORCE_MAVEN_FIRST = "true".equals(SystemInstance.get().getProperty("openejb.classloader.force-maven", "false"));
        private static final ClassLoader STOP_LOADER = getSystemClassLoader().getParent();

        private final ClassLoader loader;
        private final String name;

        private ResourceComparator(final ClassLoader loader, final String name) {
            this.loader = loader;
            this.name = name;
        }

        @Override
        public int compare(final URL o1, final URL o2) {
            if (o1.equals(o2)) {
                return 0;
            }

            final int weight1 = weight(o1);
            final int weight2 = weight(o2);
            if (weight1 == weight2) {
                final String s1 = o1.toExternalForm().replace(File.separatorChar, '/');
                final String s2 = o2.toExternalForm().replace(File.separatorChar, '/');
                if (FORCE_MAVEN_FIRST) { // tomee maven plugin dev feature
                    if (s1.contains("/target/classes/") || s1.contains("/build/classes/main/")) {
                        return -1;
                    }
                    if (s2.contains("/target/classes/") || s2.contains("/build/classes/main/")) {
                        return 1;
                    }
                    if (s1.contains("/target/test-classes/") || s1.contains("/build/classes/test/")) {
                        return -1;
                    }
                    if (s2.contains("/target/test-classes/") || s2.contains("/build/classes/test/")) {
                        return 1;
                    }
                }
                if (s1.contains("/WEB-INF/classes/")) {
                    return -1;
                }
                if (s2.contains("/WEB-INF/classes/")) {
                    return 1;
                }
                return s1.compareTo(s2);
            }
            // tomee embedded case, we can load with system loader instead of webapp loader
            return weight1 - weight2;
        }

        private int weight(final URL url) {
            int w = 0;
            ClassLoader c = loader;
            while (c != null) {
                try {
                    if (Collections.list(c.getResources(name)).contains(url)) {
                        w++;
                    } else {
                        break;
                    }
                } catch (final IOException e) {
                    break;
                }
                c = c.getParent();
                if (c == STOP_LOADER) {
                    break;
                }
            }
            return w;
        }
    }
}
