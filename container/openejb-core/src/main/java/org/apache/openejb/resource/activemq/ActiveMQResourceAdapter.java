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

package org.apache.openejb.resource.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.ra.ActiveMQConnectionRequestInfo;
import org.apache.activemq.ra.ActiveMQEndpointActivationKey;
import org.apache.activemq.ra.ActiveMQEndpointWorker;
import org.apache.activemq.ra.ActiveMQManagedConnection;
import org.apache.activemq.ra.MessageActivationSpec;
import org.apache.openejb.BeanContext;
import org.apache.openejb.core.mdb.MdbContainer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.resource.AutoConnectionTracker;
import org.apache.openejb.resource.activemq.jms2.TomEEConnectionFactory;
import org.apache.openejb.resource.activemq.jms2.TomEEManagedConnectionProxy;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URISupport;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.reflection.Reflections;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.NamingException;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static javax.management.MBeanOperationInfo.ACTION;

@SuppressWarnings("UnusedDeclaration")
public class ActiveMQResourceAdapter extends org.apache.activemq.ra.ActiveMQResourceAdapter {

    private String dataSource;
    private String useDatabaseLock;
    private String startupTimeout = "60000";
    private BootstrapContext bootstrapContext;
    private final Map<BeanContext, ObjectName> mbeanNames = new ConcurrentHashMap<>();

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(final String dataSource) {
        this.dataSource = dataSource;
    }

    public void setUseDatabaseLock(final String useDatabaseLock) {
        this.useDatabaseLock = useDatabaseLock;
    }

    public int getStartupTimeout() {
        return Integer.parseInt(this.startupTimeout);
    }

    public void setStartupTimeout(final Duration startupTimeout) {
        if (startupTimeout.getUnit() == null) {
            startupTimeout.setUnit(TimeUnit.MILLISECONDS);
        }
        this.startupTimeout = String.valueOf(TimeUnit.MILLISECONDS.convert(startupTimeout.getTime(), startupTimeout.getUnit()));
    }

    @Override
    public void setServerUrl(final String url) {
        super.setServerUrl(url);
    }

    @Override
    public void start(final BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {

        this.bootstrapContext = bootstrapContext;
        final String brokerXmlConfig = getBrokerXmlConfig();
        super.setBrokerXmlConfig(null);
        super.start(bootstrapContext);

        final Properties properties = new Properties();

        if (null != this.dataSource) {
            properties.put("DataSource", this.dataSource);
        }

        if (null != this.useDatabaseLock) {
            properties.put("UseDatabaseLock", this.useDatabaseLock);
        }

        if (null != this.startupTimeout) {
            properties.put("StartupTimeout", this.startupTimeout);
        }

        // prefix server uri with 'broker:' so our broker factory is used
        if (brokerXmlConfig != null && !brokerXmlConfig.trim().isEmpty()) {

            try {

                if (brokerXmlConfig.startsWith("broker:")) {

                    final URISupport.CompositeData compositeData = URISupport.parseComposite(URLs.uri(brokerXmlConfig));

                    if (!compositeData.getParameters().containsKey("persistent")) {
                        //Override default - Which is 'true'
                        //noinspection unchecked
                        compositeData.getParameters().put("persistent", "false");
                    }

                    if ("false".equalsIgnoreCase(compositeData.getParameters().get("persistent").toString())) {
                        properties.remove("DataSource"); // no need
                    }

                    setBrokerXmlConfig(ActiveMQFactory.getBrokerMetaFile() + compositeData.toURI());
                } else if (brokerXmlConfig.toLowerCase(Locale.ENGLISH).startsWith("xbean:")) {
                    setBrokerXmlConfig(ActiveMQFactory.getBrokerMetaFile() + brokerXmlConfig);
                }

            } catch (final URISyntaxException e) {
                throw new ResourceAdapterInternalException("Invalid BrokerXmlConfig", e);
            }

            createInternalBroker(brokerXmlConfig, properties);
        }
    }

    private void createInternalBroker(final String brokerXmlConfig, final Properties properties) {
        ActiveMQFactory.setThreadProperties(properties);

        try {
            //The returned broker should be started, but calling start is harmless.
            //We do not need to track the instance as the factory takes care of this.
            ActiveMQFactory.createBroker(URLs.uri(getBrokerXmlConfig())).start();
        } catch (final Exception e) {
            Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQResourceAdapter.class).getChildLogger("service").fatal("Failed to start ActiveMQ", e);
        } finally {
            ActiveMQFactory.setThreadProperties(null);

            // reset brokerXmlConfig
            if (brokerXmlConfig != null) {
                setBrokerXmlConfig(brokerXmlConfig);
            }
        }
    }

    @Override
    public void endpointActivation(final MessageEndpointFactory endpointFactory, final ActivationSpec activationSpec) throws ResourceException {
        final BeanContext current = MdbContainer.current();
        if (current != null && "false".equalsIgnoreCase(current.getProperties().getProperty("MdbActiveOnStartup"))) {
            if (!equals(activationSpec.getResourceAdapter())) {
                throw new ResourceException("Activation spec not initialized with this ResourceAdapter instance (" + activationSpec.getResourceAdapter() + " != " + this + ")");
            }
            if (!(activationSpec instanceof MessageActivationSpec)) {
                throw new NotSupportedException("That type of ActivationSpec not supported: " + activationSpec.getClass());
            }

            final ActiveMQEndpointActivationKey key = new ActiveMQEndpointActivationKey(endpointFactory, MessageActivationSpec.class.cast(activationSpec));
            Map.class.cast(Reflections.get(this, "endpointWorkers")).put(key, new ActiveMQEndpointWorker(this, key) {
            });
            // we dont want that worker.start();
        } else {
            super.endpointActivation(endpointFactory, activationSpec);
        }

        if (current != null) {
            addJMxControl(current, current.getProperties().getProperty("MdbJMXControl"));
        }
    }

    private void addJMxControl(final BeanContext current, final String name) throws ResourceException {
        if (name == null || "false".equalsIgnoreCase(name)) {
            return;
        }

        final ActiveMQEndpointWorker worker = getWorker(current);
        final ObjectName jmxName;
        try {
            jmxName = "true".equalsIgnoreCase(name) ? new ObjectNameBuilder()
                    .set("J2EEServer", "openejb")
                    .set("J2EEApplication", null)
                    .set("EJBModule", current.getModuleID())
                    .set("StatelessSessionBean", current.getEjbName())
                    .set("j2eeType", "control")
                    .set("name", current.getEjbName())
                    .build() : new ObjectName(name);
        } catch (final MalformedObjectNameException e) {
            throw new IllegalArgumentException(e);
        }
        mbeanNames.put(current, jmxName);

        LocalMBeanServer.registerSilently(new MdbJmxControl(worker), jmxName);
        log.info("Deployed MDB control for " + current.getDeploymentID() + " on " + jmxName);
    }

    @Override
    public void endpointDeactivation(final MessageEndpointFactory endpointFactory, final ActivationSpec activationSpec) {
        final BeanContext current = MdbContainer.current();
        if (current != null && "true".equalsIgnoreCase(current.getProperties().getProperty("MdbJMXControl"))) {
            LocalMBeanServer.unregisterSilently(mbeanNames.remove(current));
            log.info("Undeployed MDB control for " + current.getDeploymentID());
        }
        super.endpointDeactivation(endpointFactory, activationSpec);
    }

    private ActiveMQEndpointWorker getWorker(final BeanContext beanContext) throws ResourceException {
        final Map<ActiveMQEndpointActivationKey, ActiveMQEndpointWorker> workers = Map.class.cast(Reflections.get(
                MdbContainer.class.cast(beanContext.getContainer()).getResourceAdapter(), "endpointWorkers"));
        for (final Map.Entry<ActiveMQEndpointActivationKey, ActiveMQEndpointWorker> entry : workers.entrySet()) {
            if (entry.getKey().getMessageEndpointFactory() == beanContext.getContainerData()) {
                return entry.getValue();
            }
        }
        throw new IllegalStateException("No worker for " + beanContext.getDeploymentID());
    }

    @Override
    public BootstrapContext getBootstrapContext() {
        return this.bootstrapContext;
    }

    @Override
    public void stop() {

        Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQResourceAdapter.class).getChildLogger("service").info("Stopping ActiveMQ");

        final Thread stopThread = new Thread("ActiveMQResourceAdapter stop") {

            @Override
            public void run() {
                try {
                    stopImpl();
                } catch (final Throwable t) {
                    Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQResourceAdapter.class).getChildLogger("service").error("ActiveMQ shutdown failed", t);
                }
            }
        };

        stopThread.setDaemon(true);
        stopThread.start();

        int timeout = 60000;

        try {
            timeout = Integer.parseInt(this.startupTimeout);
        } catch (final Throwable e) {
            //Ignore
        }

        try {
            //Block for a maximum of timeout milliseconds waiting for this thread to die.
            stopThread.join(timeout);
        } catch (final InterruptedException ex) {
            Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQResourceAdapter.class).getChildLogger("service").warning("Gave up on ActiveMQ shutdown after " + timeout + "ms", ex);
        }
    }

    @Override
    public ActiveMQConnection makeConnection(final MessageActivationSpec activationSpec) throws JMSException {
        if (TomEEMessageActivationSpec.class.isInstance(activationSpec)) {
            final TomEEMessageActivationSpec s = TomEEMessageActivationSpec.class.cast(activationSpec);
            if (s.getConnectionFactoryLookup() != null) {
                try {
                    final Object lookup = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext()
                            .lookup("openejb:Resource/" + s.getConnectionFactoryLookup());
                    if (!ActiveMQConnectionFactory.class.isInstance(lookup)) {
                        final org.apache.activemq.ra.ActiveMQConnectionFactory connectionFactory = org.apache.activemq.ra.ActiveMQConnectionFactory.class.cast(lookup);
                        Connection connection = connectionFactory.createConnection();
                        if (Proxy.isProxyClass(connection.getClass())) { // not great, we should find a better want without bypassing ra layer
                            final InvocationHandler invocationHandler = Proxy.getInvocationHandler(connection);
                            if (AutoConnectionTracker.ConnectionInvocationHandler.class.isInstance(invocationHandler)) {
                                final Object handle = Reflections.get(invocationHandler, "handle");
                                if (TomEEManagedConnectionProxy.class.isInstance(handle)) {
                                    final ActiveMQManagedConnection c = ActiveMQManagedConnection.class.cast(Reflections.get(handle, "connection"));
                                    final ActiveMQConnection physicalConnection = ActiveMQConnection.class.cast(Reflections.get(c, "physicalConnection"));
                                    final RedeliveryPolicy redeliveryPolicy = activationSpec.redeliveryPolicy();
                                    if (redeliveryPolicy != null) {
                                        physicalConnection.setRedeliveryPolicy(redeliveryPolicy);
                                    }
                                    return physicalConnection;
                                }
                            }
                        }

                        /*
                        final RedeliveryPolicy redeliveryPolicy = activationSpec.redeliveryPolicy();
                        if (redeliveryPolicy != null) {
                            physicalConnection.setRedeliveryPolicy(redeliveryPolicy);
                        }
                        */
                        return null;
                    }
                } catch (final ClassCastException cce) {
                    throw new java.lang.IllegalStateException(cce);
                } catch (final NamingException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return super.makeConnection(activationSpec);
    }

    @Override
    protected ActiveMQConnectionFactory createConnectionFactory(final ActiveMQConnectionRequestInfo connectionRequestInfo, final MessageActivationSpec activationSpec) {
        if (TomEEMessageActivationSpec.class.isInstance(activationSpec)) {
            final TomEEMessageActivationSpec s = TomEEMessageActivationSpec.class.cast(activationSpec);
            if (s.getConnectionFactoryLookup() != null) {
                try {
                    final Object lookup = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext()
                            .lookup("openejb:Resource/" + s.getConnectionFactoryLookup());
                    if (ActiveMQConnectionFactory.class.isInstance(lookup)) {
                        return ActiveMQConnectionFactory.class.cast(lookup);
                    }
                    return ActiveMQConnectionFactory.class.cast(lookup); // already handled
                } catch (final NamingException e) {
                    throw new IllegalArgumentException("");
                }
            }
        }

        final ActiveMQConnectionFactory factory = new TomEEConnectionFactory();
        connectionRequestInfo.configure(factory, activationSpec);
        return factory;
    }

    private void stopImpl() throws Exception {
        super.stop();
        final Collection<BrokerService> brokers = ActiveMQFactory.getBrokers();
        final Iterator<BrokerService> it = brokers.iterator();
        while (it.hasNext()) {
            final BrokerService bs = it.next();
            try {
                bs.stop();
                bs.waitUntilStopped();
            } catch (final Throwable t) {
                //Ignore
            }
            it.remove();
        }
        stopScheduler();
        Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQResourceAdapter.class).getChildLogger("service").info("Stopped ActiveMQ broker");
    }

    private static void stopScheduler() {
        try {
            final Class<?> clazz = Class.forName("org.apache.kahadb.util.Scheduler");
            final Method method = clazz.getMethod("shutdown");
            method.invoke(null);
        } catch (final Throwable e) {
            //Ignore
        }
    }

    public static class MdbJmxControl implements DynamicMBean {
        private static final AttributeList ATTRIBUTE_LIST = new AttributeList();
        private static final MBeanInfo INFO = new MBeanInfo(
                "org.apache.openejb.resource.activemq.ActiveMQResourceAdapter.MdbJmxControl",
                "Allows to control a MDB (start/stop)",
                new MBeanAttributeInfo[0],
                new MBeanConstructorInfo[0],
                new MBeanOperationInfo[]{
                        new MBeanOperationInfo("start", "Ensure the listener is active.", new MBeanParameterInfo[0], "void", ACTION),
                        new MBeanOperationInfo("stop", "Ensure the listener is not active.", new MBeanParameterInfo[0], "void", ACTION)
                },
                new MBeanNotificationInfo[0]);

        private final ActiveMQEndpointWorker worker;

        private MdbJmxControl(final ActiveMQEndpointWorker worker) {
            this.worker = worker;
        }

        @Override
        public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
            switch (actionName) {
                case "stop":
                    try {
                        worker.stop();
                    } catch (final InterruptedException e) {
                        Thread.interrupted();
                    }
                    break;
                case "start":
                    try {
                        worker.start();
                    } catch (ResourceException e) {
                        throw new MBeanException(new IllegalStateException(e.getMessage()));
                    }
                    break;
                default:
                    throw new MBeanException(new IllegalStateException("unsupported operation: " + actionName));
            }
            return null;
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            return INFO;
        }

        @Override
        public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
            throw new AttributeNotFoundException();
        }

        @Override
        public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
            throw new AttributeNotFoundException();
        }

        @Override
        public AttributeList getAttributes(final String[] attributes) {
            return ATTRIBUTE_LIST;
        }

        @Override
        public AttributeList setAttributes(final AttributeList attributes) {
            return ATTRIBUTE_LIST;
        }
    }
}
