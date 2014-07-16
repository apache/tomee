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
package org.apache.openejb.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version $Rev$ $Date$
 */
public class Registry<T> {

    private final Map<String, T> components = new ConcurrentHashMap<String, T>();

    private Map<String, Class> available;

    private final String componentType;

    public static <T> Registry<T> create(final Class<T> type) {
        return new Registry<T>(type);
    }

    private Registry(final Class<T> type) {
        componentType = type.getSimpleName();

        try {
            final ResourceFinder resourceFinder = new ResourceFinder("META-INF/");
            available = resourceFinder.mapAvailableImplementations(type);
        } catch (IOException e) {
            available = new HashMap<String, Class>();
        }
    }

    public void register(final String scheme, final T factory) {
        components.put(scheme, factory);
    }

    public T unregister(final String scheme) {
        if ("default".equals(scheme)) {
            throw new IllegalArgumentException("Cannot uninstall the default " + componentType);
        }
        return components.remove(scheme);
    }

    public T get(final String scheme) {
        T factory = components.get(scheme);

        if (factory == null) {
            factory = load(scheme);
        }

        return factory;
    }

    @SuppressWarnings("unchecked")
    private T load(final String scheme) {

        final Class clazz = available.get(scheme);

        if (clazz == null) {
            return null;
        }

        try {
            final T factory = (T) clazz.newInstance();

            components.put(scheme, factory);

            return factory;
        } catch (Exception e) {
            throw new IllegalStateException(componentType + " cannot be installed.  Unable to instantiate the class " + clazz.getName() + " for scheme " + scheme);
        }
    }
}
