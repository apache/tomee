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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import java.util.Map;
import java.util.Properties;
import org.apache.openejb.loader.SystemInstance;

public final class PropertyPlaceHolderHelper {
    private static final String PREFIX = "${";
    private static final String SUFFIX = "}";

    private PropertyPlaceHolderHelper() {
        // no-op
    }

    public static String value(final String key) {
        if (key == null || !key.startsWith(PREFIX) || !key.endsWith(SUFFIX)) {
            return key;
        }

        final String value = SystemInstance.get().getOptions().get(key.substring(2, key.length() - 1), key);
        if (!value.equals(key) && value.startsWith("java:")) {
            return value.substring(5);
        }
        return value;
    }

    public static Properties holds(final Properties properties) {
        final Properties updated = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final Object rawValue = entry.getValue();
            if (rawValue instanceof String) {
                updated.put(entry.getKey(), value(rawValue.toString()));
            }
        }
        return updated;
    }

    public static void holdsWithUpdate(final Properties props) {
        final Properties toUpdate = holds(props);
        props.putAll(toUpdate);
    }
}
