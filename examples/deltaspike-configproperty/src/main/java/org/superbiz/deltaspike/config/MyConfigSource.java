/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.deltaspike.config;

import org.apache.deltaspike.core.impl.config.BaseConfigSource;
import org.apache.deltaspike.core.util.PropertyFileUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class MyConfigSource extends BaseConfigSource {

    private static final Logger LOGGER = Logger.getLogger(MyConfigSource.class.getName());
    private static final String MY_CONF_FILE_NAME = "my-app-config.properties";

    private final Properties properties;

    public MyConfigSource() {
        final Enumeration<URL> in;
        try {
            in = Thread.currentThread().getContextClassLoader().getResources(MY_CONF_FILE_NAME);
        } catch (IOException e) {
            throw new IllegalArgumentException("can't find " + MY_CONF_FILE_NAME, e);
        }

        properties = new Properties();

        while (in.hasMoreElements()) {
            final Properties currentProps = PropertyFileUtils.loadProperties(in.nextElement());
            for (Map.Entry<Object, Object> key : currentProps.entrySet()) { // some check
                if (properties.containsKey(key.getKey().toString())) {
                    LOGGER.warning("found " + key.getKey() + " multiple times, only one value will be available.");
                }
            }
            properties.putAll(currentProps);
        }

        initOrdinal(401); // before other sources
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap(); // not scannable
    }

    @Override
    public String getPropertyValue(String key) {
        return properties.getProperty(key);
    }

    @Override
    public String getConfigName() {
        return MY_CONF_FILE_NAME;
    }

    @Override
    public boolean isScannable() {
        return false;
    }
}
