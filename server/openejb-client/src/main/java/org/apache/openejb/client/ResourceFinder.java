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
package org.apache.openejb.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Note: This class is a copy of the xbean-finder ResourceFinder class.  Any changes should
 * be make to the xbean-finder source first and then copied to this class.  Also, this class
 * should be kept in sync with the xbean-finder code.
 */
@SuppressWarnings("unchecked")
public class ResourceFinder {

    private final URL[] urls;
    private final String path;
    private final ClassLoader classLoader;
    private final List<String> resourcesNotLoaded = new ArrayList<String>();

    public ResourceFinder(final URL... urls) {
        this(null, Thread.currentThread().getContextClassLoader(), urls);
    }

    public ResourceFinder(final String path) {
        this(path, Thread.currentThread().getContextClassLoader(), (URL[]) null);
    }

    public ResourceFinder(final String path, final URL... urls) {
        this(path, Thread.currentThread().getContextClassLoader(), urls);
    }

    public ResourceFinder(final String path, final ClassLoader classLoader) {
        this(path, classLoader, (URL[]) null);
    }

    public ResourceFinder(String path, ClassLoader classLoader, final URL... urls) {
        if (path == null) {
            path = "";
        } else if (path.length() > 0 && !path.endsWith("/")) {
            path += "/";
        }
        this.path = path;

        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        this.classLoader = classLoader;

        for (int i = 0; urls != null && i < urls.length; i++) {
            final URL url = urls[i];
            if (url == null || isDirectory(url) || url.getProtocol().equals("jar")) {
                continue;
            }
            try {
                urls[i] = new URL("jar", "", -1, url.toString() + "!/");
            } catch (MalformedURLException e) {
                //Ignore
            }
        }
        this.urls = (urls == null || urls.length == 0) ? null : urls;
    }

    private static boolean isDirectory(final URL url) {
        final String file = url.getFile();
        return (file.length() > 0 && file.charAt(file.length() - 1) == '/');
    }

    /**
     * Returns a list of resources that could not be loaded in the last invoked findAvailable* or
     * mapAvailable* methods.
     * <p/>
     * The list will only contain entries of resources that match the requirements
     * of the last invoked findAvailable* or mapAvailable* methods, but were unable to be
     * loaded and included in their results.
     * <p/>
     * The list returned is unmodifiable and the results of this method will change
     * after each invocation of a findAvailable* or mapAvailable* methods.
     * <p/>
     * This method is not thread safe.
     */
    public List<String> getResourcesNotLoaded() {
        return Collections.unmodifiableList(resourcesNotLoaded);
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public URL find(final String uri) throws IOException {
        final String fullUri = path + uri;

        final URL resource = getResource(fullUri);
        if (resource == null) {
            throw new IOException("Could not find resource '" + fullUri + "'");
        }

        return resource;
    }

    public List<URL> findAll(final String uri) throws IOException {
        final String fullUri = path + uri;

        final Enumeration<URL> resources = getResources(fullUri);
        final List<URL> list = new ArrayList<URL>();
        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            list.add(url);
        }
        return list;
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find String
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    /**
     * Reads the contents of the URL as a {@link String}'s and returns it.
     *
     * @param uri String
     * @return a stringified content of a resource
     * @throws IOException if a resource pointed out by the uri param could not be find
     * @see ClassLoader#getResource(String)
     */
    public String findString(final String uri) throws IOException {
        final String fullUri = path + uri;

        final URL resource = getResource(fullUri);
        if (resource == null) {
            throw new IOException("Could not find a resource in : " + fullUri);
        }

        return readContents(resource);
    }

    /**
     * Reads the contents of the found URLs as a list of {@link String}'s and returns them.
     *
     * @param uri String
     * @return a list of the content of each resource URL found
     * @throws IOException if any of the found URLs are unable to be read.
     */
    public List<String> findAllStrings(final String uri) throws IOException {
        final String fulluri = path + uri;

        final List<String> strings = new ArrayList<String>();

        final Enumeration<URL> resources = getResources(fulluri);
        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            final String string = readContents(url);
            strings.add(string);
        }
        return strings;
    }

    /**
     * Reads the contents of the found URLs as a Strings and returns them.
     * Individual URLs that cannot be read are skipped and added to the
     * list of 'resourcesNotLoaded'
     *
     * @param uri String
     * @return a list of the content of each resource URL found
     * @throws IOException if classLoader.getResources throws an exception
     */
    public List<String> findAvailableStrings(final String uri) throws IOException {
        resourcesNotLoaded.clear();
        final String fulluri = path + uri;

        final List<String> strings = new ArrayList<String>();

        final Enumeration<URL> resources = getResources(fulluri);
        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            try {
                final String string = readContents(url);
                strings.add(string);
            } catch (IOException notAvailable) {
                resourcesNotLoaded.add(url.toExternalForm());
            }
        }
        return strings;
    }

    /**
     * Reads the contents of all non-directory URLs immediately under the specified
     * location and returns them in a map keyed by the file name.
     * <p/>
     * Any URLs that cannot be read will cause an exception to be thrown.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/serializables/one
     * META-INF/serializables/two
     * META-INF/serializables/three
     * META-INF/serializables/four/foo.txt
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * Map map = finder.mapAvailableStrings("serializables");
     * map.contains("one");  // true
     * map.contains("two");  // true
     * map.contains("three");  // true
     * map.contains("four");  // false
     *
     * @param uri String
     * @return a list of the content of each resource URL found
     * @throws IOException if any of the urls cannot be read
     */
    public Map<String, String> mapAllStrings(final String uri) throws IOException {
        final Map<String, String> strings = new HashMap<String, String>();
        final Map<String, URL> resourcesMap = getResourcesMap(uri);
        for (final Map.Entry<String, URL> entry : resourcesMap.entrySet()) {
            final String name = entry.getKey();
            final URL url = entry.getValue();
            final String value = readContents(url);
            strings.put(name, value);
        }
        return strings;
    }

    /**
     * Reads the contents of all non-directory URLs immediately under the specified
     * location and returns them in a map keyed by the file name.
     * <p/>
     * Individual URLs that cannot be read are skipped and added to the
     * list of 'resourcesNotLoaded'
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/serializables/one
     * META-INF/serializables/two      # not readable
     * META-INF/serializables/three
     * META-INF/serializables/four/foo.txt
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * Map map = finder.mapAvailableStrings("serializables");
     * map.contains("one");  // true
     * map.contains("two");  // false
     * map.contains("three");  // true
     * map.contains("four");  // false
     *
     * @param uri String
     * @return a list of the content of each resource URL found
     * @throws IOException if classLoader.getResources throws an exception
     */
    public Map<String, String> mapAvailableStrings(final String uri) throws IOException {
        resourcesNotLoaded.clear();
        final Map<String, String> strings = new HashMap<String, String>();
        final Map<String, URL> resourcesMap = getResourcesMap(uri);
        for (final Map.Entry<String, URL> entry : resourcesMap.entrySet()) {
            final String name = entry.getKey();
            final URL url = entry.getValue();
            try {
                final String value = readContents(url);
                strings.put(name, value);
            } catch (IOException notAvailable) {
                resourcesNotLoaded.add(url.toExternalForm());
            }
        }
        return strings;
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find Class
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    /**
     * Executes {@link #findString(String)} assuming the contents URL found is the name of
     * a class that should be loaded and returned.
     *
     * @param uri String
     * @return Class
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Class findClass(final String uri) throws IOException, ClassNotFoundException {
        final String className = findString(uri);
        return (Class) classLoader.loadClass(className);
    }

    /**
     * Executes findAllStrings assuming the strings are
     * the names of a classes that should be loaded and returned.
     * <p/>
     * Any URL or class that cannot be loaded will cause an exception to be thrown.
     *
     * @param uri String
     * @return List
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public List<Class> findAllClasses(final String uri) throws IOException, ClassNotFoundException {
        final List<Class> classes = new ArrayList<Class>();
        final List<String> strings = findAllStrings(uri);
        for (final String className : strings) {
            final Class clazz = classLoader.loadClass(className);
            classes.add(clazz);
        }
        return classes;
    }

    /**
     * Executes findAvailableStrings assuming the strings are
     * the names of a classes that should be loaded and returned.
     * <p/>
     * Any class that cannot be loaded will be skipped and placed in the
     * 'resourcesNotLoaded' collection.
     *
     * @param uri String
     * @return List
     * @throws IOException if classLoader.getResources throws an exception
     */
    public List<Class> findAvailableClasses(final String uri) throws IOException {
        resourcesNotLoaded.clear();
        final List<Class> classes = new ArrayList<Class>();
        final List<String> strings = findAvailableStrings(uri);
        for (final String className : strings) {
            try {
                final Class clazz = classLoader.loadClass(className);
                classes.add(clazz);
            } catch (Exception notAvailable) {
                resourcesNotLoaded.add(className);
            }
        }
        return classes;
    }

    /**
     * Executes mapAllStrings assuming the value of each entry in the
     * map is the name of a class that should be loaded.
     * <p/>
     * Any class that cannot be loaded will be cause an exception to be thrown.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/xmlparsers/xerces
     * META-INF/xmlparsers/crimson
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * Map map = finder.mapAvailableStrings("xmlparsers");
     * map.contains("xerces");  // true
     * map.contains("crimson");  // true
     * Class xercesClass = map.get("xerces");
     * Class crimsonClass = map.get("crimson");
     *
     * @param uri String
     * @return Map
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Map<String, Class> mapAllClasses(final String uri) throws IOException, ClassNotFoundException {
        final Map<String, Class> classes = new HashMap<String, Class>();
        final Map<String, String> map = mapAllStrings(uri);
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            final String string = entry.getKey();
            final String className = entry.getValue();
            final Class clazz = classLoader.loadClass(className);
            classes.put(string, clazz);
        }
        return classes;
    }

    /**
     * Executes mapAvailableStrings assuming the value of each entry in the
     * map is the name of a class that should be loaded.
     * <p/>
     * Any class that cannot be loaded will be skipped and placed in the
     * 'resourcesNotLoaded' collection.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/xmlparsers/xerces
     * META-INF/xmlparsers/crimson
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * Map map = finder.mapAvailableStrings("xmlparsers");
     * map.contains("xerces");  // true
     * map.contains("crimson");  // true
     * Class xercesClass = map.get("xerces");
     * Class crimsonClass = map.get("crimson");
     *
     * @param uri String
     * @return Map
     * @throws IOException if classLoader.getResources throws an exception
     */
    public Map<String, Class> mapAvailableClasses(final String uri) throws IOException {
        resourcesNotLoaded.clear();
        final Map<String, Class> classes = new HashMap<String, Class>();
        final Map<String, String> map = mapAvailableStrings(uri);
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            final String string = entry.getKey();
            final String className = entry.getValue();
            try {
                final Class clazz = classLoader.loadClass(className);
                classes.put(string, clazz);
            } catch (Exception notAvailable) {
                resourcesNotLoaded.add(className);
            }
        }
        return classes;
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find Implementation
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    /**
     * Assumes the class specified points to a file in the classpath that contains
     * the name of a class that implements or is a subclass of the specfied class.
     * <p/>
     * Any class that cannot be loaded will be cause an exception to be thrown.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/java.io.InputStream    # contains the classname org.acme.AcmeInputStream
     * META-INF/java.io.OutputStream
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * Class clazz = finder.findImplementation(java.io.InputStream.class);
     * clazz.getName();  // returns "org.acme.AcmeInputStream"
     *
     * @param interfase a superclass or interface
     * @return Class
     * @throws IOException            if the URL cannot be read
     * @throws ClassNotFoundException if the class found is not loadable
     * @throws ClassCastException     if the class found is not assignable to the specified superclass or interface
     */
    public Class findImplementation(final Class interfase) throws IOException, ClassNotFoundException {
        final String className = findString(interfase.getName());
        final Class impl = classLoader.loadClass(className);
        if (!interfase.isAssignableFrom(impl)) {
            throw new ClassCastException("Class not of type: " + interfase.getName());
        }
        return impl;
    }

    /**
     * Assumes the class specified points to a file in the classpath that contains
     * the name of a class that implements or is a subclass of the specfied class.
     * <p/>
     * Any class that cannot be loaded or assigned to the specified interface will be cause
     * an exception to be thrown.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/java.io.InputStream    # contains the classname org.acme.AcmeInputStream
     * META-INF/java.io.InputStream    # contains the classname org.widget.NeatoInputStream
     * META-INF/java.io.InputStream    # contains the classname com.foo.BarInputStream
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * List classes = finder.findAllImplementations(java.io.InputStream.class);
     * classes.contains("org.acme.AcmeInputStream");  // true
     * classes.contains("org.widget.NeatoInputStream");  // true
     * classes.contains("com.foo.BarInputStream");  // true
     *
     * @param interfase a superclass or interface
     * @return List
     * @throws IOException            if the URL cannot be read
     * @throws ClassNotFoundException if the class found is not loadable
     * @throws ClassCastException     if the class found is not assignable to the specified superclass or interface
     */
    public List<Class> findAllImplementations(final Class interfase) throws IOException, ClassNotFoundException {
        final List<Class> implementations = new ArrayList<Class>();
        final List<String> strings = findAllStrings(interfase.getName());
        for (final String className : strings) {
            final Class impl = classLoader.loadClass(className);
            if (!interfase.isAssignableFrom(impl)) {
                throw new ClassCastException("Class not of type: " + interfase.getName());
            }
            implementations.add(impl);
        }
        return implementations;
    }

    /**
     * Assumes the class specified points to a file in the classpath that contains
     * the name of a class that implements or is a subclass of the specfied class.
     * <p/>
     * Any class that cannot be loaded or are not assignable to the specified class will be
     * skipped and placed in the 'resourcesNotLoaded' collection.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/java.io.InputStream    # contains the classname org.acme.AcmeInputStream
     * META-INF/java.io.InputStream    # contains the classname org.widget.NeatoInputStream
     * META-INF/java.io.InputStream    # contains the classname com.foo.BarInputStream
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * List classes = finder.findAllImplementations(java.io.InputStream.class);
     * classes.contains("org.acme.AcmeInputStream");  // true
     * classes.contains("org.widget.NeatoInputStream");  // true
     * classes.contains("com.foo.BarInputStream");  // true
     *
     * @param interfase a superclass or interface
     * @return List
     * @throws IOException if classLoader.getResources throws an exception
     */
    public List<Class> findAvailableImplementations(final Class interfase) throws IOException {
        resourcesNotLoaded.clear();
        final List<Class> implementations = new ArrayList<Class>();
        final List<String> strings = findAvailableStrings(interfase.getName());
        for (final String className : strings) {
            try {
                final Class impl = classLoader.loadClass(className);
                if (interfase.isAssignableFrom(impl)) {
                    implementations.add(impl);
                } else {
                    resourcesNotLoaded.add(className);
                }
            } catch (Exception notAvailable) {
                resourcesNotLoaded.add(className);
            }
        }
        return implementations;
    }

    /**
     * Assumes the class specified points to a directory in the classpath that holds files
     * containing the name of a class that implements or is a subclass of the specfied class.
     * <p/>
     * Any class that cannot be loaded or assigned to the specified interface will be cause
     * an exception to be thrown.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/java.net.URLStreamHandler/jar
     * META-INF/java.net.URLStreamHandler/file
     * META-INF/java.net.URLStreamHandler/http
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * Map map = finder.mapAllImplementations(java.net.URLStreamHandler.class);
     * Class jarUrlHandler = map.get("jar");
     * Class fileUrlHandler = map.get("file");
     * Class httpUrlHandler = map.get("http");
     *
     * @param interfase a superclass or interface
     * @return Map
     * @throws IOException            if the URL cannot be read
     * @throws ClassNotFoundException if the class found is not loadable
     * @throws ClassCastException     if the class found is not assignable to the specified superclass or interface
     */
    public Map<String, Class> mapAllImplementations(final Class interfase) throws IOException, ClassNotFoundException {
        final Map<String, Class> implementations = new HashMap<String, Class>();
        final Map<String, String> map = mapAllStrings(interfase.getName());
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            final String string = entry.getKey();
            final String className = entry.getValue();
            final Class impl = classLoader.loadClass(className);
            if (!interfase.isAssignableFrom(impl)) {
                throw new ClassCastException("Class not of type: " + interfase.getName());
            }
            implementations.put(string, impl);
        }
        return implementations;
    }

    /**
     * Assumes the class specified points to a directory in the classpath that holds files
     * containing the name of a class that implements or is a subclass of the specfied class.
     * <p/>
     * Any class that cannot be loaded or are not assignable to the specified class will be
     * skipped and placed in the 'resourcesNotLoaded' collection.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/java.net.URLStreamHandler/jar
     * META-INF/java.net.URLStreamHandler/file
     * META-INF/java.net.URLStreamHandler/http
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * Map map = finder.mapAllImplementations(java.net.URLStreamHandler.class);
     * Class jarUrlHandler = map.get("jar");
     * Class fileUrlHandler = map.get("file");
     * Class httpUrlHandler = map.get("http");
     *
     * @param interfase a superclass or interface
     * @return Map
     * @throws IOException if classLoader.getResources throws an exception
     */
    public Map<String, Class> mapAvailableImplementations(final Class interfase) throws IOException {
        resourcesNotLoaded.clear();
        final Map<String, Class> implementations = new HashMap<String, Class>();
        final Map<String, String> map = mapAvailableStrings(interfase.getName());
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            final String string = entry.getKey();
            final String className = entry.getValue();
            try {
                final Class impl = classLoader.loadClass(className);
                if (interfase.isAssignableFrom(impl)) {
                    implementations.put(string, impl);
                } else {
                    resourcesNotLoaded.add(className);
                }
            } catch (Exception notAvailable) {
                resourcesNotLoaded.add(className);
            }
        }
        return implementations;
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find Properties
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    /**
     * Finds the corresponding resource and reads it in as a properties file
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/widget.properties
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * Properties widgetProps = finder.findProperties("widget.properties");
     *
     * @param uri String
     * @return Properties
     * @throws IOException if the URL cannot be read or is not in properties file format
     */
    public Properties findProperties(final String uri) throws IOException {
        final String fulluri = path + uri;

        final URL resource = getResource(fulluri);
        if (resource == null) {
            throw new IOException("Could not find command in : " + fulluri);
        }

        return loadProperties(resource);
    }

    /**
     * Finds the corresponding resources and reads them in as a properties files
     * <p/>
     * Any URL that cannot be read in as a properties file will cause an exception to be thrown.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/app.properties
     * META-INF/app.properties
     * META-INF/app.properties
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * List<Properties> appProps = finder.findAllProperties("app.properties");
     *
     * @param uri String
     * @return List
     * @throws IOException if the URL cannot be read or is not in properties file format
     */
    public List<Properties> findAllProperties(final String uri) throws IOException {
        final String fulluri = path + uri;

        final List<Properties> properties = new ArrayList<Properties>();

        final Enumeration<URL> resources = getResources(fulluri);
        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            final Properties props = loadProperties(url);
            properties.add(props);
        }
        return properties;
    }

    /**
     * Finds the corresponding resources and reads them in as a properties files
     * <p/>
     * Any URL that cannot be read in as a properties file will be added to the
     * 'resourcesNotLoaded' collection.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/app.properties
     * META-INF/app.properties
     * META-INF/app.properties
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * List<Properties> appProps = finder.findAvailableProperties("app.properties");
     *
     * @param uri String
     * @return List
     * @throws IOException if classLoader.getResources throws an exception
     */
    public List<Properties> findAvailableProperties(final String uri) throws IOException {
        resourcesNotLoaded.clear();
        final String fulluri = path + uri;

        final List<Properties> properties = new ArrayList<Properties>();

        final Enumeration<URL> resources = getResources(fulluri);
        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            try {
                final Properties props = loadProperties(url);
                properties.add(props);
            } catch (Exception notAvailable) {
                resourcesNotLoaded.add(url.toExternalForm());
            }
        }
        return properties;
    }

    /**
     * Finds the corresponding resources and reads them in as a properties files
     * <p/>
     * Any URL that cannot be read in as a properties file will cause an exception to be thrown.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/jdbcDrivers/oracle.properties
     * META-INF/jdbcDrivers/mysql.props
     * META-INF/jdbcDrivers/derby
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * List<Properties> driversList = finder.findAvailableProperties("jdbcDrivers");
     * Properties oracleProps = driversList.get("oracle.properties");
     * Properties mysqlProps = driversList.get("mysql.props");
     * Properties derbyProps = driversList.get("derby");
     *
     * @param uri String
     * @return Map
     * @throws IOException if the URL cannot be read or is not in properties file format
     */
    public Map<String, Properties> mapAllProperties(final String uri) throws IOException {
        final Map<String, Properties> propertiesMap = new HashMap<String, Properties>();
        final Map<String, URL> map = getResourcesMap(uri);
        for (final Map.Entry<String, URL> entry : map.entrySet()) {
            final String string = entry.getKey();
            final URL url = entry.getValue();
            final Properties properties = loadProperties(url);
            propertiesMap.put(string, properties);
        }
        return propertiesMap;
    }

    /**
     * Finds the corresponding resources and reads them in as a properties files
     * <p/>
     * Any URL that cannot be read in as a properties file will be added to the
     * 'resourcesNotLoaded' collection.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/jdbcDrivers/oracle.properties
     * META-INF/jdbcDrivers/mysql.props
     * META-INF/jdbcDrivers/derby
     * <p/>
     * ResourceFinder finder = new ResourceFinder("META-INF/");
     * List<Properties> driversList = finder.findAvailableProperties("jdbcDrivers");
     * Properties oracleProps = driversList.get("oracle.properties");
     * Properties mysqlProps = driversList.get("mysql.props");
     * Properties derbyProps = driversList.get("derby");
     *
     * @param uri String
     * @return Map
     * @throws IOException if classLoader.getResources throws an exception
     */
    public Map<String, Properties> mapAvailableProperties(final String uri) throws IOException {
        resourcesNotLoaded.clear();
        final Map<String, Properties> propertiesMap = new HashMap<String, Properties>();
        final Map<String, URL> map = getResourcesMap(uri);
        for (final Map.Entry<String, URL> entry : map.entrySet()) {

            final String string = entry.getKey();
            final URL url = entry.getValue();

            try {
                final Properties properties = loadProperties(url);
                propertiesMap.put(string, properties);
            } catch (Exception notAvailable) {
                resourcesNotLoaded.add(url.toExternalForm());
            }
        }
        return propertiesMap;
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Map Resources
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public Map<String, URL> getResourcesMap(final String uri) throws IOException {
        String basePath = path + uri;

        final Map<String, URL> resources = new HashMap<String, URL>();
        if (!basePath.endsWith("/")) {
            basePath += "/";
        }
        final Enumeration<URL> urls = getResources(basePath);

        while (urls.hasMoreElements()) {
            final URL location = urls.nextElement();

            try {
                if (location.getProtocol().equals("jar")) {

                    readJarEntries(location, basePath, resources);

                } else if (location.getProtocol().equals("file")) {

                    readDirectoryEntries(location, resources);

                }
            } catch (Exception e) {
                //Ignore
            }
        }

        return resources;
    }

    @SuppressWarnings("deprecation")
    private static void readDirectoryEntries(final URL location, final Map<String, URL> resources) throws MalformedURLException {

        File dir;
        try {
            dir = new File(URLDecoder.decode(location.getPath(), "UTF-8"));
        } catch (Exception e) {
            dir = new File(URLDecoder.decode(location.getPath()));
        }

        if (dir.isDirectory()) {
            final File[] files = dir.listFiles();
            if (files != null) {
                for (final File file : files) {
                    if (!file.isDirectory()) {
                        final String name = file.getName();
                        final URL url = file.toURI().toURL();
                        resources.put(name, url);
                    }
                }
            }
        }
    }

    private static void readJarEntries(final URL location, final String basePath, final Map<String, URL> resources) throws IOException {
        final JarURLConnection conn = (JarURLConnection) location.openConnection();
        final JarFile jarfile = conn.getJarFile();

        final Enumeration<JarEntry> entries = jarfile.entries();
        while (entries != null && entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            String name = entry.getName();

            if (entry.isDirectory() || !name.startsWith(basePath) || name.length() == basePath.length()) {
                continue;
            }

            name = name.substring(basePath.length());

            if (name.contains("/")) {
                continue;
            }

            final URL resource = new URL(location, name);
            resources.put(name, resource);
        }
    }

    private Properties loadProperties(final URL resource) throws IOException {

        BufferedInputStream reader = null;

        try {
            reader = new BufferedInputStream(resource.openStream());
            final Properties properties = new Properties();
            properties.load(reader);

            return properties;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }
        }
    }

    private String readContents(final URL resource) throws IOException {
        BufferedInputStream reader = null;
        final StringBuilder sb = new StringBuilder();

        try {
            reader = new BufferedInputStream(resource.openStream());

            int b = reader.read();
            while (b != -1) {
                sb.append((char) b);
                b = reader.read();
            }

            return sb.toString().trim();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }
        }
    }

    private URL getResource(final String fullUri) {
        if (urls == null) {
            return classLoader.getResource(fullUri);
        }
        return findResource(fullUri, urls);
    }

    private Enumeration<URL> getResources(final String fulluri) throws IOException {
        if (urls == null) {
            return classLoader.getResources(fulluri);
        }
        //noinspection UseOfObsoleteCollectionType
        final Vector<URL> resources = new Vector();
        for (final URL url : urls) {
            final URL resource = findResource(fulluri, url);
            if (resource != null) {
                resources.add(resource);
            }
        }
        return resources.elements();
    }

    private URL findResource(final String resourceName, final URL... search) {
        for (int i = 0; i < search.length; i++) {
            final URL currentUrl = search[i];
            if (currentUrl == null) {
                continue;
            }

            try {
                final String protocol = currentUrl.getProtocol();
                if (protocol.equals("jar")) {
                    /*
                    * If the connection for currentUrl or resURL is
                    * used, getJarFile() will throw an exception if the
                    * entry doesn't exist.
                    */
                    final URL jarURL = ((JarURLConnection) currentUrl.openConnection()).getJarFileURL();
                    final JarFile jarFile;
                    try {
                        final JarURLConnection juc = (JarURLConnection) new URL("jar", "", jarURL.toExternalForm() + "!/").openConnection();
                        jarFile = juc.getJarFile();
                    } catch (IOException e) {
                        // Don't look for this jar file again
                        search[i] = null;
                        throw e;
                    }

                    final String entryName;
                    if (currentUrl.getFile().endsWith("!/")) {
                        entryName = resourceName;
                    } else {
                        final String file = currentUrl.getFile();
                        int sepIdx = file.lastIndexOf("!/");
                        if (sepIdx == -1) {
                            // Invalid URL, don't look here again
                            search[i] = null;
                            continue;
                        }
                        sepIdx += 2;
                        final StringBuilder sb = new StringBuilder(file.length() - sepIdx + resourceName.length());
                        sb.append(file.substring(sepIdx));
                        sb.append(resourceName);
                        entryName = sb.toString();
                    }
                    if (entryName.equals("META-INF/") && jarFile.getEntry("META-INF/MANIFEST.MF") != null) {
                        return targetURL(currentUrl, "META-INF/MANIFEST.MF");
                    }
                    if (jarFile.getEntry(entryName) != null) {
                        return targetURL(currentUrl, resourceName);
                    }
                } else if (protocol.equals("file")) {
                    final String baseFile = currentUrl.getFile();
                    final String host = currentUrl.getHost();
                    int hostLength = 0;
                    if (host != null) {
                        hostLength = host.length();
                    }
                    final StringBuilder buf = new StringBuilder(2 + hostLength + baseFile.length() + resourceName.length());

                    if (hostLength > 0) {
                        buf.append("//").append(host);
                    }
                    // baseFile always ends with '/'
                    buf.append(baseFile);
                    String fixedResName = resourceName;
                    // Do not create a UNC path, i.e. \\host
                    while (fixedResName.startsWith("/") || fixedResName.startsWith("\\")) {
                        fixedResName = fixedResName.substring(1);
                    }
                    buf.append(fixedResName);
                    final String filename = buf.toString();
                    final File file = new File(filename);
                    File file2;
                    try {
                        file2 = new File(URLDecoder.decode(filename, "UTF-8"));
                    } catch (Exception e) {
                        //noinspection deprecation
                        file2 = new File(URLDecoder.decode(filename));
                    }

                    if (file.exists() || file2.exists()) {
                        return targetURL(currentUrl, fixedResName);
                    }
                } else {
                    final URL resourceURL = targetURL(currentUrl, resourceName);
                    final URLConnection urlConnection = resourceURL.openConnection();

                    try {
                        urlConnection.getInputStream().close();
                    } catch (SecurityException e) {
                        return null;
                    }
                    // HTTP can return a stream on a non-existent file
                    // So check for the return code;
                    if (!resourceURL.getProtocol().equals("http")) {
                        return resourceURL;
                    }

                    final int code = ((HttpURLConnection) urlConnection).getResponseCode();
                    if (code >= 200 && code < 300) {
                        return resourceURL;
                    }
                }
            } catch (SecurityException | IOException e) {
                // Keep iterating through the URL list
            }
        }
        return null;
    }

    private static URL targetURL(final URL base, final String name) throws MalformedURLException {
        final StringBuilder sb = new StringBuilder(base.getFile().length() + name.length());
        sb.append(base.getFile());
        sb.append(name);
        final String file = sb.toString();
        return new URL(base.getProtocol(), base.getHost(), base.getPort(), file, null);
    }
}
