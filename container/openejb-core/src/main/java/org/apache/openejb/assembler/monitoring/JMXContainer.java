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

package org.apache.openejb.assembler.monitoring;

import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.apache.openejb.api.internal.Internal;
import org.apache.openejb.api.jmx.Description;
import org.apache.openejb.api.jmx.ManagedAttribute;
import org.apache.openejb.assembler.classic.ContainerInfo;

import java.util.Map;

@Internal
@Description("describe a container")
public class JMXContainer {
    private final Container container;
    private final ContainerInfo info;

    public JMXContainer(final ContainerInfo serviceInfo, final Container service) {
        info = serviceInfo;
        container = service;
    }

    @ManagedAttribute
    @Description("Container id.")
    public String getContainerId() {
        return container.getContainerID().toString();
    }

    @ManagedAttribute
    @Description("Container type.")
    public String getContainerType() {
        return container.getContainerType().name().toLowerCase().replace("_", " ");
    }

    @ManagedAttribute
    @Description("Container managed beans.")
    public String[] getManagedBeans() {
        final BeanContext[] beans = container.getBeanContexts();
        final String[] beanNames = new String[beans.length];
        int i = 0;
        for (final BeanContext bc : beans) {
            beanNames[i++] = new StringBuilder("bean-class: ").append(bc.getBeanClass().getName()).append(", ")
                .append("ejb-name: ").append(bc.getEjbName()).append(", ")
                .append("deployment-id: ").append(bc.getDeploymentID()).append(", ")
                .toString();
        }
        return beanNames;
    }

    @ManagedAttribute
    @Description("Container service.")
    public String getService() {
        return info.service;
    }

    @ManagedAttribute
    @Description("Container class name.")
    public String getClassName() {
        return info.className;
    }

    @ManagedAttribute
    @Description("Container factory method.")
    public String getFactoryMethod() {
        return info.factoryMethod;
    }

    @ManagedAttribute
    @Description("Container properties.")
    public String[] getProperties() {
        final String[] properties = new String[info.properties.size()];
        int i = 0;
        for (final Map.Entry<Object, Object> entry : info.properties.entrySet()) {
            properties[i++] = new StringBuilder(entry.getKey().toString())
                .append(" = ").append(entry.getValue().toString())
                .toString();
        }
        return properties;
    }
}
