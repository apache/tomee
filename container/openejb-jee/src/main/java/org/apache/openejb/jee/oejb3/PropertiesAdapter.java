/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jee.oejb3;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class PropertiesAdapter extends XmlAdapter<String, Properties> {
    public Properties unmarshal(final String s) throws Exception {
        final Properties properties = new Properties();
        final ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
        properties.load(in);
        return properties;
    }

    public String marshal(final Properties properties) throws Exception {
        if (properties == null) return null;

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        new Properties() { // sort entries as before java 9, todo: decide if we want to sort it like that or if we just stop from being deterministic there
            {
                putAll(properties);
            }

            @Override
            public Set<Map.Entry<Object, Object>> entrySet() {
                final Set<Map.Entry<Object, Object>> entrySet = super.entrySet();
                final Set<Map.Entry<Object, Object>> entries = new TreeSet<>(new Comparator<Map.Entry<Object, Object>>() {
                    @Override
                    public int compare(final Map.Entry<Object, Object> o1, final Map.Entry<Object, Object> o2) {
                        return String.valueOf(o1.getKey()).compareTo(String.valueOf(o2.getKey()));
                    }
                });
                entries.addAll(entrySet);
                return entries;
            }
        }.store(out, null);

        // First comment is added by properties.store()
        final String string = new String(out.toByteArray());
        return string.replaceFirst("#.*?" + System.lineSeparator(), "");
    }
}
