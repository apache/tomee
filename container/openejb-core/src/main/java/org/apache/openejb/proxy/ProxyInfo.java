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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.proxy;

import org.apache.openejb.EJBComponentType;


public class ProxyInfo extends org.apache.openejb.ProxyInfo {
    private static final long serialVersionUID = 569021597222976175L;
    private final int componentType;
    private final String containerId;
    private final Object primaryKey;

    private final Class remoteInterface;
    private final Class homeInterface;
    private final Class localHomeInterface;
    private final Class localInterface;
    private final Class serviceEndpointInterface;
    private final Class primaryKeyClass;


    public ProxyInfo(ProxyInfo info, Object primaryKey) {
        this.componentType = info.componentType;
        this.containerId = info.containerId;
        this.homeInterface = info.homeInterface;
        this.remoteInterface = info.remoteInterface;
        this.localHomeInterface = info.localHomeInterface;
        this.localInterface = info.localInterface;
        this.serviceEndpointInterface = info.serviceEndpointInterface;
        this.primaryKeyClass = info.primaryKeyClass;
        this.primaryKey = primaryKey;
    }

    public ProxyInfo(
            int componentType,
            String containerId,
            Class homeInterface,
            Class remoteInterface,
            Class localHomeInterface,
            Class localInterface,
            Class serviceEndpointInterface,
            Class primaryKeyClass) {

        this.componentType = componentType;
        this.containerId = containerId;
        this.homeInterface = homeInterface;
        this.remoteInterface = remoteInterface;
        this.localHomeInterface = localHomeInterface;
        this.localInterface = localInterface;
        this.serviceEndpointInterface = serviceEndpointInterface;
        this.primaryKeyClass = primaryKeyClass;
        this.primaryKey = null;
    }

    public String getContainerID() {
        return containerId;
    }

    public boolean isSessionBean() {
        return componentType == EJBComponentType.STATELESS || componentType == EJBComponentType.STATEFUL;
    }

    public boolean isStatefulSessionBean() {
        return componentType == EJBComponentType.STATEFUL;
    }

    public boolean isStatelessSessionBean() {
        return componentType == EJBComponentType.STATELESS;
    }

    public boolean isBMPEntityBean() {
        return componentType == EJBComponentType.BMP_ENTITY;
    }

    public boolean isCMPEntityBean() {
        return componentType == EJBComponentType.CMP_ENTITY;
    }

    public boolean isMessageBean() {
        return componentType == EJBComponentType.MESSAGE_DRIVEN;
    }

    public int getComponentType() {
        return componentType;
    }

    public Class getHomeInterface() {
        return homeInterface;
    }

    public Class getRemoteInterface() {
        return remoteInterface;
    }

    public Class getLocalHomeInterface() {
        return localHomeInterface;
    }

    public Class getLocalInterface() {
        return localInterface;
    }

    public Class getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public Class getPrimaryKeyClass() {
        return primaryKeyClass;
    }

    // TODO: Kill this method
    public Object getPrimaryKey() {
        return primaryKey;
    }

}
