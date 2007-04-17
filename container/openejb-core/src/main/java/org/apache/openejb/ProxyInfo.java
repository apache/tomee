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
package org.apache.openejb;

import java.util.List;
import java.util.ArrayList;

public class ProxyInfo {

    protected DeploymentInfo deploymentInfo;
    protected Object primaryKey;
    protected List<Class> proxyInterfaces;
    protected RpcContainer beanContainer;
    protected InterfaceType interfaceType;

    protected ProxyInfo() {
    }

    public ProxyInfo(DeploymentInfo deploymentInfo, Object primaryKey, List<Class> interfaces, InterfaceType proxyType) {
        this.deploymentInfo = deploymentInfo;
        this.primaryKey = primaryKey;
        this.proxyInterfaces = interfaces;
        this.interfaceType = proxyType;
        this.beanContainer = (RpcContainer) deploymentInfo.getContainer();
    }

    /**
     * This is the constructor that containers should call.
     * Containers do not know the list of interfaces that should
     * be applied nor do they need to tell the proxy handling
     * code what kind of proxy it should create.
     * 
     * @param depInfo
     * @param pk
     */
    public ProxyInfo(DeploymentInfo depInfo, Object pk) {
        this(depInfo, pk, new ArrayList<Class>(), InterfaceType.UNKNOWN);
    }

    public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public Class getInterface() {
        return proxyInterfaces.get(0);
    }

    public List<Class> getInterfaces() {
        return proxyInterfaces;
    }

    public RpcContainer getBeanContainer() {
        return beanContainer;
    }

}
