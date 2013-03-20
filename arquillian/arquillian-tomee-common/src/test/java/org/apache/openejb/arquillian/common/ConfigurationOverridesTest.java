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
package org.apache.openejb.arquillian.common;

import junit.framework.TestCase;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationOverridesTest extends TestCase {

    /**
     * This test is to guarantee the order that we find the related properties
     * files.  Changing the order would significantly change the logic.
     *
     * Do not change the order
     *
     * Note: the order is guaranteed by org.apache.openejb.arquillian.common.ConfigurationOverrides#apply(java.lang.Object, java.util.Properties, java.lang.String...)
     *      because it needs to know if we work on a default or not property file
     *      to be able to not override already set properties with defaults ones
     *
     * @throws Exception
     */
    public void testFindPropertiesFiles() throws Exception {
        String[] prefixes = { "color", "color.orange" };
        final List<URL> color = ConfigurationOverrides.apply(new Color(), new Properties(), prefixes);

        assertEquals(4, color.size());
        assertTrue(color.get(0).toExternalForm().endsWith("/default.arquillian-color.properties"));
        assertTrue(color.get(1).toExternalForm().endsWith("/default.arquillian-color-orange.properties"));
        assertTrue(color.get(2).toExternalForm().endsWith("/arquillian-color.properties"));
        assertTrue(color.get(3).toExternalForm().endsWith("/arquillian-color-orange.properties"));
    }

    public void testApply() throws Exception {

        {
            final Color color = new Color();
            ConfigurationOverrides.apply(color, new Properties(), "color");
            assertEquals(240, color.red);
            assertEquals(241, color.green);
            assertEquals(255, color.blue);
            assertEquals("1.0", color.alpha);
        }

        {
            final Color color = new Color();
            final Properties systemProperties = new Properties();
            systemProperties.setProperty("color.green", "20");
            systemProperties.setProperty("blue", "20"); // should have no effect

            ConfigurationOverrides.apply(color, systemProperties, "color");
            assertEquals(240, color.red);
            assertEquals(20, color.green);
            assertEquals(255, color.blue);
            assertEquals("1.0", color.alpha);
        }

        {
            final Color color = new Color();
            ConfigurationOverrides.apply(color, new Properties(), "color", "color.orange");
            assertEquals(240, color.red);
            assertEquals(140, color.green);
            assertEquals(0, color.blue);
            assertEquals("1.0", color.alpha);
        }

        {
            final Properties systemProperties = new Properties();
            systemProperties.setProperty("color.blue", "1");
            systemProperties.setProperty("red", "20"); // should have no effect

            final Color color = new Color();

            ConfigurationOverrides.apply(color, systemProperties, "color", "color.orange");
            assertEquals(240, color.red);
            assertEquals(140, color.green);
            assertEquals(1, color.blue);
            assertEquals("1.0", color.alpha);
        }

        {
            final Properties systemProperties = new Properties();
            systemProperties.setProperty("color.blue", "1");
            systemProperties.setProperty("color.orange.blue", "2");
            systemProperties.setProperty("red", "20"); // should have no effect

            final Color color = new Color();

            ConfigurationOverrides.apply(color, systemProperties, "color", "color.orange");
            assertEquals(240, color.red);
            assertEquals(140, color.green);
            assertEquals(2, color.blue);
            assertEquals("1.0", color.alpha);
        }
    }

    public static class Color {

        private int red;
        private int green;
        private int blue;
        private String alpha;

        public int getRed() {
            return red;
        }

        public void setRed(int red) {
            this.red = red;
        }

        public int getGreen() {
            return green;
        }

        public void setGreen(int green) {
            this.green = green;
        }

        public int getBlue() {
            return blue;
        }

        public void setBlue(int blue) {
            this.blue = blue;
        }

        public String getAlpha() {
            return alpha;
        }

        public void setAlpha(String alpha) {
            this.alpha = alpha;
        }
    }
}
