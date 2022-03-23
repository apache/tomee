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

package org.apache.openejb.core.mdb;

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;

import javax.management.ObjectName;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.UnavailableException;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EndpointFactory implements MessageEndpointFactory {

    private final ActivationSpec activationSpec;
    private final BaseMdbContainer container;
    private final BeanContext beanContext;
    private final MdbInstanceFactory instanceFactory;
    private final MdbInstanceManager instanceManager;
    private final ClassLoader classLoader;
    private final Class[] interfaces;
    private final XAResourceWrapper xaResourceWrapper;
    protected final List<ObjectName> jmxNames = new ArrayList<>();
    private final Class<?> proxy;
    private final boolean usePool;

    public EndpointFactory(final ActivationSpec activationSpec, final BaseMdbContainer container, final BeanContext beanContext, final MdbInstanceFactory instanceFactory, final MdbInstanceManager instanceManager, final XAResourceWrapper xaResourceWrapper, boolean usePool) {
        this.activationSpec = activationSpec;
        this.container = container;
        this.beanContext = beanContext;
        this.instanceFactory = instanceFactory;
        this.instanceManager = instanceManager;
        classLoader = container.getMessageListenerInterface().getClassLoader();
        interfaces = new Class[]{container.getMessageListenerInterface(), MessageEndpoint.class};
        this.xaResourceWrapper = xaResourceWrapper;
        this.usePool = usePool;
        final BeanContext.ProxyClass proxyClass = beanContext.get(BeanContext.ProxyClass.class);
        if (proxyClass == null) {
            proxy = LocalBeanProxyFactory.createProxy(beanContext.getBeanClass(), beanContext.getClassLoader(), interfaces);
            beanContext.set(BeanContext.ProxyClass.class, new BeanContext.ProxyClass(beanContext, interfaces));
        } else {
            proxy = proxyClass.getProxy();
        }
    }

    public ActivationSpec getActivationSpec() {
        return activationSpec;
    }

    public MdbInstanceFactory getInstanceFactory() {
        return instanceFactory;
    }

    @Override
    public MessageEndpoint createEndpoint(XAResource xaResource) throws UnavailableException {
        if (xaResource != null && xaResourceWrapper != null) {
            xaResource = xaResourceWrapper.wrap(xaResource, container.getContainerID().toString());
        }

        InvocationHandler endpointHandler = null;
        if (usePool) {
            endpointHandler = new PoolEndpointHandler(container, beanContext, instanceManager, xaResource);
        } else {
            endpointHandler = new EndpointHandler(container, beanContext, instanceFactory, xaResource);
        }

        try {
            return (MessageEndpoint) LocalBeanProxyFactory.constructProxy(proxy, endpointHandler);
        } catch (final InternalError e) { // should be useless
            //try to create the proxy with tccl once again.
            try {
                return MessageEndpoint.class.cast(LocalBeanProxyFactory.newProxyInstance(Thread.currentThread().getContextClassLoader(), endpointHandler, beanContext.getBeanClass(), interfaces));
            } catch (final InternalError ie) {
                try {
                    return MessageEndpoint.class.cast(LocalBeanProxyFactory.newProxyInstance(classLoader, endpointHandler, beanContext.getBeanClass(), interfaces));
                } catch (final InternalError ie2) {
                    // no-op
                }
            }
            throw e;
        }
    }

    @Override
    public MessageEndpoint createEndpoint(final XAResource xaResource, final long timeout) throws UnavailableException {
        if (timeout <= 0) {
            return createEndpoint(xaResource);
        }

        final long end = System.currentTimeMillis() + timeout;
        MessageEndpoint messageEndpoint = null;

        while (System.currentTimeMillis() <= end) {
            try {
                messageEndpoint = createEndpoint(xaResource);
                break;
            } catch (final Exception ex) {
                // ignore so we can keep trying
            }
        }

        if (messageEndpoint != null) {
            return messageEndpoint;
        } else {
            throw new UnavailableException("Unable to create end point within the specified timeout " + timeout);
        }
    }

    @Override
    public boolean isDeliveryTransacted(final Method method) throws NoSuchMethodException {
        final TransactionType transactionType = beanContext.getTransactionType(method);
        return TransactionType.Required == transactionType;
    }
}
