package org.openejb.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class ResourceFinder {

    private final String path;

    public ResourceFinder(String path) {
        this.path = path;
    }

    public Properties findProperties(String key) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return findProperties(key, classLoader);
    }

    public Properties findProperties(String key, ClassLoader classLoader) throws IOException {
        String uri = path + key;

        URL resource = classLoader.getResource(uri);
        if (resource == null) {
            throw new IOException("Could not find command in : " + uri);
        }

        return loadProperties(resource);
    }

    public List<Properties> findAllProperties(String key) throws IOException {
        return findAllProperties(key, Thread.currentThread().getContextClassLoader());
    }

    public List<Properties> findAllProperties(String key, ClassLoader classLoader) throws IOException {
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
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return findAvailableProperties(key, classLoader);
    }

    public List<Properties> findAvailableProperties(String key, ClassLoader classLoader) throws IOException {
        String uri = path + key;

        List<Properties> properties = new ArrayList<Properties>();

        Enumeration<URL> resources = classLoader.getResources(uri);
        while (resources.hasMoreElements()) {
            try {
                URL url = resources.nextElement();
                Properties props = loadProperties(url);
                properties.add(props);
            } catch (IOException dontCare) {
            }
        }
        return properties;
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
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    public String findString(String key) throws IOException {
        return findString(key, Thread.currentThread().getContextClassLoader());
    }

    public String findString(String key, ClassLoader classLoader) throws IOException {
        String uri = path + key;

        URL resource = classLoader.getResource(uri);
        if (resource == null) {
            throw new IOException("Could not find command in : " + uri);
        }

        return readContents(resource);
    }


    public List<String> findAllStrings(String key) throws IOException {
        return findAllStrings(key, Thread.currentThread().getContextClassLoader());
    }

    public List<String> findAllStrings(String key, ClassLoader classLoader) throws IOException {
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
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return findAvailableStrings(key, classLoader);
    }

    public List<String> findAvailableStrings(String key, ClassLoader classLoader) throws IOException {
        String uri = path + key;

        List<String> strings = new ArrayList<String>();

        Enumeration<URL> resources = classLoader.getResources(uri);
        while (resources.hasMoreElements()) {
            try {
                URL url = resources.nextElement();
                String string = readContents(url);
                strings.add(string);
            } catch (IOException dontCare) {
            }
        }
        return strings;
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
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    public List<Class> findAvailableClasses(String key) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return findAvailableClasses(key, classLoader);
    }

    public List<Class> findAvailableClasses(String key, ClassLoader classLoader) throws IOException {
        List<Class> classes = new ArrayList<Class>();
        List<String> strings = findAvailableStrings(key, classLoader);
        for (String className : strings) {
            try {
                Class clazz = classLoader.loadClass(className);
                classes.add(clazz);
            } catch (ClassNotFoundException dontCare) {
            }
        }
        return classes;
    }

    public List<Class> findAllClasses(String key) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return findAllClasses(key, classLoader);
    }

    public List<Class> findAllClasses(String key, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        List<String> strings = findAllStrings(key, classLoader);
        for (String className : strings) {
            Class clazz = classLoader.loadClass(className);
            classes.add(clazz);
        }
        return classes;
    }

    public Class findClass(String key) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return findClass(key, classLoader);
    }

    public Class findClass(String key, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        String className = findString(key, classLoader);
        Class clazz = classLoader.loadClass(className);
        return clazz;
    }



    public List<Class> findAvailableImplementations(Class interfase) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return findAvailableImplementations(interfase, classLoader);
    }

    public List<Class> findAvailableImplementations(Class interfase, ClassLoader classLoader) throws IOException {
        List<Class> classes = new ArrayList<Class>();
        List<String> strings = findAvailableStrings(interfase.getName(), classLoader);
        for (String className : strings) {
            try {
                Class impl = classLoader.loadClass(className);
                if (interfase.isAssignableFrom(impl)) {
                    classes.add(impl);
                }
            } catch (ClassNotFoundException dontCare) {
            }
        }
        return classes;
    }

    public List<Class> findAllImplementations(Class interfase) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return findAllImplementations(interfase, classLoader);
    }

    public List<Class> findAllImplementations(Class interfase, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        List<String> strings = findAllStrings(interfase.getName(), classLoader);
        for (String className : strings) {
            Class impl = classLoader.loadClass(className);
            if (!interfase.isAssignableFrom(impl)) {
                throw new ClassCastException("Class not of type: " + interfase.getName());
            }
            classes.add(impl);
        }
        return classes;
    }

    public Class findImplementation(Class interfase) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return findImplementation(interfase, classLoader);
    }

    public Class findImplementation(Class interfase, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        String className = findString(interfase.getName(), classLoader);
        Class impl = classLoader.loadClass(className);
        if (!interfase.isAssignableFrom(impl)) {
            throw new ClassCastException("Class not of type: " + interfase.getName());
        }
        return impl;
    }

    public Enumeration doFindCommands() throws IOException {
        return Thread.currentThread().getContextClassLoader().getResources(path);
    }
}