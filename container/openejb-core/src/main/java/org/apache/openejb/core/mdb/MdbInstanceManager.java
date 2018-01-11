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
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.instance.InstanceCreatorRunnable;
import org.apache.openejb.core.instance.InstanceManager;
import org.apache.openejb.core.instance.InstanceManagerData;
import org.apache.openejb.loader.Options;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ManagedMBean;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.monitoring.StatsInterceptor;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.PassthroughFactory;
import org.apache.openejb.util.Pool;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static javax.management.MBeanOperationInfo.ACTION;

public class MdbInstanceManager extends InstanceManager {

    private final Map<BeanContext, MdbPoolContainer.MdbActivationContext> activationContexts = new ConcurrentHashMap<BeanContext, MdbPoolContainer.MdbActivationContext>();
    private final Map<BeanContext, ObjectName> mbeanNames = new ConcurrentHashMap<BeanContext, ObjectName>();
    protected final List<ObjectName> jmxNames = new ArrayList<ObjectName>();
    private final ResourceAdapter resourceAdapter;
    private final InboundRecovery inboundRecovery;
    private final Object containerID;
    private final SecurityService securityService;

    public MdbInstanceManager(final SecurityService securityService,
                              final ResourceAdapter resourceAdapter,
                              final InboundRecovery inboundRecovery,
                              final Object containerID,
                              final Duration accessTimeout, final Duration closeTimeout,
                              final Pool.Builder poolBuilder, final int callbackThreads,
                              final ScheduledExecutorService ses) {
        super(accessTimeout, closeTimeout, poolBuilder, callbackThreads, ses);
        this.securityService = securityService;
        this.resourceAdapter = resourceAdapter;
        this.inboundRecovery = inboundRecovery;
        this.containerID = containerID;
    }


    public void deploy(final BeanContext beanContext, final ActivationSpec activationSpec, final EndpointFactory endpointFactory)
            throws OpenEJBException{
        if (inboundRecovery != null) {
            inboundRecovery.recover(resourceAdapter, activationSpec, containerID.toString());
        }

        final ObjectRecipe recipe = PassthroughFactory.recipe(new Pool.Builder(poolBuilder));
        recipe.allow(Option.CASE_INSENSITIVE_FACTORY);
        recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        recipe.setAllProperties(beanContext.getProperties());

        final Pool.Builder builder = (Pool.Builder) recipe.create();
        setDefault(builder.getMaxAge(), TimeUnit.HOURS);
        setDefault(builder.getIdleTimeout(), TimeUnit.MINUTES);
        setDefault(builder.getInterval(), TimeUnit.MINUTES);

        final InstanceSupplier supplier = new InstanceSupplier(beanContext);
        builder.setSupplier(supplier);
        builder.setExecutor(executor);
        builder.setScheduledExecutor(scheduledExecutor);

        final int min = builder.getMin();
        final long maxAge = builder.getMaxAge().getTime(TimeUnit.MILLISECONDS);
        final double maxAgeOffset = builder.getMaxAgeOffset();

        final InstanceManagerData data = new InstanceManagerData(builder.build(), accessTimeout, closeTimeout);

        final MdbContext mdbContext = new MdbContext(securityService, new Flushable() {
            @Override
            public void flush() throws IOException {
                data.flush();
            }
        });
        data.setBaseContext(mdbContext);
        beanContext.setContainerData(data);

        // Create stats interceptor
        if (StatsInterceptor.isStatsActivated()) {
            final StatsInterceptor stats = new StatsInterceptor(beanContext.getBeanClass());
            beanContext.addFirstSystemInterceptor(stats);

            final MBeanServer server = LocalMBeanServer.get();

            final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
            jmxName.set("J2EEServer", "openejb");
            jmxName.set("J2EEApplication", null);
            jmxName.set("EJBModule", beanContext.getModuleID());
            jmxName.set("MessageDrivenBean", beanContext.getEjbName());
            jmxName.set("j2eeType", "");
            jmxName.set("name", beanContext.getEjbName());

            // register the invocation stats interceptor
            try {
                final ObjectName objectName = jmxName.set("j2eeType", "Invocations").build();
                if (server.isRegistered(objectName)) {
                    server.unregisterMBean(objectName);
                }
                server.registerMBean(new ManagedMBean(stats), objectName);
                jmxNames.add(objectName);
            } catch (final Exception e) {
                logger.error("Unable to register MBean ", e);
            }
        }

        // activate the endpoint
        try {

            final MdbPoolContainer.MdbActivationContext activationContext = new MdbPoolContainer.MdbActivationContext(Thread.currentThread().getContextClassLoader(), beanContext, resourceAdapter, endpointFactory, activationSpec);
            activationContexts.put(beanContext, activationContext);

            boolean activeOnStartup = true;
            String activeOnStartupSetting = beanContext.getActivationProperties().get("MdbActiveOnStartup");

            if (activeOnStartupSetting == null) {
                activeOnStartupSetting = beanContext.getActivationProperties().get("DeliveryActive");
            }

            if (activeOnStartupSetting != null) {
                activeOnStartup = Boolean.parseBoolean(activeOnStartupSetting);
            }

            if (activeOnStartup) {
                activationContext.start();
            } else {
                logger.info("Not auto-activating endpoint for " + beanContext.getDeploymentID());
            }

            String jmxName = beanContext.getActivationProperties().get("MdbJMXControl");
            if (jmxName == null) {
                jmxName  = "true";
            }

            addJMxControl(beanContext, jmxName, activationContext);

        } catch (final ResourceException e) {
            throw new OpenEJBException(e);
        }

        final Options options = new Options(beanContext.getProperties());
        // Finally, fill the pool and start it
        if (!options.get("BackgroundStartup", false) && min > 0) {
            final ExecutorService es = Executors.newFixedThreadPool(min);
            for (int i = 0; i < min; i++) {
                es.submit(new InstanceCreatorRunnable(maxAge, i, min, maxAgeOffset, data, supplier));
            }
            es.shutdown();
            try {
                es.awaitTermination(5, TimeUnit.MINUTES);
            } catch (final InterruptedException e) {
                logger.error("can't fill the stateless pool", e);
            }
        }

        data.getPool().start();
    }

    public void undeploy(final BeanContext beanContext){
        final MdbPoolContainer.MdbActivationContext actContext = activationContexts.get(beanContext);
        if (actContext == null) {
            return;
        }
        final EndpointFactory endpointFactory = actContext.getEndpointFactory();
        if (endpointFactory != null) {

            final ObjectName jmxBeanToRemove = mbeanNames.remove(beanContext);
            if (jmxBeanToRemove != null) {
                LocalMBeanServer.unregisterSilently(jmxBeanToRemove);
                logger.info("Undeployed MDB control for " + beanContext.getDeploymentID());
            }

            final MdbPoolContainer.MdbActivationContext activationContext = activationContexts.remove(beanContext);
            if (activationContext != null && activationContext.isStarted()) {
                resourceAdapter.endpointDeactivation(endpointFactory, endpointFactory.getActivationSpec());
            }

            final MBeanServer server = LocalMBeanServer.get();
            for (final ObjectName objectName : jmxNames) {
                try {
                    server.unregisterMBean(objectName);
                } catch (final InstanceNotFoundException e) {
                    // ignore it as the object name is gone already
                } catch (final Exception e) {
                    logger.error("Unable to unregister MBean " + objectName, e);
                }
            }
        }
    }

    private void addJMxControl(final BeanContext current, final String name, final MdbPoolContainer.MdbActivationContext activationContext) throws ResourceException {
        if (name == null || "false".equalsIgnoreCase(name)) {
            logger.debug("Not adding JMX control for " + current.getDeploymentID());
            return;
        }

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

        LocalMBeanServer.registerSilently(new MdbJmxControl(activationContext), jmxName);
        logger.info("Deployed MDB control for " + current.getDeploymentID() + " on " + jmxName);
    }

    public static final class MdbJmxControl implements DynamicMBean {
        private static final AttributeList ATTRIBUTE_LIST = new AttributeList();
        private static final MBeanInfo INFO = new MBeanInfo(
                "org.apache.openejb.resource.activemq.ActiveMQResourceAdapter.MdbJmxControl",
                "Allows to control a MDB (start/stop)",
                new MBeanAttributeInfo[]{
                        new MBeanAttributeInfo("started", "boolean", "started: boolean indicating whether this MDB endpoint has been activated.", true, false, true)
                },
                new MBeanConstructorInfo[0],
                new MBeanOperationInfo[]{
                        new MBeanOperationInfo("start", "Ensure the listener is active.", new MBeanParameterInfo[0], "void", ACTION),
                        new MBeanOperationInfo("stop", "Ensure the listener is not active.", new MBeanParameterInfo[0], "void", ACTION)
                },
                new MBeanNotificationInfo[0]);

        private final MdbPoolContainer.MdbActivationContext activationContext;

        private MdbJmxControl(final MdbPoolContainer.MdbActivationContext activationContext) {
            this.activationContext = activationContext;
        }

        @Override
        public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
            if (actionName.equals("stop")) {
                activationContext.stop();

            } else if (actionName.equals("start")) {
                try {
                    activationContext.start();
                } catch (ResourceException e) {
                    logger.error("Error invoking " + actionName + ": " + e.getMessage());
                    throw new MBeanException(new IllegalStateException(e.getMessage(), e));
                }

            } else {
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
            if ("started".equals(attribute)) {
                return activationContext.isStarted();
            }

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