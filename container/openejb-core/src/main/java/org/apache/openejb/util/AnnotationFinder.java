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

import org.apache.openejb.config.DeploymentsResolver;
import org.apache.xbean.asm9.AnnotationVisitor;
import org.apache.xbean.asm9.Attribute;
import org.apache.xbean.asm9.ClassReader;
import org.apache.xbean.asm9.ClassVisitor;
import org.apache.xbean.asm9.FieldVisitor;
import org.apache.xbean.asm9.MethodVisitor;
import org.apache.xbean.asm9.Opcodes;
import org.apache.xbean.finder.UrlSet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
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
 * @version $Rev$ $Date$
 */
public class AnnotationFinder {
    private static final int ASM_FLAGS = ClassReader.SKIP_CODE + ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES;

    private final ClassLoader classLoader;
    private final List<String> classesNotLoaded = new ArrayList<>();
    private final List<String> classNames;

    /**
     * Creates a ClassFinder that will search the urls in the specified classloader
     * excluding the urls in the classloader's parent.
     *
     * To include the parent classloader, use:
     *
     * new ClassFinder(classLoader, false);
     *
     * To exclude the parent's parent, use:
     *
     * new ClassFinder(classLoader, classLoader.getParent().getParent());
     *
     * @param classLoader source of classes to scan
     * @throws Exception if something goes wrong
     */
    public AnnotationFinder(final ClassLoader classLoader) throws Exception {
        this(classLoader, true);
    }

    /**
     * Creates a ClassFinder that will search the urls in the specified classloader.
     *
     * @param classLoader   source of classes to scan
     * @param excludeParent Allegedly excludes classes from parent classloader, whatever that might mean
     * @throws Exception if something goes wrong.
     */
    public AnnotationFinder(final ClassLoader classLoader, final boolean excludeParent) throws Exception {
        this(classLoader, AnnotationFinder.getUrls(classLoader, excludeParent));
    }

    /**
     * Creates a ClassFinder that will search the urls in the specified classloader excluding
     * the urls in the 'exclude' classloader.
     *
     * @param classLoader source of classes to scan
     * @param exclude     source of classes to exclude from scanning
     * @throws Exception if something goes wrong
     */
    public AnnotationFinder(final ClassLoader classLoader, final ClassLoader exclude) throws Exception {
        this(classLoader, AnnotationFinder.getUrls(classLoader, exclude));
    }

    public AnnotationFinder(final ClassLoader classLoader, final URL url) {
        this(classLoader, Collections.singletonList(url));
    }

    public AnnotationFinder(final ClassLoader classLoader, final Collection<URL> urls) {
        this.classLoader = classLoader;
        classNames = new ArrayList<>();
        for (final URL location : urls) {
            if (location == null) {
                continue;
            }

            try {
                if (location.getProtocol().equals("jar")) {
                    classNames.addAll(jar(location));
                } else if (location.getProtocol().equals("file")) {
                    try {
                        // See if it's actually a jar
                        final URL jarUrl = new URL("jar", "", location.toExternalForm().replace("%20", " ").replace("%23", "#") + "!/");
                        final JarURLConnection juc = (JarURLConnection) jarUrl.openConnection();
                        classNames.addAll(jar(juc.getJarFile()));
                    } catch (final IOException e) {
                        classNames.addAll(file(location));
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a list of classes that could not be loaded in last invoked findAnnotated* method.
     *
     * The list will only contain entries of classes whose byte code matched the requirements
     * of last invoked find* method, but were unable to be loaded and included in the results.
     *
     * The list returned is unmodifiable.  Once obtained, the returned list will be a live view of the
     * results from the last findAnnotated* method call.
     *
     * This method is not thread safe.
     *
     * @return an unmodifiable live view of classes that could not be loaded in previous findAnnotated* call.
     */
    public List<String> getClassesNotLoaded() {
        return Collections.unmodifiableList(classesNotLoaded);
    }

    public boolean find(final Filter filter) {
        final Visitor annotationVisitor = new Visitor(filter);

        for (final String className : classNames) {
            try {
                readClassDef(className, annotationVisitor);
            } catch (final NotFoundException e) {
                // no-op
            } catch (final FoundException e) {
                return true;
            }
        }
        return false;
    }

    public interface Filter {

        boolean accept(String annotationName);
    }

    private static Collection<URL> getUrls(final ClassLoader classLoader, final boolean excludeParent) throws IOException {
        return AnnotationFinder.getUrls(classLoader, excludeParent ? classLoader.getParent() : null);
    }

    private static Collection<URL> getUrls(final ClassLoader classLoader, final ClassLoader excludeParent) throws IOException {
        UrlSet urlSet = new UrlSet(classLoader);
        if (excludeParent != null) {
            urlSet = urlSet.exclude(excludeParent);
        }
        return urlSet.getUrls();
    }

    @SuppressWarnings("deprecation")
    private static List<String> file(final URL location) {
        final List<String> classNames = new ArrayList<>();
        File dir;
        try {
            dir = new File(URLDecoder.decode(location.getPath(), "UTF-8"));
        } catch (final Exception e) {
            dir = new File(URLDecoder.decode(location.getPath()));
        }
        if (dir.getName().equals("META-INF")) {
            dir = dir.getParentFile(); // Scrape "META-INF" off
        }
        if (dir.isDirectory()) {
            scanDir(dir, classNames, "");
        }
        return classNames;
    }

    private static void scanDir(final File dir, final List<String> classNames, final String packageName) {
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    if (DeploymentsResolver.isExtractedDir(file)) {
                        continue;
                    }

                    scanDir(file, classNames, packageName + file.getName() + ".");
                } else if (file.getName().endsWith(".class")) {
                    String name = file.getName();
                    name = name.replaceFirst(".class$", "");
                    if (name.contains(".")) {
                        continue;
                    }
                    classNames.add(packageName + name);
                }
            }
        }
    }

    private static List<String> jar(final URL location) throws IOException, URISyntaxException {
        String jarPath = location.getFile();
        if (jarPath.contains("!")) {
            jarPath = jarPath.substring(0, jarPath.indexOf('!'));
        }
        final URL url = new URL(jarPath);
        if ("file".equals(url.getProtocol())) { // ZipFile is faster than ZipInputStream
            final JarFile jarFile = new JarFile(url.getFile().replace("%20", " ").replace("%23", "#"));
            return jar(jarFile);
        } else {
            InputStream in = url.openStream();
            in = new BufferedInputStream(in);
            try {
                final JarInputStream jarStream = new JarInputStream(in);
                return jar(jarStream);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    //no-op
                }
            }
        }
    }

    private static List<String> jar(final JarFile jarFile) {
        final List<String> classNames = new ArrayList<>();

        final Enumeration<? extends JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            final JarEntry entry = jarEntries.nextElement();
            addClassName(classNames, entry);
        }

        return classNames;
    }

    private static List<String> jar(final JarInputStream jarStream) throws IOException {
        final List<String> classNames = new ArrayList<>();

        JarEntry entry;
        while ((entry = jarStream.getNextJarEntry()) != null) {
            addClassName(classNames, entry);
        }

        return classNames;
    }

    private static void addClassName(final List<String> classNames, final JarEntry entry) {
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

    private void readClassDef(String className, final ClassVisitor visitor) {
        classes++;
        if (!className.endsWith(".class")) {
            className = className.replace('.', '/') + ".class";
        }
        try {
            final URL resource = classLoader.getResource(className);
            if (resource != null) {
                InputStream in = resource.openStream();
                in = new BufferedInputStream(in);
                try {
                    final ClassReader classReader = new ClassReader(in);
                    classReader.accept(visitor, ASM_FLAGS);
                } finally {
                    in.close();
                }
            } else {
                new Exception("Could not load " + className).printStackTrace();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    public static int classes;

    public static class NotFoundException extends RuntimeException {

    }

    public static class FoundException extends RuntimeException {

    }

    public class Visitor extends ClassVisitor {

        private NotFoundException notFoundException;
        private final FoundException foundException;
        private final Filter filter;

        public Visitor(final Filter filter) {
            super(Opcodes.ASM9);
            this.filter = filter;

            try {
                throw new NotFoundException();
            } catch (final NotFoundException e) {
                notFoundException = e;
            }

            try {
                throw new FoundException();
            } catch (final FoundException e) {
                foundException = e;
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, final boolean visible) {
            // annotation names show up as
            // Ljakarta.ejb.Stateless;
            // so we hack of the first and last chars and replace the slashes
            final StringBuilder sb = new StringBuilder(name);
            sb.deleteCharAt(0);
            sb.deleteCharAt(sb.length() - 1);
            for (int i = 0; i < sb.length(); i++) {
                if (sb.charAt(i) == '/') {
                    sb.setCharAt(i, '.');
                }
            }

            name = sb.toString();

            if (filter.accept(name)) {
                throw foundException;
            }
            return null;
        }

        @Override
        public void visit(final int i, final int i1, final String string, final String string1, final String string2, final String[] strings) {
        }

        @Override
        public void visitSource(final String string, final String string1) {
        }

        @Override
        public void visitOuterClass(final String string, final String string1, final String string2) {
        }

        @Override
        public void visitAttribute(final Attribute attribute) {
            throw notFoundException;
        }

        @Override
        public void visitInnerClass(final String string, final String string1, final String string2, final int i) {
            throw notFoundException;
        }

        @Override
        public FieldVisitor visitField(final int i, final String string, final String string1, final String string2, final Object object) {
            throw notFoundException;
        }

        @Override
        public MethodVisitor visitMethod(final int i, final String string, final String string1, final String string2, final String[] strings) {
            throw notFoundException;
        }

        @Override
        public void visitEnd() {
            throw notFoundException;
        }

    }

}
