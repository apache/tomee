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

import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.transaction.TransactionType;

import javax.resource.spi.UnavailableException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import javax.management.ObjectName;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class EndpointFactory implements MessageEndpointFactory {
    private final ActivationSpec activationSpec;
    private final MdbContainer container;
    private final CoreDeploymentInfo deploymentInfo;
    private final MdbInstanceFactory instanceFactory;
    private final ClassLoader classLoader;
    private final Class[] interfaces;
    private final XAResourceWrapper xaResourceWrapper;
    protected final List<ObjectName> jmxNames = new ArrayList<ObjectName>();
    
    public EndpointFactory(ActivationSpec activationSpec, MdbContainer container, CoreDeploymentInfo deploymentInfo, MdbInstanceFactory instanceFactory, XAResourceWrapper xaResourceWrapper) {
        this.activationSpec = activationSpec;
        this.container = container;
        this.deploymentInfo = deploymentInfo;
        this.instanceFactory = instanceFactory;
        classLoader = container.getMessageListenerInterface().getClassLoader();
        interfaces = new Class[]{container.getMessageListenerInterface(), MessageEndpoint.class};
        this.xaResourceWrapper = xaResourceWrapper;
    }

    public ActivationSpec getActivationSpec() {
        return activationSpec;
    }

    public MdbInstanceFactory getInstanceFactory() {
        return instanceFactory;
    }

    public MessageEndpoint createEndpoint(XAResource xaResource) throws UnavailableException {
        if (xaResource != null && xaResourceWrapper != null) {
            xaResource = xaResourceWrapper.wrap(xaResource, container.getContainerID().toString());
        }
        EndpointHandler endpointHandler = new EndpointHandler(container, deploymentInfo, instanceFactory, xaResource);
        MessageEndpoint messageEndpoint = (MessageEndpoint) Proxy.newProxyInstance(classLoader, interfaces, endpointHandler);
        return messageEndpoint;
    }

    public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
        TransactionType transactionType = deploymentInfo.getTransactionType(method);
        return TransactionType.Required == transactionType;
    }
}
