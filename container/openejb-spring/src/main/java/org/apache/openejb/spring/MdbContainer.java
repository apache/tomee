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
package org.apache.openejb.spring;

import java.util.Properties;

import org.springframework.beans.factory.BeanNameAware;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.BeanTypes;
import org.apache.openejb.OpenEJBException;

public class MdbContainer implements ContainerProvider, BeanNameAware {
    private String id;
    private String beanName;
    private String provider;
    private String resourceAdapter;
    private String messageListenerInterface;
    private String activationSpecClass;
    private Integer instanceLimit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(String resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    public String getMessageListenerInterface() {
        return messageListenerInterface;
    }

    public void setMessageListenerInterface(String messageListenerInterface) {
        this.messageListenerInterface = messageListenerInterface;
    }

    public String getActivationSpecClass() {
        return activationSpecClass;
    }

    public void setActivationSpecClass(String activationSpecClass) {
        this.activationSpecClass = activationSpecClass;
    }

    public Integer getInstanceLimit() {
        return instanceLimit;
    }

    public void setInstanceLimit(Integer instanceLimit) {
        this.instanceLimit = instanceLimit;
    }

    public Container getContainerDefinition() throws OpenEJBException {
        Container container = new Container();
        container.setCtype(BeanTypes.MESSAGE);

        if (id != null) {
            container.setId(id);
        } else if (beanName != null) {
            container.setId(beanName);
        } else {
            throw new OpenEJBException("No id defined for MdbContainer");
        }

        if (provider != null) {
            container.setProvider(provider);
        }

        Properties properties = container.getProperties();
        if (resourceAdapter != null) {
            properties.put("ResourceAdapter", resourceAdapter);
        }
        if (messageListenerInterface != null) {
            properties.put("MessageListenerInterface", messageListenerInterface);
        }
        if (activationSpecClass != null) {
            properties.put("ActivationSpecClass", activationSpecClass);
        }
        if (instanceLimit != null) {
            properties.put("InstanceLimit", instanceLimit);
        }
        return container;
    }
}