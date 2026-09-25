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

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jakarta.ejb.EJB;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.Stateless;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.InvalidPropertyException;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.naming.Context;
import javax.transaction.xa.XAResource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * A resource adapter may deliver messages as soon as an endpoint is activated, for example
 * messages that were left on a destination when the server stopped. The session beans an
 * MDB calls must be started by then.
 */
@RunWith(Parameterized.class)
public class MdbActivationOrderTest {

    @After
    public void tearDown() {
        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        if (assembler != null) {
            assembler.destroy();
        }
        SystemInstance.reset();
    }

    @Parameterized.Parameters(name = "pool={0}")
    public static Collection<Object[]> pool() {
        return List.of(new Object[]{false}, new Object[]{true});
    }

    @Parameterized.Parameter
    public boolean pool;

    @Test
    public void pendingMessageIsDeliveredToStartedSessionBeans() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final Resource resourceAdapter = new Resource("PendingRA");
        resourceAdapter.setClassName(PendingMessageResourceAdapter.class.getName());
        assembler.createResource(config.configureService(resourceAdapter, ResourceInfo.class));

        final Container container = new Container("PendingMdbContainer", "MESSAGE", null);
        container.getProperties().setProperty("ResourceAdapter", "PendingRA");
        container.getProperties().setProperty("MessageListenerInterface", PendingMessageListener.class.getName());
        container.getProperties().setProperty("ActivationSpecClass", PendingMessageActivationSpec.class.getName());
        container.getProperties().setProperty("Pool", Boolean.toString(pool));
        assembler.createContainer(config.configureService(container, MdbContainerInfo.class));

        final EjbJar ejbJar = new EjbJar("activation-order");
        ejbJar.addEnterpriseBean(new StatelessBean(Greeter.class));
        ejbJar.addEnterpriseBean(new MessageDrivenBean(ListenerBean.class));

        ListenerBean.received.clear();
        ListenerBean.failure = null;

        assembler.createApplication(config.configureApplication(ejbJar));

        if (ListenerBean.failure != null) {
            throw new AssertionError("pending message failed", ListenerBean.failure);
        }
        assertEquals(List.of("Hello pending"), ListenerBean.received);
    }

    @Stateless
    public static class Greeter {
        public String greet(final String name) {
            return "Hello " + name;
        }
    }

    @MessageDriven
    public static class ListenerBean implements PendingMessageListener {
        static final List<String> received = new ArrayList<>();
        static Throwable failure;

        @EJB
        private Greeter greeter;

        @Override
        public void onMessage(final String message) {
            try {
                received.add(greeter.greet(message));
            } catch (final RuntimeException e) {
                failure = e;
            }
        }
    }

    public interface PendingMessageListener {
        void onMessage(String message);
    }

    public static class PendingMessageResourceAdapter implements jakarta.resource.spi.ResourceAdapter {
        @Override
        public void start(final BootstrapContext bootstrapContext) {
        }

        @Override
        public void stop() {
        }

        @Override
        public void endpointActivation(final MessageEndpointFactory factory, final ActivationSpec spec) throws ResourceException {
            final MessageEndpoint endpoint = factory.createEndpoint(null);
            try {
                endpoint.beforeDelivery(PendingMessageListener.class.getMethod("onMessage", String.class));
                ((PendingMessageListener) endpoint).onMessage("pending");
                endpoint.afterDelivery();
            } catch (final NoSuchMethodException e) {
                throw new ResourceException(e);
            } finally {
                endpoint.release();
            }
        }

        @Override
        public void endpointDeactivation(final MessageEndpointFactory factory, final ActivationSpec spec) {
        }

        @Override
        public XAResource[] getXAResources(final ActivationSpec[] specs) {
            return new XAResource[0];
        }
    }

    public static class PendingMessageActivationSpec implements ActivationSpec {
        private jakarta.resource.spi.ResourceAdapter resourceAdapter;

        @Override
        public void validate() throws InvalidPropertyException {
        }

        @Override
        public jakarta.resource.spi.ResourceAdapter getResourceAdapter() {
            return resourceAdapter;
        }

        @Override
        public void setResourceAdapter(final jakarta.resource.spi.ResourceAdapter resourceAdapter) {
            this.resourceAdapter = resourceAdapter;
        }
    }
}
