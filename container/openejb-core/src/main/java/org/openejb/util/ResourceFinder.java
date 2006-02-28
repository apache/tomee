package org.openejb.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceFinder {

    private final String path;
    private final ClassLoader classLoader;

    public ResourceFinder(String path) {
        this(path, Thread.currentThread().getContextClassLoader());
    }

    public ResourceFinder(String path, ClassLoader classLoader) {
        this.path = path;
        this.classLoader = classLoader;
    }


    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find String
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public String findString(String key) throws IOException {
        String uri = path + key;

        URL resource = classLoader.getResource(uri);
        if (resource == null) {
            throw new IOException("Could not find command in : " + uri);
        }

        return readContents(resource);
    }

    public List<String> findAllStrings(String key) throws IOException {
        String uri = path + key;

        List<String> strings = new ArrayList<String>();

        Enumeration<URL> resources = classLoader.getResources(uri);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            String string = readContents(url);
            strings.add(string);
        }
        return strings;
    }

    public List<String> findAvailableStrings(String key) throws IOException {
        String uri = path + key;

        List<String> strings = new ArrayList<String>();

        Enumeration<URL> resources = classLoader.getResources(uri);
        while (resources.hasMoreElements()) {
            try {
                URL url = resources.nextElement();
                String string = readContents(url);
                strings.add(string);
            } catch (Exception notAvailable) {
            }
        }
        return strings;
    }

    public Map<String,String> mapAllStrings(String key) throws IOException {
        Map<String,String> strings = new HashMap<String,String>();
        Map<String, URL> resourcesMap = getResourcesMap(key);
        for (Iterator iterator = resourcesMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            URL url = (URL) entry.getValue();
            String value = readContents(url);
            strings.put(name,value);
        }
        return strings;
    }

    public Map<String,String> mapAvailableStrings(String key) throws IOException {
        Map<String,String> strings = new HashMap<String,String>();
        Map<String, URL> resourcesMap = getResourcesMap(key);
        for (Iterator iterator = resourcesMap.entrySet().iterator(); iterator.hasNext();) {
            try {
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                URL url = (URL) entry.getValue();
                String value = readContents(url);
                strings.put(name,value);
            } catch (Exception notAvailable) {
            }
        }
        return strings;
    }


    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find Class
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public Class findClass(String key) throws IOException, ClassNotFoundException {
        String className = findString(key);
        Class clazz = classLoader.loadClass(className);
        return clazz;
    }

    public List<Class> findAllClasses(String key) throws IOException, ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        List<String> strings = findAllStrings(key);
        for (String className : strings) {
            Class clazz = classLoader.loadClass(className);
            classes.add(clazz);
        }
        return classes;
    }

    public List<Class> findAvailableClasses(String key) throws IOException {
        List<Class> classes = new ArrayList<Class>();
        List<String> strings = findAvailableStrings(key);
        for (String className : strings) {
            try {
                Class clazz = classLoader.loadClass(className);
                classes.add(clazz);
            } catch (Exception notAvailable) {
            }
        }
        return classes;
    }

    public Map<String, Class> mapAllClasses(String key) throws IOException, ClassNotFoundException {
        Map<String, Class> classes = new HashMap<String, Class>();
        Map<String, String> map = mapAllStrings(key);
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String string = (String) entry.getKey();
            String className = (String) entry.getValue();
            Class clazz = classLoader.loadClass(className);
            classes.put(string, clazz);
        }
        return classes;
    }

    public Map<String, Class> mapAvailableClasses(String key) throws IOException {
        Map<String, Class> classes = new HashMap<String, Class>();
        Map<String, String> map = mapAvailableStrings(key);
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            try {
                Map.Entry entry = (Map.Entry) iterator.next();
                String string = (String) entry.getKey();
                String className = (String) entry.getValue();
                Class clazz = classLoader.loadClass(className);
                classes.put(string, clazz);
            } catch (Exception notAvailable) {
            }
        }
        return classes;
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find Implementation
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public Class findImplementation(Class interfase) throws IOException, ClassNotFoundException {
        String className = findString(interfase.getName());
        Class impl = classLoader.loadClass(className);
        if (!interfase.isAssignableFrom(impl)) {
            throw new ClassCastException("Class not of type: " + interfase.getName());
        }
        return impl;
    }

    public List<Class> findAllImplementations(Class interfase) throws IOException, ClassNotFoundException {
        List<Class> implementations = new ArrayList<Class>();
        List<String> strings = findAllStrings(interfase.getName());
        for (String className : strings) {
            Class impl = classLoader.loadClass(className);
            if (!interfase.isAssignableFrom(impl)) {
                throw new ClassCastException("Class not of type: " + interfase.getName());
            }
            implementations.add(impl);
        }
        return implementations;
    }

    public List<Class> findAvailableImplementations(Class interfase) throws IOException {
        List<Class> implementations = new ArrayList<Class>();
        List<String> strings = findAvailableStrings(interfase.getName());
        for (String className : strings) {
            try {
                Class impl = classLoader.loadClass(className);
                if (interfase.isAssignableFrom(impl)) {
                    implementations.add(impl);
                }
            } catch (Exception notAvailable) {
            }
        }
        return implementations;
    }

    public Map<String, Class> mapAllImplementations(Class interfase) throws IOException, ClassNotFoundException {
        Map<String, Class> implementations = new HashMap<String, Class>();
        Map<String, String> map = mapAllStrings(interfase.getName());
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String string = (String) entry.getKey();
            String className = (String) entry.getValue();
            Class impl = classLoader.loadClass(className);
            if (!interfase.isAssignableFrom(impl)) {
                throw new ClassCastException("Class not of type: " + interfase.getName());
            }
            implementations.put(string, impl);
        }
        return implementations;
    }

    public Map<String, Class> mapAvailableImplementations(Class interfase) throws IOException {
        Map<String, Class> implementations = new HashMap<String, Class>();
        Map<String, String> map = mapAvailableStrings(interfase.getName());
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            try {
                Map.Entry entry = (Map.Entry) iterator.next();
                String string = (String) entry.getKey();
                String className = (String) entry.getValue();
                Class impl = classLoader.loadClass(className);
                if (interfase.isAssignableFrom(impl)) {
                    implementations.put(string, impl);
                }
            } catch (Exception notAvailable) {
            }
        }
        return implementations;
    }


    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find Properties
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public Properties findProperties(String key) throws IOException {
        String uri = path + key;

        URL resource = classLoader.getResource(uri);
        if (resource == null) {
            throw new IOException("Could not find command in : " + uri);
        }

        return loadProperties(resource);
    }

    public List<Properties> findAllProperties(String key) throws IOException {
        String uri = path + key;

        List<Properties> properties = new ArrayList<Properties>();

        Enumeration<URL> resources = classLoader.getResources(uri);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            Properties props = loadProperties(url);
            properties.add(props);
        }
        return properties;
    }

    public List<Properties> findAvailableProperties(String key) throws IOException {
        String uri = path + key;

        List<Properties> properties = new ArrayList<Properties>();

        Enumeration<URL> resources = classLoader.getResources(uri);
        while (resources.hasMoreElements()) {
            try {
                URL url = resources.nextElement();
                Properties props = loadProperties(url);
                properties.add(props);
            } catch (Exception notAvailable) {
            }
        }
        return properties;
    }

    public Map<String, Properties> mapAllProperties(String key) throws IOException {
        Map<String, Properties> propertiesMap = new HashMap<String, Properties>();
        Map<String, URL> map = getResourcesMap(key);
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String string = (String) entry.getKey();
            URL url = (URL) entry.getValue();
            Properties properties = loadProperties(url);
            propertiesMap.put(string, properties);
        }
        return propertiesMap;
    }

    public Map<String, Properties> mapAvailableProperties(String key) throws IOException {
        Map<String, Properties> propertiesMap = new HashMap<String, Properties>();
        Map<String, URL> map = getResourcesMap(key);
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            try {
                Map.Entry entry = (Map.Entry) iterator.next();
                String string = (String) entry.getKey();
                URL url = (URL) entry.getValue();
                Properties properties = loadProperties(url);
                propertiesMap.put(string, properties);
            } catch (Exception notAvailable) {
            }
        }
        return propertiesMap;
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Map Resources
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public Map<String, URL> getResourcesMap(String key) throws IOException {
        String basePath = path + key;

        Map<String, URL> resources = new HashMap<String, URL>();
        if (!basePath.endsWith("/")){
            basePath += "/";
        }
        Enumeration<URL> urls = classLoader.getResources(basePath);

        while (urls.hasMoreElements()) {
            URL location = urls.nextElement();

            try {
                if (location.getProtocol().equals("jar")) {

                    readJarEntries(location, basePath, resources);

                } else if (location.getProtocol().equals("file")) {

                    readDirectoryEntries(location, resources);

                }
            } catch (Exception e) {
            }
        }

        return resources;
    }

    private static void readDirectoryEntries(URL location, Map<String, URL> resources) throws MalformedURLException {
        File dir = new File(location.getPath());
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (!file.isDirectory()){
                    String name = file.getName();
                    URL url = file.toURL();
                    resources.put(name, url);
                }
            }
        }
    }

    private static void readJarEntries(URL location, String basePath, Map<String, URL> resources) throws IOException {
        JarURLConnection conn = (JarURLConnection) location.openConnection();
        JarFile jarfile = conn.getJarFile();

        Enumeration<JarEntry> entries = jarfile.entries();
        while (entries != null && entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();

            if (entry.isDirectory() || !name.startsWith(basePath) || name.length() == basePath.length()) {
                continue;
            }

            name = name.substring(basePath.length());

            if (name.contains("/")) {
                continue;
            }

            URL resource = new URL(location, name);
            resources.put(name, resource);
        }
    }

    private Properties loadProperties(URL resource) throws IOException {
        InputStream in = resource.openStream();

        BufferedInputStream reader = null;
        try {
            reader = new BufferedInputStream(in);
            Properties properties = new Properties();
            properties.load(reader);

            return properties;
        } finally {
            try {
                in.close();
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    private String readContents(URL resource) throws IOException {
        InputStream in = resource.openStream();
        BufferedInputStream reader = null;
        StringBuffer sb = new StringBuffer();

        try {
            reader = new BufferedInputStream(in);

            int b = reader.read();
            while (b != -1) {
                sb.append((char) b);
                b = reader.read();
            }

            return sb.toString().trim();
        } finally {
            try {
                in.close();
                reader.close();
            } catch (Exception e) {
            }
        }
    }


    public Enumeration doFindCommands() throws IOException {
        return Thread.currentThread().getContextClassLoader().getResources(path);
    }

}