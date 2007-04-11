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
    protected List<Class> proxyInterface;
    protected RpcContainer beanContainer;
    protected InterfaceType interfaceType;

    protected ProxyInfo() {
    }

    public ProxyInfo(DeploymentInfo depInfo, Object pk, Class intrfc, RpcContainer container, InterfaceType proxyType) {
        this(depInfo, pk, asList(intrfc), container, proxyType);
    }

    public ProxyInfo(DeploymentInfo depInfo, Object pk, List<Class> intrfc, RpcContainer container, InterfaceType proxyType) {
        this.deploymentInfo = depInfo;
        this.primaryKey = pk;
        this.proxyInterface = intrfc;
        this.interfaceType = proxyType;
        this.beanContainer = container;
    }

    public ProxyInfo(DeploymentInfo depInfo, Object pk, List<Class> intrfc, RpcContainer container) {
        this(depInfo, pk, intrfc, container, InterfaceType.UNKNOWN);
    }

    public ProxyInfo(DeploymentInfo depInfo, Object pk, Class intrfc, RpcContainer container) {
        this(depInfo, pk, asList(intrfc), container);
    }

    private static List asList(Object object){
        List list = new ArrayList();
        list.add(object);
        return list;
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
        return proxyInterface.get(0);
    }

    public List<Class> getInterfaces() {
        return proxyInterface;
    }

    public RpcContainer getBeanContainer() {
        return beanContainer;
    }

}
