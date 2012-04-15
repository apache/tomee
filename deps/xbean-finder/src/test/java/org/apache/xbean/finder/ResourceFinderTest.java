/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.xbean.finder;

/**
 * @version $Rev$ $Date$
 */

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.TestCase;
import org.acme.BarUrlHandler;
import org.acme.FooUrlHandler;
import org.acme.One;
import org.acme.Three;
import org.acme.Two;
import org.acme.javaURLContextFactory;
import org.acme.kernelURLContextFactory;
import org.acme.ldapURLContextFactory;
import org.apache.xbean.finder.archive.Archives;

public class ResourceFinderTest extends TestCase {
    ResourceFinder resourceFinder = new ResourceFinder("META-INF/");

    public void testGetResourcesMap1() throws Exception {
        Map<String, URL> resourcesMap = resourceFinder.getResourcesMap("");
        Set<Map.Entry<String, URL>> entries = resourcesMap.entrySet();
        for (Map.Entry<String, URL> entry : entries) {
            String key = entry.getKey();
            URL value = entry.getValue();

            assertTrue("key not a directory", !key.contains("/"));
            assertTrue("contains META-INF/", value.getPath().contains("META-INF/"));
            assertTrue("ends with META-INF/" + key, value.getPath().endsWith("META-INF/" + key));
            assertTrue("value not a directory", !value.getPath().endsWith("/"));
        }
    }

    public void testGetResourcesMap2() throws Exception {
        String token = "tvshows";
        Map<String, URL> resourcesMap = resourceFinder.getResourcesMap(token);
        Set<Map.Entry<String, URL>> entries = resourcesMap.entrySet();
        for (Map.Entry<String, URL> entry : entries) {
            String key = entry.getKey();
            URL value = entry.getValue();

            assertTrue("key not a directory", !key.contains("/"));
            assertTrue("contains META-INF/", value.getPath().contains("META-INF/"));
            assertTrue("ends with META-INF/" + token + "/" + key, value.getPath().endsWith("META-INF/" + token + "/" + key));
            assertTrue("value not a directory", !value.getPath().endsWith("/"));
        }

        assertTrue("map contains simpsons.properties", resourcesMap.containsKey("simpsons.properties"));
        assertTrue("map contains familyguy.properties", resourcesMap.containsKey("familyguy.properties"));
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find String
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public void testFindString() throws Exception {
        String expected = One.class.getName();
        String actual = resourceFinder.findString("java.io.Serializable");
        assertEquals(expected, actual);
    }

    public void testFindAllStrings() throws Exception {
        List<String> manifests;
        try {
            manifests = resourceFinder.findAllStrings("MANIFEST.MF");
        } catch (Exception thisIsLegal) {
            return;
        }

        assertTrue("manifests found", manifests.size() > 1);
        for (String manifest : manifests) {
            assertTrue("starts with 'Manifest-Version'", manifest.startsWith("Manifest-Version"));
        }
    }

    public void testFindAvailableStrings() throws Exception {
        List<String> manifests = resourceFinder.findAvailableStrings("MANIFEST.MF");

        assertTrue("manifests found", manifests.size() > 1);
        for (String manifest : manifests) {
            assertTrue("starts with 'Manifest-Version'", manifest.startsWith("Manifest-Version"));
        }
    }

    public void testMapAllStrings() throws Exception {
        Map<String, String> resourcesMap = resourceFinder.mapAllStrings("serializables");

        assertEquals("map size", 3, resourcesMap.size());
        assertTrue("map contains key 'one'", resourcesMap.containsKey("one"));
        assertEquals(One.class.getName(), resourcesMap.get("one"));

        assertTrue("map contains key 'two'", resourcesMap.containsKey("two"));
        assertEquals(Two.class.getName(), resourcesMap.get("two"));

        assertTrue("map contains key 'three'", resourcesMap.containsKey("three"));
        assertEquals(Three.class.getName(), resourcesMap.get("three"));
    }

    public void testMapAvailableStrings() throws Exception {
        Map<String, String> resourcesMap = resourceFinder.mapAvailableStrings("serializables");

        assertEquals("map size", 3, resourcesMap.size());
        assertTrue("map contains key 'one'", resourcesMap.containsKey("one"));
        assertEquals(One.class.getName(), resourcesMap.get("one"));

        assertTrue("map contains key 'two'", resourcesMap.containsKey("two"));
        assertEquals(Two.class.getName(), resourcesMap.get("two"));

        assertTrue("map contains key 'three'", resourcesMap.containsKey("three"));
        assertEquals(Three.class.getName(), resourcesMap.get("three"));
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find Class
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public void testFindClass() throws Exception {
        Class actual = resourceFinder.findClass("java.io.Serializable");
        assertEquals(One.class, actual);

        try {
            resourceFinder.findClass("java.io.OutputStream");
            fail("ClassNotFoundException should be thrown");
        } catch (ClassNotFoundException success) {
            // pass
        } catch (Exception e) {
            fail("Wrong exception type was thrown: " + e.getClass().getName());
        }
    }

    public void testFindAllClasses() throws Exception {
        List<Class<?>> classes = resourceFinder.findAllClasses("java.io.Serializable");
        assertEquals("size", 1, classes.size());
        assertEquals(One.class, classes.get(0));

        try {
            resourceFinder.findAllClasses("java.io.OutputStream");
            fail("ClassNotFoundException should be thrown");
        } catch (ClassNotFoundException success) {
            // pass
        } catch (Exception e) {
            fail("Wrong exception type was thrown: " + e.getClass().getName());
        }
    }

    public void testFindAvailableClasses() throws Exception {
        List<Class<?>> classes = resourceFinder.findAvailableClasses("java.io.Serializable");
        assertEquals("size", 1, classes.size());
        assertEquals(One.class, classes.get(0));

        classes = resourceFinder.findAvailableClasses("java.io.OutputStream");
        assertEquals("size", 0, classes.size());
    }

    public void testMapAllClasses() throws Exception {
        Map<String, Class<?>> resourcesMap = resourceFinder.mapAllClasses("serializables");

        assertEquals("map size", 3, resourcesMap.size());
        assertTrue("map contains key 'one'", resourcesMap.containsKey("one"));
        assertEquals(One.class, resourcesMap.get("one"));

        assertTrue("map contains key 'two'", resourcesMap.containsKey("two"));
        assertEquals(Two.class, resourcesMap.get("two"));

        assertTrue("map contains key 'three'", resourcesMap.containsKey("three"));
        assertEquals(Three.class, resourcesMap.get("three"));

        try {
            resourceFinder.mapAllClasses("externalizables");
            fail("ClassNotFoundException should be thrown");
        } catch (ClassNotFoundException success) {
            // pass
        } catch (Exception e) {
            fail("Wrong exception type was thrown: " + e.getClass().getName());
        }
    }

    public void testMapAvailableClasses() throws Exception {
        Map<String, Class<?>> resourcesMap = resourceFinder.mapAvailableClasses("externalizables");

        assertEquals("map size", 2, resourcesMap.size());
        assertTrue("map contains key 'one'", resourcesMap.containsKey("one"));
        assertEquals(One.class, resourcesMap.get("one"));

        assertTrue("map contains key 'two'", resourcesMap.containsKey("two"));
        assertEquals(Two.class, resourcesMap.get("two"));
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find Implementation
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public void testFindImplementation() throws Exception {
        Class expected = One.class;
        Class actual = resourceFinder.findImplementation(java.io.Serializable.class);
        assertEquals(expected, actual);

        try {
            resourceFinder.findImplementation(java.io.InputStream.class);
            fail("ClassCastException should be thrown");
        } catch (ClassCastException success) {
        } catch (Exception e) {
            fail("Wrong exception type was thrown: " + e.getClass().getName());
        }
    }

    public void testFindAllImplementations() throws Exception {
        List<Class<? extends java.io.Serializable>> classes = resourceFinder.findAllImplementations(java.io.Serializable.class);
        assertEquals("size", 1, classes.size());
        assertEquals(One.class, classes.get(0));

        try {
            resourceFinder.findAllImplementations(java.io.InputStream.class);
            fail("ClassNotFoundException should be thrown");
        } catch (ClassCastException success) {
        } catch (Exception e) {
            fail("Wrong exception type was thrown: " + e.getClass().getName());
        }
    }

    public void testMapAllImplementations() throws Exception {
        Map<String, Class<? extends javax.naming.spi.ObjectFactory>> resourcesMap = resourceFinder.mapAllImplementations(javax.naming.spi.ObjectFactory.class);

        assertEquals("map size", 3, resourcesMap.size());
        assertTrue("map contains key 'java'", resourcesMap.containsKey("java"));
        assertEquals(javaURLContextFactory.class, resourcesMap.get("java"));

        assertTrue("map contains key 'kernel'", resourcesMap.containsKey("kernel"));
        assertEquals(kernelURLContextFactory.class, resourcesMap.get("kernel"));

        assertTrue("map contains key 'ldap'", resourcesMap.containsKey("ldap"));
        assertEquals(ldapURLContextFactory.class, resourcesMap.get("ldap"));

        try {
            resourceFinder.mapAllImplementations(java.net.URLStreamHandler.class);
            fail("ClassNotFoundException should be thrown");
        } catch (ClassCastException success) {
            // pass
        } catch (Exception e) {
            fail("Wrong exception type was thrown: " + e.getClass().getName());
        }
    }

    public void testMapAvailableImplementations() throws Exception {
        Map<String, Class<? extends java.net.URLStreamHandler>> resourcesMap = resourceFinder.mapAvailableImplementations(java.net.URLStreamHandler.class);

        assertEquals("map size", 2, resourcesMap.size());
        assertTrue("map contains key 'bar'", resourcesMap.containsKey("bar"));
        assertEquals(BarUrlHandler.class, resourcesMap.get("bar"));

        assertTrue("map contains key 'foo'", resourcesMap.containsKey("foo"));
        assertEquals(FooUrlHandler.class, resourcesMap.get("foo"));
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find Properties
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public void testFindProperties() throws Exception {
        Properties properties = resourceFinder.findProperties("tvshows/familyguy.properties");
        assertNotNull("properties", properties);
        validateFamilyGuy(properties);

        properties = resourceFinder.findProperties("tvshows/simpsons.properties");
        assertNotNull("properties", properties);
        validateSimpsons(properties);
    }

    public void testFindAllProperties() throws Exception {
        List<Properties> propertiesLists = resourceFinder.findAllProperties("tvshows/familyguy.properties");
        assertNotNull("properties", propertiesLists);
        assertEquals("list size", 1, propertiesLists.size());

        Properties properties = propertiesLists.get(0);
        validateFamilyGuy(properties);
    }

    public void testFindAvailableProperties() throws Exception {
        List<Properties> propertiesLists = resourceFinder.findAvailableProperties("tvshows/familyguy.properties");
        assertNotNull("properties", propertiesLists);
        assertEquals("list size", 1, propertiesLists.size());

        Properties properties = propertiesLists.get(0);
        validateFamilyGuy(properties);
    }

    public void testMapAllProperties() throws Exception {
        Map<String, Properties> propertiesMap = resourceFinder.mapAllProperties("tvshows");
        assertNotNull("properties", propertiesMap);
        assertEquals("map size", 2, propertiesMap.size());

        assertTrue("contains 'familyguy.properties'", propertiesMap.containsKey("familyguy.properties"));
        validateFamilyGuy(propertiesMap.get("familyguy.properties"));

        assertTrue("contains 'simpsons.properties'", propertiesMap.containsKey("simpsons.properties"));
        validateSimpsons(propertiesMap.get("simpsons.properties"));

        try {
            resourceFinder.mapAllProperties("movies");
        } catch (Exception success) {
        }

    }

    public void testMapAvailableProperties() throws Exception {
        Map<String, Properties> propertiesMap = resourceFinder.mapAvailableProperties("movies");
        assertNotNull("properties", propertiesMap);
        assertEquals("map size", 2, propertiesMap.size());

        assertTrue("contains 'serenity.properties'", propertiesMap.containsKey("serentity.properties"));
        Properties properties = propertiesMap.get("serentity.properties");
        assertEquals("director", "Joss Whedon", properties.getProperty("director"));
        assertEquals("star", "Nathan Fillion", properties.getProperty("star"));
        assertEquals("year", "2005", properties.getProperty("year"));

        assertTrue("contains 'kingkong.properties'", propertiesMap.containsKey("kingkong.properties"));
        properties = propertiesMap.get("kingkong.properties");
        assertEquals("director", "Peter Jackson", properties.getProperty("director"));
        assertEquals("star", "Naomi Watts", properties.getProperty("star"));
        assertEquals("year", "2005", properties.getProperty("year"));
    }


    public void testWebinfJar() throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("WEB-INF/beans.xml", "<beans/>");

        final File jarFile = Archives.jarArchive(map);

        final URL jarFileUrl = jarFile.toURI().toURL();
        final ResourceFinder finder = new ResourceFinder(jarFileUrl);

        final URL beansXmlUrl = finder.find("WEB-INF/beans.xml");

        assertNotNull(beansXmlUrl);
    }


    private static void readJarEntries(URL location, String basePath, Map<String, URL> resources) throws IOException {
        JarURLConnection conn = (JarURLConnection) location.openConnection();
        JarFile jarfile = null;
        jarfile = conn.getJarFile();

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



    private void validateSimpsons(Properties properties) {
        assertEquals("props size", 6, properties.size());
        assertEquals("creator", "Matt Groening", properties.getProperty("creator"));
        assertEquals("father", "Homer", properties.getProperty("father"));
        assertEquals("mother", "Marge", properties.getProperty("mother"));
        assertEquals("son", "Bart", properties.getProperty("son"));
        assertEquals("daughter", "Lisa", properties.getProperty("daughter"));
        assertEquals("baby", "Maggie", properties.getProperty("baby"));
    }

    private void validateFamilyGuy(Properties properties) {
        assertEquals("props size", 6, properties.size());
        assertEquals("creator", "Seth MacFarlane", properties.getProperty("creator"));
        assertEquals("father", "Peter", properties.getProperty("father"));
        assertEquals("mother", "Lois", properties.getProperty("mother"));
        assertEquals("son", "Chris", properties.getProperty("son"));
        assertEquals("daughter", "Meg", properties.getProperty("daughter"));
        assertEquals("baby", "Stewie", properties.getProperty("baby"));
    }


    /*
     * Disable test because it's failing its purpose:
     *   - when running in maven in a clean build, no urls are found
     *       so the test runs with the ResourceFinder using the classloader
     *       instead of urls
     *   - when running on a non clean build in maven, one url is found,
     *       but the test fails

    public void testUrlConstructor() throws Exception {
        List<URL> all = resourceFinder.findAll("MANIFEST.MF");

        List<URL> urls = new ArrayList();
        for (URL url : all) {
            if (url.getPath().contains("xbean-finder")){
                urls.add(url);
            }
        }

        resourceFinder = new ResourceFinder("META-INF/", urls.toArray(new URL[]{}));
        testGetResourcesMap1();
        testGetResourcesMap2();
        testFindString();
        testFindAllStrings();
        testFindAvailableStrings();
        testMapAllStrings();
        testMapAvailableStrings();
        testFindClass();
        testFindAllClasses();
        testFindAvailableClasses();
        testMapAllClasses();
        testMapAvailableClasses();
        testFindImplementation();
        testFindAllImplementations();
        testMapAllImplementations();
        testMapAvailableImplementations();
        testFindProperties();
        testFindAllProperties();
        testFindAvailableProperties();
        testMapAllProperties();
        testMapAvailableProperties();
    }
    */


}
