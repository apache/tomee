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
package org.apache.openejb;

import org.apache.openejb.loader.Options;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentContext {
    private final String id;
    private final Map<Class, Object> data = new HashMap<Class, Object>();
    private final Properties properties = new Properties();
    private Options options;

    public DeploymentContext(String id, Options parent) {
        this.id = id;
        this.options = new Options(properties, parent);
    }

    public Properties getProperties() {
        return properties;
    }

    @SuppressWarnings({"unchecked"})
        public <T> T get(Class<T> type) {
        return (T)data.get(type);
    }

    @SuppressWarnings({"unchecked"})
        public <T> T set(Class<T> type, T value) {
        return (T) data.put(type, value);
    }

    public Options getOptions() {
        return options;
    }

    public String getId() {
        return id;
    }
}
