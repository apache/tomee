/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.rest;

import jakarta.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InternalApplication extends Application {
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private final Set<Object> singletons = new HashSet<Object>();
    private final Application original;

    public InternalApplication(final Application original) {
        this.original = original;
        if (original != null) {
            singletons.addAll(original.getSingletons());
            classes.addAll(original.getClasses());
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    @Override
    public Map<String, Object> getProperties() {
        return original == null ? Collections.<String, Object>emptyMap() : original.getProperties();
    }

    public Application getOriginal() {
        return original;
    }
}
