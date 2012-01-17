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

import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.asm.ClassReader;
import org.apache.xbean.asm.Opcodes;
import org.apache.xbean.asm.commons.EmptyVisitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
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

    private Set<Skip> skip;

    public TempClassLoader(ClassLoader parent) {
        super(new URL[0], parent);

        Options options = SystemInstance.get().getOptions();
        skip = options.getAll("openejb.tempclassloader.skip", Skip.NONE);
    }

    /*
     * Needed for testing
     */
    public void skip(Skip s) {
        this.skip.add(s);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name == null) throw new NullPointerException("name cannot be null");
        
        // see if we've already loaded it
        Class c = findLoadedClass(name);
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
        if (skip(name)) {
            return Class.forName(name, resolve, getClass().getClassLoader());
        }
//        ( && !name.startsWith("javax.faces.") )||
        String resourceName = name.replace('.', '/') + ".class";
        InputStream in = getResourceAsStream(resourceName);
        if (in == null) {
            throw new ClassNotFoundException(name);
        }

        // 80% of class files are smaller then 6k
        ByteArrayOutputStream bout = new ByteArrayOutputStream(8 * 1024);

        // copy the input stream into a byte array
        byte[] bytes;
        try {
            byte[] buf = new byte[4 * 1024];
            for (int count; (count = in.read(buf)) >= 0;) {
                bout.write(buf, 0, count);
            }
            bytes = bout.toByteArray();
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }

        // Annotation classes must be loaded by the normal classloader
        // So must Enum classes to prevent problems with the sun jdk.
        if (skip.contains(Skip.ANNOTATIONS) && isAnnotationClass(bytes)) {
            return Class.forName(name, resolve, getClass().getClassLoader());
        }

        if (skip.contains(Skip.ENUMS) && isEnum(bytes)) {
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
        } catch (LinkageError le) {
            // fallback
            return super.loadClass(name, resolve);
        }
    }

    // TODO: for jsf it can be useful to include commons-logging and openwebbeans...
    private boolean skip(String name) {
        if (skip.equals(Skip.ALL)) {
            return true;
        }

        if (name.startsWith("javax.faces.")) return false;
        if (name.startsWith("javax.servlet.jsp.jstl")) return false;
        if (name.equals("org.apache.commons.logging.impl.LogFactoryImpl")) return false;
        if (name.startsWith("org.apache.webbeans.jsf")) return false;

        if (name.startsWith("java.")) return true;
        if (name.startsWith("javax.")) return true;
        if (name.startsWith("sun.")) return true;
        if (name.startsWith("org.apache.openjpa.")) return true;
        if (name.startsWith("org.apache.derby.")) return true;
        if (name.startsWith("org.apache.xbean.")) return true;
        if (name.startsWith("org.eclipse.jdt.")) return true;
        if (name.startsWith("org.apache.openejb.jee.")) return true;
        if (name.startsWith("org.apache.openejb.api.")) return true;
        if (name.startsWith("org.apache.commons.")) return true;
        if (name.startsWith("javassist")) return true;
        if (name.startsWith("org.codehaus.swizzle")) return true;
        if (name.startsWith("org.w3c.dom")) return true;
        if (name.startsWith("org.apache.geronimo.")) return true;
        if (name.startsWith("com.sun.org.apache.")) return true;
        if (name.startsWith("org.apache.coyote")) return true;
        if (name.startsWith("org.quartz")) return true;
        if (name.startsWith("serp.bytecode")) return true;
        if (name.startsWith("org.apache.webbeans.")) return true;

        if (name.startsWith("org.apache.log4j")) return true;
        if (name.startsWith("org.slf4j")) return true;

//        if (name.startsWith("org.apache.myfaces.")) return true;
//        if (name.startsWith("org.apache.taglibs.")) return true;
//        if (name.startsWith("org.apache.tomcat.")) return true;
//        if (name.startsWith("org.apache.el.")) return true;
//        if (name.startsWith("org.apache.jasper.")) return true;
//        if (name.startsWith("org.apache.catalina")) return true;
//        if (name.startsWith("org.apache.jsp")) return true;
//        if (name.startsWith("org.apache.naming")) return true;
//        if (name.startsWith("org.apache.openejb")) return true;

        return false;
    }

    public static enum Skip {
        NONE, ANNOTATIONS, ENUMS, ALL
    }

    /**
     * Fast-parse the given class bytecode to determine if it is an
     * enum class.
     */
    private static boolean isEnum(byte[] bytes) {
        IsEnumVisitor isEnumVisitor = new IsEnumVisitor();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(isEnumVisitor, ClassReader.SKIP_DEBUG);
        return isEnumVisitor.isEnum;
    }

    /**
     * Fast-parse the given class bytecode to determine if it is an
     * annotation class.
     */
    private static boolean isAnnotationClass(byte[] bytes) {
        IsAnnotationVisitor isAnnotationVisitor = new IsAnnotationVisitor();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(isAnnotationVisitor, ClassReader.SKIP_DEBUG);
        return isAnnotationVisitor.isAnnotation;
    }

    public static class IsAnnotationVisitor extends EmptyVisitor {
        public boolean isAnnotation = false;

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            isAnnotation = (access & Opcodes.ACC_ANNOTATION) != 0;
        }

    }
    
    public static class IsEnumVisitor extends EmptyVisitor {
        public boolean isEnum = false;

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            isEnum = (access & Opcodes.ACC_ENUM) != 0;
        }

    }
}
