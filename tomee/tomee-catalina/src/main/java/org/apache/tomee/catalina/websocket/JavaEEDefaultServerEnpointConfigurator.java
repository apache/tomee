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
package org.apache.tomee.catalina.websocket;

import org.apache.openejb.loader.SystemInstance;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.websocket.server.DefaultServerEndpointConfigurator;
import org.apache.tomee.catalina.TomcatWebAppBuilder;

import java.util.Map;

public class JavaEEDefaultServerEnpointConfigurator extends DefaultServerEndpointConfigurator {
    private final Map<ClassLoader, InstanceManager> instanceManagers;

    public JavaEEDefaultServerEnpointConfigurator() {
        this.instanceManagers = SystemInstance.get().getComponent(TomcatWebAppBuilder.class).getInstanceManagers();
    }

    @Override
    public <T> T getEndpointInstance(final Class<T> clazz) throws InstantiationException {
        final ClassLoader classLoader = clazz.getClassLoader();
        InstanceManager instanceManager = instanceManagers.get(classLoader);

        if (instanceManager == null) {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            if (tccl != null) {
                instanceManager = instanceManagers.get(tccl);
            }
        }
        // if we have a single app fallback otherwise we don't have enough contextual information here
        if (instanceManager == null && instanceManagers.size() == 1) {
            instanceManager = instanceManagers.values().iterator().next();
        }
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
}
