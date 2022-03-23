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


package org.apache.openejb.rest;

import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ThreadLocalConfiguration extends AbstractRestThreadLocalProxy<Configuration>
    implements Configuration {

    protected ThreadLocalConfiguration() {
        super(Configuration.class);
    }

    @Override
    public RuntimeType getRuntimeType() {
        return get().getRuntimeType();
    }

    @Override
    public Map<String, Object> getProperties() {
        return get().getProperties();
    }

    @Override
    public Object getProperty(final String name) {
        return get().getProperty(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return get().getPropertyNames();
    }

    @Override
    public boolean isEnabled(final Feature feature) {
        return get().isEnabled(feature);
    }

    @Override
    public boolean isEnabled(final Class<? extends Feature> featureClass) {
        return get().isEnabled(featureClass);
    }

    @Override
    public boolean isRegistered(final Object component) {
        return get().isRegistered(component);
    }

    @Override
    public boolean isRegistered(final Class<?> componentClass) {
        return get().isRegistered(componentClass);
    }

    @Override
    public Map<Class<?>, Integer> getContracts(final Class<?> componentClass) {
        return get().getContracts(componentClass);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return get().getClasses();
    }

    @Override
    public Set<Object> getInstances() {
        return get().getInstances();
    }
}
