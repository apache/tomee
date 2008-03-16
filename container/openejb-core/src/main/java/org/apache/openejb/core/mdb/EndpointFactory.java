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
package org.apache.openejb.core.mdb;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;

import javax.resource.spi.UnavailableException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class EndpointFactory implements MessageEndpointFactory {
    private final ActivationSpec activationSpec;
    private final MdbContainer container;
    private final CoreDeploymentInfo deploymentInfo;
    private final MdbInstanceFactory instanceFactory;
    private final ClassLoader classLoader;
    private final Class[] interfaces;
    private final boolean txRecovery;

    public EndpointFactory(ActivationSpec activationSpec, MdbContainer container, CoreDeploymentInfo deploymentInfo, MdbInstanceFactory instanceFactory, boolean txRecovery) {
        this.activationSpec = activationSpec;
        this.container = container;
        this.deploymentInfo = deploymentInfo;
        this.instanceFactory = instanceFactory;
        classLoader = container.getMessageListenerInterface().getClassLoader();
        interfaces = new Class[]{container.getMessageListenerInterface(), MessageEndpoint.class};
        this.txRecovery = txRecovery;
    }

    public ActivationSpec getActivationSpec() {
        return activationSpec;
    }

    public MdbInstanceFactory getInstanceFactory() {
        return instanceFactory;
    }

    public MessageEndpoint createEndpoint(XAResource xaResource) throws UnavailableException {
        if (txRecovery) {
            xaResource = new WrapperNamedXAResource(xaResource, container.getContainerID().toString());
        }
        EndpointHandler endpointHandler = new EndpointHandler(container, deploymentInfo, instanceFactory, xaResource);
        MessageEndpoint messageEndpoint = (MessageEndpoint) Proxy.newProxyInstance(classLoader, interfaces, endpointHandler);
        return messageEndpoint;
    }

    public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
        byte transactionAttribute = deploymentInfo.getTransactionAttribute(method);
        return DeploymentInfo.TX_REQUIRED == transactionAttribute;
    }
}
