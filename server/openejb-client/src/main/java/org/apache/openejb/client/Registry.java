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

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class Registry<T> {

    private Map<String, T> components = new ConcurrentHashMap<String, T>();

    private Map<String, Class> available;

    private String componentType;


    public static <T> Registry<T> create(Class<T> type){
        return new Registry(type);
    }

    private Registry(Class<T> type) {
        componentType = type.getSimpleName();

        try {
            ResourceFinder resourceFinder = new ResourceFinder("META-INF/");
            available = resourceFinder.mapAvailableImplementations(type);
        } catch (IOException e) {
            available = new HashMap();
        }
    }

    public void register(String scheme, T factory) {
        components.put(scheme, factory);
    }

    public T unregister(String scheme) {
        if ("default".equals(scheme)) {
            throw new IllegalArgumentException("Cannot uninstall the default " + componentType);
        }
        return components.remove(scheme);
    }

    public T get(String scheme) {
        T factory = components.get(scheme);

        if (factory == null) {
            factory = load(scheme);
        }

        return factory;
    }

    private T load(String scheme) {

        Class clazz = available.get(scheme);

        if (clazz == null) return null;

        try {
            T factory = (T) clazz.newInstance();

            components.put(scheme, factory);

            return factory;
        } catch (Exception e) {
            throw new IllegalStateException(componentType + " cannot be installed.  Unable to instantiate the class " + clazz.getName() + " for scheme " + scheme);
        }
    }
}
