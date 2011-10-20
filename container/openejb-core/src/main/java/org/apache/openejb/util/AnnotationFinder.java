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
package org.apache.openejb.util;

import org.apache.xbean.asm.AnnotationVisitor;
import org.apache.xbean.asm.Attribute;
import org.apache.xbean.asm.ClassReader;
import org.apache.xbean.asm.ClassVisitor;
import org.apache.xbean.asm.FieldVisitor;
import org.apache.xbean.asm.MethodVisitor;
import org.apache.xbean.finder.UrlSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * ClassFinder searches the classpath of the specified classloader for
 * packages, classes, constructors, methods, or fields with specific annotations.
 *
 * For security reasons ASM is used to find the annotations.  Classes are not
 * loaded unless they match the requirements of a called findAnnotated* method.
 * Once loaded, these classes are cached.
 *
 * The getClassesNotLoaded() method can be used immediately after any find*
 * method to get a list of classes which matched the find requirements (i.e.
 * contained the annotation), but were unable to be loaded.
 *
 * @author David Blevins
 * @version $Rev$ $Date$
 */
public class AnnotationFinder {

    private final ClassLoader classLoader;
    private final List<String> classesNotLoaded = new ArrayList<String>();
    private final int ASM_FLAGS = ClassReader.SKIP_CODE + ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES;
    private final Collection<URL> urls;
    private List<String> classNames;

    /**
     * Creates a ClassFinder that will search the urls in the specified classloader
     * excluding the urls in the classloader's parent.
     *
     * To include the parent classloader, use:
     *
     *    new ClassFinder(classLoader, false);
     *
     * To exclude the parent's parent, use:
     *
     *    new ClassFinder(classLoader, classLoader.getParent().getParent());
     *
     * @param classLoader source of classes to scan
     * @throws Exception if something goes wrong
     */
    public AnnotationFinder(ClassLoader classLoader) throws Exception {
        this(classLoader, true);
    }

    /**
     * Creates a ClassFinder that will search the urls in the specified classloader.
     *
     * @param classLoader source of classes to scan
     * @param excludeParent Allegedly excludes classes from parent classloader, whatever that might mean
     * @throws Exception if something goes wrong.
     */
    public AnnotationFinder(ClassLoader classLoader, boolean excludeParent) throws Exception {
        this(classLoader, AnnotationFinder.getUrls(classLoader, excludeParent));
    }

    /**
     * Creates a ClassFinder that will search the urls in the specified classloader excluding
     * the urls in the 'exclude' classloader.
     *
     * @param classLoader source of classes to scan
     * @param exclude source of classes to exclude from scanning
     * @throws Exception if something goes wrong
     */
    public AnnotationFinder(ClassLoader classLoader, ClassLoader exclude) throws Exception {
        this(classLoader, AnnotationFinder.getUrls(classLoader, exclude));
    }

    public AnnotationFinder(ClassLoader classLoader, URL url) {
        this(classLoader, Arrays.asList(url));
    }

    public AnnotationFinder(ClassLoader classLoader, Collection<URL> urls) {
        this.classLoader = classLoader;
        this.urls = urls;
        classNames = new ArrayList<String>();
        for (URL location : urls) {
            try {
                if (location.getProtocol().equals("jar")) {
                    classNames.addAll(jar(location));
                } else if (location.getProtocol().equals("file")) {
                    try {
                        // See if it's actually a jar
                        URL jarUrl = new URL("jar", "", location.toExternalForm() + "!/");
                        JarURLConnection juc = (JarURLConnection) jarUrl.openConnection();
                        juc.getJarFile();
                        classNames.addAll(jar(jarUrl));
                    } catch (IOException e) {
                        classNames.addAll(file(location));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a list of classes that could not be loaded in last invoked findAnnotated* method.
     * <p/>
     * The list will only contain entries of classes whose byte code matched the requirements
     * of last invoked find* method, but were unable to be loaded and included in the results.
     * <p/>
     * The list returned is unmodifiable.  Once obtained, the returned list will be a live view of the
     * results from the last findAnnotated* method call.
     * <p/>
     * This method is not thread safe.
     * @return an unmodifiable live view of classes that could not be loaded in previous findAnnotated* call.
     */
    public List<String> getClassesNotLoaded() {
        return Collections.unmodifiableList(classesNotLoaded);
    }

    public boolean find(Filter filter){
        Visitor annotationVisitor = new Visitor(filter);

        for (String className : classNames) {
            try {
                readClassDef(className, annotationVisitor);
            } catch (NotFoundException e) {
            } catch (FoundException e) {
                return true;
            }
        }
        return false;
    }

    public interface Filter {
        boolean accept(String annotationName);
    }

    private static Collection<URL> getUrls(ClassLoader classLoader, boolean excludeParent) throws IOException {
        return AnnotationFinder.getUrls(classLoader, excludeParent? classLoader.getParent() : null);
    }

    private static Collection<URL> getUrls(ClassLoader classLoader, ClassLoader excludeParent) throws IOException {
        UrlSet urlSet = new UrlSet(classLoader);
        if (excludeParent != null){
            urlSet = urlSet.exclude(excludeParent);
        }
        return urlSet.getUrls();
    }

    private List<String> file(URL location) {
        List<String> classNames = new ArrayList<String>();
        File dir = new File(URLDecoder.decode(location.getPath()));
        if (dir.getName().equals("META-INF")) {
            dir = dir.getParentFile(); // Scrape "META-INF" off
        }
        if (dir.isDirectory()) {
            scanDir(dir, classNames, "");
        }
        return classNames;
    }

    private void scanDir(File dir, List<String> classNames, String packageName) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                scanDir(file, classNames, packageName + file.getName() + ".");
            } else if (file.getName().endsWith(".class")) {
                String name = file.getName();
                name = name.replaceFirst(".class$", "");
                if (name.contains(".")) continue;
                classNames.add(packageName + name);
            }
        }
    }

    private List<String> jar(URL location) throws IOException {
        String jarPath = location.getFile();
        if (jarPath.indexOf("!") > -1){
            jarPath = jarPath.substring(0, jarPath.indexOf("!"));
        }
        URL url = new URL(jarPath);
        if ("file".equals(url.getProtocol())) { // ZipFile is faster than ZipInputStream
            JarFile jarFile = new JarFile(url.getFile().replace("%20", " "));
            return jar(jarFile);
        } else {
            InputStream in = url.openStream();
            try {
                JarInputStream jarStream = new JarInputStream(in);
                return jar(jarStream);
            } finally {
                in.close();
            }
        }
    }

    private List<String> jar(JarFile jarFile) {
        List<String> classNames = new ArrayList<String>();

        Enumeration<? extends JarEntry> jarEntries =jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry entry = jarEntries.nextElement();
            addClassName(classNames, entry);
        }

        return classNames;
    }

    private List<String> jar(JarInputStream jarStream) throws IOException {
        List<String> classNames = new ArrayList<String>();

        JarEntry entry;
        while ((entry = jarStream.getNextJarEntry()) != null) {
            addClassName(classNames, entry);
        }

        return classNames;
    }

    private void addClassName(List<String> classNames, JarEntry entry) {
        if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
            return;
        }
        String className = entry.getName();
        className = className.replaceFirst(".class$", "");
        if (className.contains(".")) {
            return;
        }
        className = className.replace(File.separatorChar, '.');
        classNames.add(className);
    }

    private void readClassDef(String className, ClassVisitor visitor) {
        classes++;
        if (!className.endsWith(".class")) {
            className = className.replace('.', '/') + ".class";
        }
        try {
            URL resource = classLoader.getResource(className);
            if (resource != null) {
                InputStream in = resource.openStream();
//                in = new BufferedInputStream(in, 8192 / 4);
                try {
                    ClassReader classReader = new ClassReader(in);
                    classReader.accept(visitor, ASM_FLAGS);
                } finally {
                    in.close();
                }
            } else {
                new Exception("Could not load " + className).printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int classes;

    public static class NotFoundException extends RuntimeException {
    }

    public static class FoundException extends RuntimeException {
    }


    public class Visitor implements ClassVisitor {
        private NotFoundException notFoundException;
        private FoundException foundException;
        private final Filter filter;

        public Visitor(Filter filter) {
            this.filter = filter;

            try {
                throw new NotFoundException();
            } catch (NotFoundException e) {
                notFoundException = e;
            }

            try {
                throw new FoundException();
            } catch (FoundException e) {
                foundException = e;
            }
        }

        public AnnotationVisitor visitAnnotation(String name, boolean visible) {
            // annotation names show up as
            // Ljavax.ejb.Stateless;
            // so we hack of the first and last chars and replace the slashes
            StringBuilder sb = new StringBuilder(name);
            sb.deleteCharAt(0);
            sb.deleteCharAt(sb.length()-1);
            for (int i = 0; i < sb.length(); i++) {
                if (sb.charAt(i) == '/'){
                    sb.setCharAt(i, '.');
                }
            }

            name = sb.toString();

            if (filter.accept(name)){
                throw foundException;
            }
            return null;
        }


        public void visit(int i, int i1, String string, String string1, String string2, String[] strings) {
        }

        public void visitSource(String string, String string1) {
        }

        public void visitOuterClass(String string, String string1, String string2) {
        }

        public void visitAttribute(Attribute attribute) {
            throw notFoundException;
        }

        public void visitInnerClass(String string, String string1, String string2, int i) {
            throw notFoundException;
        }

        public FieldVisitor visitField(int i, String string, String string1, String string2, Object object) {
            throw notFoundException;
        }

        public MethodVisitor visitMethod(int i, String string, String string1, String string2, String[] strings) {
            throw notFoundException;
        }

        public void visitEnd() {
            throw notFoundException;
        }

    }



}
