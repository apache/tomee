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

package org.apache.openejb.util;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class PropertiesHelper {
    private PropertiesHelper() {
        // no-op
    }

    public static String propertiesToString(final Properties p) {
        if (p == null) {
            return "";
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            p.store(baos, "");
        } catch (final Exception ignored) {
            // no-op
        }
        return new String(baos.toByteArray());
    }

    public static Map<String, Object> map(final Properties props) {
        final Map<String, Object> map = new HashMap<>();
        for (final Map.Entry<Object, Object> entry : props.entrySet()) {
            map.put((String) entry.getKey(), entry.getValue());
        }
        return map;
    }
}
