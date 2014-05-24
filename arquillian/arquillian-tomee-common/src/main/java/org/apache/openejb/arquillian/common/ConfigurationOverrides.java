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
package org.apache.openejb.arquillian.common;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationOverrides {
    protected static final Logger LOGGER = Logger.getLogger(TomEEContainer.class.getName());

    public static List<URL> apply(final Object configuration, final Properties systemProperties, final String... prefixes) {
        final List<URL> propertiesFiles = findPropertiesFiles("default.arquillian-%s.properties", prefixes);
        if (!propertiesFiles.isEmpty()) {
            apply(configuration, systemProperties, propertiesFiles, false, prefixes);
        }

        final List<URL> overridePropFiles = findPropertiesFiles("arquillian-%s.properties", prefixes);
        if (!overridePropFiles.isEmpty()) {
            apply(configuration, systemProperties, overridePropFiles, true, prefixes);
        }

        propertiesFiles.addAll(overridePropFiles);

        // use system properties in all cases
        apply(configuration, systemProperties, Collections.<URL>emptyList(), true, prefixes);

        return propertiesFiles;
    }

    public static void apply(final Object configuration, final Properties systemProperties, final List<URL> urls, final boolean overrideNotNull, final String... prefixes) {
        final List<Properties> propertiesList = read(urls);

        final Properties defaults = new Properties();

        // Merge all the properties
        for (final Properties p : propertiesList) {
            defaults.putAll(p);
        }

        final ObjectMap map = new ObjectMap(configuration);
        for (final Map.Entry<Object, Object> entry : defaults.entrySet()) {
            final String key = entry.getKey().toString();
            final String value = entry.getValue().toString();
            setProperty(map, key, key, value, Level.FINE, overrideNotNull);
        }

        //
        // Override the config with system properties
        //
        for (final String key : map.keySet()) {
            for (final String prefix : prefixes) {
                final String property = prefix + "." + key;
                final String value = systemProperties.getProperty(property);

                setProperty(map, key, property, value, Level.INFO, true); // always override with system properties
            }
        }
    }

    private static List<Properties> read(final List<URL> urls) {
        final List<Properties> propertiesList = new ArrayList<Properties>();
        for (final URL url : urls) {
            try {
                propertiesList.add(IO.readProperties(url));
            } catch (final IOException e) {
                LOGGER.log(Level.WARNING, "Cannot read : " + url, e);
            }
        }
        return propertiesList;
    }

    public static List<URL> findPropertiesFiles(final String name, final String... prefixes) {
        final List<URL> urls = new ArrayList<URL>();

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (final String prefix : prefixes) {
            final String resourceName = String.format(name, prefix.replace('.', '-'));
            addResources(urls, loader, resourceName);
        }

        return urls;
    }

    private static void addResources(final List<URL> urls, final ClassLoader loader, final String resourceName) {
        try {
            final Enumeration<URL> resources = loader.getResources(resourceName);
            while (resources.hasMoreElements()) {
                urls.add(resources.nextElement());
            }
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Failed getResources: " + resourceName, e);
        }
    }

    private static void setProperty(final ObjectMap map, final String key, final String property, final String value, final Level info, final boolean overrideNotNull) {
        if (value == null) {
            LOGGER.log(Level.FINE, String.format("Unset '%s'", property));
            return;
        }

        if (!overrideNotNull && !isNull(map.get(key))) {
            LOGGER.log(Level.FINE, String.format("Unset '%s' because already set", property));
            return;
        }

        try {
            LOGGER.log(info, String.format("Applying override '%s=%s'", property, value));
            map.put(key, value);
        } catch (final Exception e) {
            try {
                map.put(key, Integer.parseInt(value)); // we manage String and int and boolean so let's try an int
            } catch (final Exception ignored) {
                try {
                    map.put(key, Boolean.parseBoolean(value)); // idem let's try a boolean
                } catch (final Exception ignored2) {
                    LOGGER.log(Level.WARNING, String.format("Override failed '%s=%s'", property, value), e);
                }
            }
        }
    }

    private static boolean isNull(final Object o) {
        if (Number.class.isInstance(o)) {
            return Number.class.cast(o).intValue() == 0;
        }
        return o == null;
    }
}
