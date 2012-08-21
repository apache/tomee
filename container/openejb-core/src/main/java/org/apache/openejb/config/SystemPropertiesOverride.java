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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * Anything starting with "openejb" or "tomee" trumps other properties
 * so "openejb.foo" always beats "foo"
 *
 * @version $Rev$ $Date$
 */
public class SystemPropertiesOverride implements DynamicDeployer {

    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {

        final Properties properties = new Properties();

        for (Map.Entry<Object, Object> entry : SystemInstance.get().getProperties().entrySet()) {
            final String key = entry.getKey().toString();

            for (String prefix : Arrays.asList("openejb.", "tomee.")) {
                if (key.startsWith(prefix)) {
                    final String property = key.substring(prefix.length());
                    properties.put(property, entry.getValue());
                }
            }
        }

        SystemInstance.get().getProperties().putAll(properties);

        return appModule;
    }
}
