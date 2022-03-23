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
package org.apache.openejb.rest;

import org.apache.openejb.loader.SystemInstance;

import jakarta.ws.rs.core.Application;
import java.util.Map;
import java.util.Set;

public class ThreadLocalApplication extends Application {
    private final ThreadLocal<Application> infos = new ThreadLocal<>();

    @Override
    public Set<Class<?>> getClasses() {
        return super.getClasses();
    }

    @Override
    public Set<Object> getSingletons() {
        return super.getSingletons();
    }

    @Override
    public Map<String, Object> getProperties() {
        return super.getProperties();
    }

    public Application get() {
        Application t = infos.get();
        if (t == null) {
            t = find();
        }
        return t;
    }

    public Application find() {
        final RESTResourceFinder finder = SystemInstance.get().getComponent(RESTResourceFinder.class);
        if (finder != null) {
            return finder.find(Application.class);
        }
        return null;
    }

    public void remove() {
        infos.remove();
    }

    public void set(final Application value) {
        infos.set(value);
    }

}
