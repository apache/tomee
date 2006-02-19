package org.openejb.util;
/**
 * @version $Revision$ $Date$
 */

import junit.framework.*;
import org.openejb.util.ResourceFinder;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Properties;
import java.io.IOException;

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
            assertTrue("ends with META-INF/"+key, value.getPath().endsWith("META-INF/"+key));
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
            assertTrue("ends with META-INF/"+token+"/"+key, value.getPath().endsWith("META-INF/"+token+"/"+key));
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
        List<String> manifests = null;
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
        Map<String,String> resourcesMap = resourceFinder.mapAllStrings("serializables");

        assertEquals("map size", 3, resourcesMap.size());
        assertTrue("map contains key 'one'", resourcesMap.containsKey("one"));
        assertEquals(One.class.getName(), resourcesMap.get("one"));

        assertTrue("map contains key 'two'", resourcesMap.containsKey("two"));
        assertEquals(Two.class.getName(), resourcesMap.get("two"));

        assertTrue("map contains key 'three'", resourcesMap.containsKey("three"));
        assertEquals(Three.class.getName(), resourcesMap.get("three"));
    }

    public void testMapAvailableStrings() throws Exception {
        Map<String,String> resourcesMap = resourceFinder.mapAvailableStrings("serializables");

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
            fail("Wrong exception type was thrown: "+e.getClass().getName());
        }
    }

    public void testFindAllClasses() throws Exception {
        List<Class> classes = resourceFinder.findAllClasses("java.io.Serializable");
        assertEquals("size", 1, classes.size());
        assertEquals(One.class, classes.get(0));

        try {
            resourceFinder.findAllClasses("java.io.OutputStream");
            fail("ClassNotFoundException should be thrown");
        } catch (ClassNotFoundException success) {
            // pass
        } catch (Exception e) {
            fail("Wrong exception type was thrown: "+e.getClass().getName());
        }
    }

    public void testFindAvailableClasses() throws Exception {
        List<Class> classes = resourceFinder.findAvailableClasses("java.io.Serializable");
        assertEquals("size", 1, classes.size());
        assertEquals(One.class, classes.get(0));

        classes = resourceFinder.findAvailableClasses("java.io.OutputStream");
        assertEquals("size", 0, classes.size());
    }

    public void testMapAllClasses() throws Exception {
        Map<String,Class> resourcesMap = resourceFinder.mapAllClasses("serializables");

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
            fail("Wrong exception type was thrown: "+e.getClass().getName());
        }
    }

    public void testMapAvailableClasses() throws Exception {
        Map<String,Class> resourcesMap = resourceFinder.mapAvailableClasses("externalizables");

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
        } catch (Exception e){
            fail("Wrong exception type was thrown: "+e.getClass().getName());
        }
    }

    public void testFindAllImplementations() throws Exception {
        List<Class> classes = resourceFinder.findAllImplementations(java.io.Serializable.class);
        assertEquals("size", 1, classes.size());
        assertEquals(One.class, classes.get(0));

        try {
            resourceFinder.findAllImplementations(java.io.InputStream.class);
            fail("ClassNotFoundException should be thrown");
        } catch (ClassCastException success) {
        } catch (Exception e) {
            fail("Wrong exception type was thrown: "+e.getClass().getName());
        }
    }

    public void testMapAllImplementations() throws Exception {
        Map<String,Class> resourcesMap = resourceFinder.mapAllImplementations(javax.naming.spi.ObjectFactory.class);

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
            fail("Wrong exception type was thrown: "+e.getClass().getName());
        }
    }

    public void testMapAvailableImplementations() throws Exception {
        Map<String,Class> resourcesMap = resourceFinder.mapAvailableImplementations(java.net.URLStreamHandler.class);

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
        assertNotNull("properties",properties);
        validateFamilyGuy(properties);

        properties = resourceFinder.findProperties("tvshows/simpsons.properties");
        assertNotNull("properties",properties);
        validateSimpsons(properties);
    }

    public void testFindAllProperties() throws Exception {
        List<Properties> propertiesLists = resourceFinder.findAllProperties("tvshows/familyguy.properties");
        assertNotNull("properties",propertiesLists);
        assertEquals("list size", 1, propertiesLists.size());

        Properties properties = propertiesLists.get(0);
        validateFamilyGuy(properties);
    }

    public void testFindAvailableProperties() throws Exception {
        List<Properties> propertiesLists = resourceFinder.findAvailableProperties("tvshows/familyguy.properties");
        assertNotNull("properties",propertiesLists);
        assertEquals("list size", 1, propertiesLists.size());

        Properties properties = propertiesLists.get(0);
        validateFamilyGuy(properties);
    }

    public void testMapAllProperties() throws Exception {
        Map<String,Properties> propertiesMap = resourceFinder.mapAllProperties("tvshows");
        assertNotNull("properties",propertiesMap);
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
        Map<String,Properties> propertiesMap = resourceFinder.mapAvailableProperties("movies");
        assertNotNull("properties",propertiesMap);
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


}