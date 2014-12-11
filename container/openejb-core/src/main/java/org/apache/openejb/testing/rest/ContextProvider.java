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
package org.apache.openejb.testing.rest;

import org.apache.openejb.rest.RESTResourceFinder;

import java.util.HashMap;
import java.util.Map;

public class ContextProvider implements RESTResourceFinder {
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final RESTResourceFinder defaults;

    public ContextProvider() {
        this(null);
    }

    public ContextProvider(final RESTResourceFinder fallback) {
        this.defaults = fallback;
    }

    @Override
    public <T> T find(final Class<T> clazz) {
        final Object obj = instances.get(clazz);
        if (obj == null) {
            return defaults != null ? defaults.find(clazz) : null;
        }
        return clazz.cast(obj);
    }

    public ContextProvider deregister(final Class<?> contextType) {
        instances.remove(contextType);
        return this;
    }

    public <T> ContextProvider register(final Class<T> contextType, final T instance) {
        instances.put(contextType, instance);
        return this;
    }
}
