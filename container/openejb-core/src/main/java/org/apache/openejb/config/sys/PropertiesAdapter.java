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

import org.apache.openejb.loader.IO;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.SuperProperties;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

/**
 * Converts a java.util.Properties object to a String in the XML file.
 */
public class PropertiesAdapter extends XmlAdapter<String, Properties> {
    public Properties unmarshal(final String s) throws Exception {
        return IO.readProperties(IO.read(s), new SuperProperties());
    }

    public String marshal(final Properties properties) throws Exception {
        if (properties == null) {
            return null;
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        properties.store(out, null);

        String string = new String(out.toByteArray());

        if (!(properties instanceof SuperProperties)) {
            // First comment is added by properties.store()
            string = string.replaceFirst("#.*?" + JavaSecurityManagers.getSystemProperty("line.separator"), "");
        }

        return string;
    }
}
