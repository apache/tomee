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
package org.apache.tomee.catalina.websocket;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.websocket.server.DefaultServerEndpointConfigurator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JavaEEDefaultServerEnpointConfigurator extends DefaultServerEndpointConfigurator {
    private static final Map<ClassLoader, InstanceManager> INSTANCE_MANAGERS = new ConcurrentHashMap<ClassLoader, InstanceManager>();

    @Override
    public <T> T getEndpointInstance(final Class<T> clazz) throws InstantiationException {
        final InstanceManager instanceManager = INSTANCE_MANAGERS.get(clazz.getClassLoader());
        if (instanceManager == null) {
            return super.getEndpointInstance(clazz);
        }

        try {
            return clazz.cast(instanceManager.newInstance(clazz));
        } catch (final Exception e) {
            if (InstantiationException.class.isInstance(e)) {
                throw InstantiationException.class.cast(e);
            }
            throw new InstantiationException(e.getMessage());
        }
    }

    public static void registerInstanceManager(final ClassLoader loader, final InstanceManager manager) {
        INSTANCE_MANAGERS.put(loader, manager);
    }

    public static void unregisterInstanceManager(final ClassLoader loader) {
        INSTANCE_MANAGERS.remove(loader);
    }
}
