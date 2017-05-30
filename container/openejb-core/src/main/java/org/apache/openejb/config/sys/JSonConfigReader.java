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

package org.apache.openejb.config.sys;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.SimpleJSonParser;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class JSonConfigReader {
    private static final String COMMENT_KEY = "__";

    private static Map map(final Object rawMap) {
        return Map.class.cast(rawMap);
    }

    public static <T> T read(final Class<T> clazz, final InputStream is) throws OpenEJBException {
        if (Openejb.class.equals(clazz) || Tomee.class.equals(clazz)) {
            final SaxOpenejb handler = read(is, "openejb",
                Arrays.asList("System-Property", "Resource", "Container", "JndiProvider", "TransactionManager", "ConnectionManager",
                    "ProxyFactory", "Connector", "Deployments", "Import", "Service", "SecurityService"),
                new SaxOpenejb());

            return clazz.cast(handler.getOpenejb());
        } else if (Resources.class.equals(clazz)) {
            final Resources resources = new Resources();

            // reuse openejb parser since we use saw logic and not jaxb one
            final Openejb openejb = read(is, "openejb",
                Arrays.asList("Resource", "Container", "JndiProvider", "Connector", "Import", "Service"),
                new SaxOpenejb()).getOpenejb();

            resources.getContainer().addAll(openejb.getContainer());
            resources.getResource().addAll(openejb.getResource());
            resources.getService().addAll(openejb.getServices());
            resources.getConnector().addAll(openejb.getConnector());
            resources.getJndiProvider().addAll(openejb.getJndiProvider());

            return clazz.cast(resources);
        }
        throw new IllegalArgumentException(clazz.getName() + " not supported");
    }

    private static <T extends DefaultHandler> T read(final InputStream is, final String mainRoot, final Collection<String> roots, final T handler) throws OpenEJBException {
        try {
            handler.startDocument();
            handler.startElement(null, mainRoot, null, new AttributesImpl());

            final Map<?, ?> jsConfig = map(SimpleJSonParser.read(is));
            jsConfig.remove(COMMENT_KEY);

            for (final String root : roots) {
                final String currentRoot;
                if (root.endsWith("s")) {
                    currentRoot = root.toLowerCase(Locale.ENGLISH);
                } else {
                    currentRoot = root.toLowerCase(Locale.ENGLISH) + "s";
                }

                final Map<String, Map<String, Map<String, String>>> resources = map(jsConfig.get(currentRoot));
                if (resources != null) {
                    resources.remove(COMMENT_KEY);

                    for (final Map.Entry<String, Map<String, Map<String, String>>> resource : resources.entrySet()) {
                        final AttributesImpl attributes = toAttributes(map(resource.getValue()), "properties");
                        if (!"deployments".equals(currentRoot)) {
                            attributes.addAttribute(null, "id", "id", null, resource.getKey());
                        }

                        if ("resources".equals(currentRoot) && attributes.getIndex("type") == -1 && attributes.getIndex("class-name") == -1 && attributes.getIndex("provider") == -1) {
                            attributes.addAttribute(null, "type", "type", null, "DataSource");
                        }

                        handler.startElement(null, root, root, attributes);

                        final String propertiesAsStr = toString(map(resource.getValue().get("properties")));

                        handler.characters(propertiesAsStr.toCharArray(), 0, propertiesAsStr.length());
                        // properties
                        handler.endElement(null, root, root);
                    }
                }
            }

            handler.endElement(null, mainRoot, null);

            // global config
            if (jsConfig.containsKey("system-properties")) {
                final Map<?, ?> sysProps = map(jsConfig.get("system-properties"));

                setProperties("", sysProps);
            }

            // same as global but more specific, would be more readable
            if (jsConfig.containsKey("daemons")) {
                final Map<String, ?> daemons = map(jsConfig.get("daemons"));
                for (final Map.Entry<String, ?> entry : daemons.entrySet()) {
                    setProperties(entry.getKey() + '.', map(entry.getValue()));
                }
            }
        } catch (final Exception e) {
            throw new OpenEJBException(e.getMessage(), e);
        }

        return handler;
    }

    private static void setProperties(final String prefix, final Map<?, ?> sysProps) {
        sysProps.remove(COMMENT_KEY);

        for (final Map.Entry<?, ?> entry : sysProps.entrySet()) {
            final String key = prefix + entry.getKey().toString();
            final Object value = entry.getValue();
            if (String.class.isInstance(value)) {
                final String str = String.class.cast(value);

                // set it for openejb AND the JVM since that's probably too late to let it be done automatically
                SystemInstance.get().setProperty(key, str);
                JavaSecurityManagers.setSystemProperty(key, str);
            } else {
                setProperties(key + '.', map(value));
            }
        }
    }

    private static String toString(final Map<String, ?> properties) {
        if (properties == null) {
            return "";
        }

        properties.remove(COMMENT_KEY);

        final Properties builder = new Properties();
        for (final Map.Entry<String, ?> entry : properties.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().toString());
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            builder.store(baos, "");
        } catch (final IOException e) {
            // no-op
        }
        return new String(baos.toByteArray());
    }

    private static AttributesImpl toAttributes(final Map<String, String> map, final String... ignored) {
        map.remove(COMMENT_KEY);

        final AttributesImpl attributes = new AttributesImpl();
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            final String key = entry.getKey();
            boolean add = true;
            for (final String i : ignored) {
                if (key.equals(i)) {
                    add = false;
                    break;
                }
            }

            if (add) {
                attributes.addAttribute(null, key, key, null, entry.getValue());
            }
        }
        return attributes;
    }
}
