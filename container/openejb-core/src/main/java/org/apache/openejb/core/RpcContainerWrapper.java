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
package org.apache.openejb.core;

import org.apache.openejb.RpcContainer;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ContainerType;
import org.apache.openejb.core.transaction.TransactionContainer;

import java.lang.reflect.Method;

public class RpcContainerWrapper implements RpcContainer, TransactionContainer {

    private final RpcContainer container;

    public RpcContainerWrapper(RpcContainer container) {
        this.container = container;
    }

    public Object invoke(Object deployID, Method callMethod, Object [] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        return container.invoke(deployID, callMethod.getDeclaringClass(), callMethod, args, primKey);
    }

    public Object invoke(Object deployID, Class callInterface, Method callMethod, Object [] args, Object primKey) throws OpenEJBException {
        return container.invoke(deployID, callInterface, callMethod, args, primKey);
    }

    public ContainerType getContainerType() {
        return container.getContainerType();
    }

    public Object getContainerID() {
        return container.getContainerID();
    }

    public org.apache.openejb.DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return container.getDeploymentInfo(deploymentID);
    }

    public org.apache.openejb.DeploymentInfo [] deployments() {
        return container.deployments();
    }

    public void deploy(org.apache.openejb.DeploymentInfo info) throws OpenEJBException {
        container.deploy(info);
    }

    public void undeploy(org.apache.openejb.DeploymentInfo info) throws OpenEJBException {
        container.undeploy(info);
    }

    public void discardInstance(Object instance, ThreadContext context) {
        ((TransactionContainer) container).discardInstance(instance, context);
    }

    public RpcContainer getContainer() {
        return container;
    }

}
