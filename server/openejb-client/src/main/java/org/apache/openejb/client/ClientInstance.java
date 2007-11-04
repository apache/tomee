/**
 *
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
package org.apache.openejb.client;

import java.util.Properties;
import java.util.HashMap;

public class ClientInstance {
    /**
     * The time the client instance class was initialized
     */
    private final long startTime = System.currentTimeMillis();

    /**
     * Properties that have to be away from System (i.e. {@link System#setProperty(String, String)} must not be called)
     */
    private final Properties internalProperties = new Properties();

    /**
     * Global component registry
     */
    private final HashMap<Class, Object> components;

    private ClientInstance(Properties properties) throws Exception {
        this.components = new HashMap<Class, Object>();

        this.internalProperties.putAll(System.getProperties());
        this.internalProperties.putAll(properties);
    }

    public long getStartTime() {
        return startTime;
    }

    public Properties getProperties() {
        return internalProperties;
    }

    public String getProperty(String key) {
        return internalProperties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return internalProperties.getProperty(key, defaultValue);
    }

    public Object setProperty(String key, String value) {
        return internalProperties.setProperty(key, value);
    }

    /**
     * @param propName property name
     *
     * @return true when property is set; false otherwise
     */
    public boolean hasProperty(String propName) {
        return this.internalProperties.get(propName) != null;
    }

    /**
     * Gets a global component instance.
     *
     * @param type the class type of the component - required
     * @return the object associated with the class type or null
     * @throws IllegalStateException of the component isn't found
     */
    @SuppressWarnings({"unchecked"})
    public <T> T getComponent(Class<T> type) {
        return (T)components.get(type);
    }

    /**
     * Removes a global component instance.
     *
     * @param type the class type of the component - required
     * @return the component instance or null if component type was not registered
     */
    @SuppressWarnings({"unchecked"})
    public <T> T removeComponent(Class<T> type) {
        return (T)components.remove(type);
    }

    /**
     * Registers a component instance with the client, so it may be reference globally.
     *
     * @param type the class type of the component - required
     * @param component the component instance
     */
    @SuppressWarnings({"unchecked"})
    public <T> T setComponent(Class<T> type, T component) {
        return (T)components.put(type, component);
    }

    private static ClientInstance client;

    static {
        try {
            client = new ClientInstance(System.getProperties());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create default instance of SystemInstance", e);
        }
    }

    public static ClientInstance get() {
        return client;
    }
}
