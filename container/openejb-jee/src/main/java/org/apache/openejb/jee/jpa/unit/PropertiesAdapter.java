/**
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
package org.apache.openejb.jee.jpa.unit;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class PropertiesAdapter extends XmlAdapter<org.apache.openejb.jee.jpa.unit.Properties, java.util.Properties> {

    @Override
    public org.apache.openejb.jee.jpa.unit.Properties marshal(final java.util.Properties v) throws Exception {

        if (v == null) return null;

        final org.apache.openejb.jee.jpa.unit.Properties p = new org.apache.openejb.jee.jpa.unit.Properties();

        for (final Map.Entry<Object, Object> entry : v.entrySet()) {

            p.setProperty(entry.getKey().toString(), entry.getValue().toString());

        }

        return p;
    }

    @Override
    public java.util.Properties unmarshal(final org.apache.openejb.jee.jpa.unit.Properties v) throws Exception {

        if (v == null) return null;

        final java.util.Properties properties = new java.util.Properties();

        for (final Property property : v.getProperty()) {
            properties.setProperty(property.getName(), property.getValue());
        }

        return properties;
    }
}
