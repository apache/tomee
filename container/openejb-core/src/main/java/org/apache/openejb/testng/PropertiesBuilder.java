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

package org.apache.openejb.testng;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesBuilder {
    private final Properties properties = new Properties();

    public PropertiesBuilder p(final String key, final String value) {
        return property(key, value);
    }

    public PropertiesBuilder property(final String key, final String value) {
        properties.setProperty(key, value);
        return this;
    }

    public Properties build() {
        return properties;
    }

    public Map<String, String> asMap() {
        final Map<String, String> map = new HashMap<>();
        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            map.put(String.class.cast(entry.getKey()), String.class.cast(entry.getValue()));
        }
        return map;
    }
}
