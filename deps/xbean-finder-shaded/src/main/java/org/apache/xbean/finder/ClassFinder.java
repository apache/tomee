/**
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
package org.apache.xbean.finder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarEntry;
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
public class ClassFinder extends AbstractFinder {

    private final ClassLoader classLoader;

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
    public ClassFinder(ClassLoader classLoader) throws Exception {
        this(classLoader, true);
    }

    /**
     * Creates a ClassFinder that will search the urls in the specified classloader.
     *
     * @param classLoader source of classes to scan
     * @param excludeParent Allegedly excludes classes from parent classloader, whatever that might mean
     * @throws Exception if something goes wrong.
     */
    public ClassFinder(ClassLoader classLoader, boolean excludeParent) throws Exception {
        this(classLoader, getUrls(classLoader, excludeParent));
    }

    /**
     * Creates a ClassFinder that will search the urls in the specified classloader excluding
     * the urls in the 'exclude' classloader.
     *
     * @param classLoader source of classes to scan
     * @param exclude source of classes to exclude from scanning
     * @throws Exception if something goes wrong
     */
    public ClassFinder(ClassLoader classLoader, ClassLoader exclude) throws Exception {
        this(classLoader, getUrls(classLoader, exclude));
    }

    public ClassFinder(ClassLoader classLoader, URL url) {
        this(classLoader, Arrays.asList(url));
    }

    public ClassFinder(ClassLoader classLoader, Collection<URL> urls) {
        this.classLoader = classLoader;

        List<String> classNames = new ArrayList<String>();
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

        for (String className : classNames) {
            readClassDef(className);
        }
    }

    public ClassFinder(Class<?>... classes){
        this(Arrays.asList(classes));
    }

    public ClassFinder(List<Class<?>> classes){
        this.classLoader = null;
        for (Class<?> clazz : classes) {
            try {
                readClassDef(clazz);
            } catch (NoClassDefFoundError e) {
                throw new NoClassDefFoundError("Could not fully load class: " + clazz.getName() + "\n due to:" + e.getMessage() + "\n in classLoader: \n" + clazz.getClassLoader());
            }
        }
    }

    private static Collection<URL> getUrls(ClassLoader classLoader, boolean excludeParent) throws IOException {
        return getUrls(classLoader, excludeParent? classLoader.getParent() : null);
    }

    private static Collection<URL> getUrls(ClassLoader classLoader, ClassLoader excludeParent) throws IOException {
        UrlSet urlSet = new UrlSet(classLoader);
        if (excludeParent != null){
            urlSet = urlSet.exclude(excludeParent);
        }
        return urlSet.getUrls();
    }

    @Override
    protected URL getResource(String className) {
        return classLoader.getResource(className);
    }

    @Override
    protected Class<?> loadClass(String fixedName) throws ClassNotFoundException {
        return classLoader.loadClass(fixedName);
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
        InputStream in = url.openStream();
        try {
            JarInputStream jarStream = new JarInputStream(in);
            return jar(jarStream);
        } finally {
            in.close();
        }
    }

    private List<String> jar(JarInputStream jarStream) throws IOException {
        List<String> classNames = new ArrayList<String>();

        JarEntry entry;
        while ((entry = jarStream.getNextJarEntry()) != null) {
            if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                continue;
            }
            String className = entry.getName();
            className = className.replaceFirst(".class$", "");
            if (className.contains(".")) continue;
            className = className.replace('/', '.');
            classNames.add(className);
        }

        return classNames;
    }

}
