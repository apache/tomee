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

public class ProxyInfo {

    protected DeploymentInfo deploymentInfo;
    protected Object primaryKey;
    protected Class type;
    protected RpcContainer beanContainer;

    protected ProxyInfo() {
    }

    public ProxyInfo(DeploymentInfo depInfo, Object pk, Class intrfc, RpcContainer container) {
        deploymentInfo = depInfo;
        primaryKey = pk;
        type = intrfc;
        beanContainer = container;
    }

    public ProxyInfo(DeploymentInfo depInfo, Object pk, boolean isLocalInterface, RpcContainer container) {
        this.deploymentInfo = depInfo;
        this.primaryKey = pk;
        this.beanContainer = container;
        if (isLocalInterface) {
            this.type = deploymentInfo.getLocalInterface();
        } else {
            this.type = deploymentInfo.getRemoteInterface();
        }
    }

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public Class getInterface() {
        return type;
    }

    public RpcContainer getBeanContainer() {
        return beanContainer;
    }
}
