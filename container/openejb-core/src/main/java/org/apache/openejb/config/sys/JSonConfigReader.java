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
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class JSonConfigReader {
    private static Map map(final Object rawMap) {
        return Map.class.cast(rawMap);
    }

    public static Openejb read(final String json) throws OpenEJBException {
        final Bindings bindings = new SimpleBindings();
        bindings.put("json", json);

        final SaxOpenejb config = new SaxOpenejb();

        final ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("rhino");
        if (engine == null) { // try another engine
            engine = manager.getEngineByExtension("js");
        }
        if (engine == null) {
            throw new OpenEJBException("rhino not available");
        }

        try {
            config.startDocument();
            config.startElement(null, "openejb", null, new AttributesImpl());

            engine.eval("var openejbConfig = JSON.parse(json)", bindings);
            final Map<String, Map<String, Map<String, String>>> rawConfiguration = map(bindings.get("openejbConfig"));

            {
                final Map<String, Map<String, Map<String, String>>> resources = map(rawConfiguration.get("resources"));
                if (resources != null) {
                    for (final Map.Entry<String, Map<String, Map<String, String>>> resource : resources.entrySet()) {
                        final AttributesImpl attributes = toAttributes(map(resource.getValue()), "properties");
                        attributes.addAttribute(null, "id", "id", null, resource.getKey());
                        if (attributes.getIndex("type") == -1) {
                            attributes.addAttribute(null, "type", "type", null, "DataSource");
                        }
                        config.startElement(null, "Resource", "Resource", attributes);

                        final String propertiesAsStr = toString(map(resource.getValue().get("properties")));

                        config.characters(propertiesAsStr.toCharArray(), 0, propertiesAsStr.length());
                        // properties
                        config.endElement(null, "Resource", "Resource");
                    }
                }
            }

            {
                final Map<String, Map<String, String>> containers = map(rawConfiguration.get("containers"));
                if (containers != null) {
                    // TODO
                }
            }

            // TODO: see org.apache.openejb.config.sys.SaxOpenejb.Root.startElement() for all parent tags)

            config.endElement(null, "openejb", null);
        } catch (final Exception e) {
            throw new OpenEJBException(e.getMessage(), e);
        }

        return config.getOpenejb();
    }

    private static String toString(final Map<String, ?> properties) {
        if (properties == null) {
            return "";
        }
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
