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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.config.provider.ID;
import org.apache.openejb.config.provider.ProviderCircularReferenceException;
import org.apache.openejb.config.provider.ProviderLoader;
import org.apache.openejb.config.provider.ProviderManager;
import org.apache.openejb.config.sys.ServiceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class ProviderManagerTest extends TestCase {
    public void testRegister() throws Exception {
        final ProviderManager manager = new ProviderManager(new ProviderLoader() {
            @Override
            public ServiceProvider load(final ID id) {
                return null;
            }

            @Override
            public List<ServiceProvider> load(final String namespace) {
                return null;
            }
        });

        { // Add Color
            final ServiceProvider color = new ServiceProvider(Color.class, "Color", "Resource");
            color.getProperties().setProperty("red", "0");
            color.getProperties().setProperty("green", "0");
            color.getProperties().setProperty("blue", "0");
            manager.register("default", color);
        }

        { // Assert Color
            // Must be able to retrieve provider and properties in a case-insensitive manner
            final ServiceProvider provider = manager.get("DeFaulT", "CoLoR");
            assertNotNull(provider);
            assertEquals(Color.class.getName(), provider.getClassName());
            assertEquals("0", provider.getProperties().getProperty("rEd"));
            assertEquals("0", provider.getProperties().get("grEEn"));
            assertEquals("0", provider.getProperties().get("blUE"));
        }

        { // Add Red
            final ServiceProvider red = new ServiceProvider();
            red.setId("Red");
            red.setParent("Color");
            red.getProperties().setProperty("red", "255");
            manager.register("default", red);
        }

        { // Assert Red

            // Should have inherited green and blue values from Color

            final ServiceProvider provider = manager.get("dEFaulT", "REd");
            assertNotNull(provider);
            assertEquals(Color.class.getName(), provider.getClassName());
            assertEquals("255", provider.getProperties().getProperty("rED"));
            assertEquals("0", provider.getProperties().get("grEEN"));
            assertEquals("0", provider.getProperties().get("bLUe"));
        }


        { // Add Orange
            final ServiceProvider orange = new ServiceProvider();
            orange.setId("Orange");
            orange.setParent("Red");
            orange.getProperties().setProperty("green", "200");
            manager.register("default", orange);
        }

        { // Assert Orange

            // Should have inherited from Red

            final ServiceProvider provider = manager.get("dEFAUlT", "orAngE");
            assertNotNull(provider);
            assertEquals(Color.class.getName(), provider.getClassName());
            assertEquals("255", provider.getProperties().getProperty("reD"));
            assertEquals("200", provider.getProperties().get("grEeN"));
            assertEquals("0", provider.getProperties().get("bLue"));
        }
    }

    public void testLoader() throws Exception {

        final ProviderManager manager = new ProviderManager(new ProviderLoader() {
            @Override
            public ServiceProvider load(final ID id) {
                if ("color".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider color = new ServiceProvider(Color.class, "Color", "Resource");
                    color.getProperties().setProperty("red", "0");
                    color.getProperties().setProperty("green", "0");
                    color.getProperties().setProperty("blue", "0");
                    return color;
                }

                if ("red".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider red = new ServiceProvider();
                    red.setId("Red");
                    red.setParent("Color");
                    red.getProperties().setProperty("red", "255");
                    return red;
                }

                if ("orange".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider orange = new ServiceProvider();
                    orange.setId("Orange");
                    orange.setParent("Red");
                    orange.getProperties().setProperty("green", "200");
                    return orange;
                }

                throw new IllegalStateException(id.toString());
            }

            @Override
            public List<ServiceProvider> load(final String namespace) {
                return null;
            }
        });

        assertEquals(0, manager.getAll().size());

        { // Assert Orange

            // Should have inherited from Red

            final ServiceProvider provider = manager.get("dEFAUlT", "orAngE");
            assertNotNull(provider);
            assertEquals(Color.class.getName(), provider.getClassName());
            assertEquals("255", provider.getProperties().getProperty("reD"));
            assertEquals("200", provider.getProperties().get("grEeN"));
            assertEquals("0", provider.getProperties().get("bLue"));
        }

        { // Assert Red

            // Should have inherited green and blue values from Color

            final ServiceProvider provider = manager.get("dEFaulT", "REd");
            assertNotNull(provider);
            assertEquals(Color.class.getName(), provider.getClassName());
            assertEquals("255", provider.getProperties().getProperty("rED"));
            assertEquals("0", provider.getProperties().get("grEEN"));
            assertEquals("0", provider.getProperties().get("bLUe"));
        }

        { // Assert Color
            // Must be able to retrieve provider and properties in a case-insensitive manner

            final ServiceProvider provider = manager.get("DeFaulT", "CoLoR");
            assertNotNull(provider);
            assertEquals(Color.class.getName(), provider.getClassName());
            assertEquals("0", provider.getProperties().getProperty("rEd"));
            assertEquals("0", provider.getProperties().get("grEEn"));
            assertEquals("0", provider.getProperties().get("blUE"));
        }

        assertEquals(3, manager.getAll().size());
    }

    public void testLoaderLoadNamespace() throws Exception {

        final ProviderManager manager = new ProviderManager(new ProviderLoader() {
            @Override
            public ServiceProvider load(final ID id) {
                if ("color".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider color = new ServiceProvider(Color.class, "Color", "Resource");
                    color.getProperties().setProperty("red", "0");
                    color.getProperties().setProperty("green", "0");
                    color.getProperties().setProperty("blue", "0");
                    return color;
                }

                if ("red".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider red = new ServiceProvider();
                    red.setId("Red");
                    red.setParent("Color");
                    red.getProperties().setProperty("red", "255");
                    return red;
                }

                if ("orange".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider orange = new ServiceProvider();
                    orange.setId("Orange");
                    orange.setParent("Red");
                    orange.getProperties().setProperty("green", "200");
                    return orange;
                }

                throw new IllegalStateException(id.toString());
            }

            @Override
            public List<ServiceProvider> load(final String namespace) {
                final List<ServiceProvider> list = new ArrayList<>();
                list.add(load(new ID(namespace, "color")));
                list.add(load(new ID(namespace, "red")));
                list.add(load(new ID(namespace, "orange")));
                return list;
            }
        });

        assertEquals(0, manager.getAll().size());

        manager.get("default", "Color");

        assertEquals(1, manager.getAll().size());

        manager.load("default");

        assertEquals(3, manager.getAll().size());

        { // Assert Orange

            // Should have inherited from Red

            final ServiceProvider provider = manager.get("dEFAUlT", "orAngE");
            assertNotNull(provider);
            assertEquals(Color.class.getName(), provider.getClassName());
            assertEquals("255", provider.getProperties().getProperty("reD"));
            assertEquals("200", provider.getProperties().get("grEeN"));
            assertEquals("0", provider.getProperties().get("bLue"));
        }

        { // Assert Red

            // Should have inherited green and blue values from Color

            final ServiceProvider provider = manager.get("dEFaulT", "REd");
            assertNotNull(provider);
            assertEquals(Color.class.getName(), provider.getClassName());
            assertEquals("255", provider.getProperties().getProperty("rED"));
            assertEquals("0", provider.getProperties().get("grEEN"));
            assertEquals("0", provider.getProperties().get("bLUe"));
        }

        { // Assert Color
            // Must be able to retrieve provider and properties in a case-insensitive manner

            final ServiceProvider provider = manager.get("DeFaulT", "CoLoR");
            assertNotNull(provider);
            assertEquals(Color.class.getName(), provider.getClassName());
            assertEquals("0", provider.getProperties().getProperty("rEd"));
            assertEquals("0", provider.getProperties().get("grEEn"));
            assertEquals("0", provider.getProperties().get("blUE"));
        }

        assertEquals(3, manager.getAll().size());
    }


    public void testCircularDependency() throws Exception {

        final ProviderManager manager = new ProviderManager(new ProviderLoader() {
            @Override
            public ServiceProvider load(final ID id) {
                if ("color".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider color = new ServiceProvider(Color.class, "Color", "Resource");
                    color.setParent("Orange");
                    color.getProperties().setProperty("red", "0");
                    color.getProperties().setProperty("green", "0");
                    color.getProperties().setProperty("blue", "0");
                    return color;
                }

                if ("red".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider red = new ServiceProvider();
                    red.setId("Red");
                    red.setParent("Color");
                    red.getProperties().setProperty("red", "255");
                    return red;
                }

                if ("orange".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider orange = new ServiceProvider();
                    orange.setId("Orange");
                    orange.setParent("Red");
                    orange.getProperties().setProperty("green", "200");
                    return orange;
                }

                throw new IllegalStateException(id.toString());
            }

            @Override
            public List<ServiceProvider> load(final String namespace) {
                return null;
            }
        });

        assertEquals(0, manager.getAll().size());

        try {
            manager.get("dEFAUlT", "orAngE");
            fail("ProviderCircularReferenceException should have been thrown");
        } catch (final ProviderCircularReferenceException e) {
            // pass
        }
    }

    public void testInheritedAttributes() throws Exception {

        final ProviderManager manager = new ProviderManager(new ProviderLoader() {
            @Override
            public ServiceProvider load(final ID id) {
                if ("color".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider color = new ServiceProvider();
                    color.setClassName(Color.class.getName());
                    color.setFactoryName("fooFactory");
                    color.setId("Color");
                    color.setService("Resource");
                    color.setConstructor("one, two, three");
                    color.setDescription("the description");
                    color.setDisplayName("the display name");
                    color.getProperties().setProperty("red", "0");
                    color.getProperties().setProperty("green", "0");
                    color.getProperties().setProperty("blue", "0");
                    color.getTypes().add(Color.class.getName());
                    return color;
                }

                if ("red".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider red = new ServiceProvider();
                    red.setId("Red");
                    red.setParent("Color");
                    red.getProperties().setProperty("red", "255");
                    return red;
                }

                if ("orange".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider orange = new ServiceProvider();
                    orange.setId("Orange");
                    orange.setParent("Red");
                    orange.getProperties().setProperty("green", "200");
                    return orange;
                }

                throw new IllegalStateException(id.toString());
            }

            @Override
            public List<ServiceProvider> load(final String namespace) {
                final List<ServiceProvider> list = new ArrayList<>();
                list.add(load(new ID(namespace, "color")));
                list.add(load(new ID(namespace, "red")));
                list.add(load(new ID(namespace, "orange")));
                return list;
            }
        });

        { // Assert Orange

            // Should have inherited from Red

            final ServiceProvider provider = manager.get("dEFAUlT", "orAngE");
            assertNotNull(provider);
            assertEquals(Color.class.getName(), provider.getClassName());
            assertEquals("Resource", provider.getService());
            assertEquals("one, two, three", provider.getConstructor());
            assertEquals("the description", provider.getDescription());
            assertEquals("the display name", provider.getDisplayName());
            assertEquals("fooFactory", provider.getFactoryName());
            assertEquals("255", provider.getProperties().getProperty("reD"));
            assertEquals("200", provider.getProperties().get("grEeN"));
            assertEquals("0", provider.getProperties().get("bLue"));
            assertEquals(1, provider.getTypes().size());
        }

        { // Assert Red

            // Should have inherited green and blue values from Color

            final ServiceProvider provider = manager.get("dEFaulT", "REd");
            assertNotNull(provider);
            assertEquals(Color.class.getName(), provider.getClassName());
            assertEquals("Resource", provider.getService());
            assertEquals("one, two, three", provider.getConstructor());
            assertEquals("the description", provider.getDescription());
            assertEquals("the display name", provider.getDisplayName());
            assertEquals("fooFactory", provider.getFactoryName());
            assertEquals("255", provider.getProperties().getProperty("rED"));
            assertEquals("0", provider.getProperties().get("grEEN"));
            assertEquals("0", provider.getProperties().get("bLUe"));
        }

        { // Assert Color
            // Must be able to retrieve provider and properties in a case-insensitive manner

            final ServiceProvider provider = manager.get("DeFaulT", "CoLoR");
            assertNotNull(provider);
            assertEquals(Color.class.getName(), provider.getClassName());
            assertEquals("Resource", provider.getService());
            assertEquals("one, two, three", provider.getConstructor());
            assertEquals("the description", provider.getDescription());
            assertEquals("the display name", provider.getDisplayName());
            assertEquals("fooFactory", provider.getFactoryName());
            assertEquals("0", provider.getProperties().getProperty("rEd"));
            assertEquals("0", provider.getProperties().get("grEEn"));
            assertEquals("0", provider.getProperties().get("blUE"));
        }

        assertEquals(3, manager.getAll().size());
    }


    public static class Color {
        private int red;
        private int green;
        private int blue;

        public Color() {
        }

        public int getRed() {
            return red;
        }

        public void setRed(final int red) {
            this.red = red;
        }

        public int getGreen() {
            return green;
        }

        public void setGreen(final int green) {
            this.green = green;
        }

        public int getBlue() {
            return blue;
        }

        public void setBlue(final int blue) {
            this.blue = blue;
        }
    }
}
