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

package org.apache.openejb.config.typed.util;

import org.apache.openejb.config.sys.AbstractService;

import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class Builders {

    public static Properties getProperties(final AbstractService service) {
        final ObjectMap map = new ObjectMap(service);

        final Properties properties = new Properties();
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            final Object value = entry.getValue();
            if (value != null) {
                properties.put(entry.getKey(), value.toString());
            } else {
                properties.put(entry.getKey(), "");
            }
        }
        return properties;
    }


}
