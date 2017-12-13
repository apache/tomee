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
import org.apache.openejb.core.instance.InstanceManager;
import org.apache.openejb.loader.Options;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ManagedMBean;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.monitoring.StatsInterceptor;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Pool;

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
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import static javax.management.MBeanOperationInfo.ACTION;

public class MdbInstanceManager extends InstanceManager {

    private  static final ThreadLocal<BeanContext> CURRENT = new ThreadLocal<>();
    private final Map<BeanContext, PoolMdbContainer.MdbActivationContext> activationContexts = new ConcurrentHashMap<>();
    private final Map<BeanContext, ObjectName> mbeanNames = new ConcurrentHashMap<>();
    private final ResourceAdapter resourceAdapter;
    private final InboundRecovery inboundRecovery;
    private final Object containerID;
    private int instanceLimit;

    public MdbInstanceManager(final ResourceAdapter resourceAdapter,
                              final InboundRecovery inboundRecovery,
                              final Object containerID,
                              final int instanceLimit,
                              final SecurityService securityService,
                                    final Duration accessTimeout, final Duration closeTimeout,
                                    final Pool.Builder poolBuilder, final int callbackThreads,
                                    final ScheduledExecutorService ses) {
        super(securityService, accessTimeout, closeTimeout, poolBuilder, callbackThreads, ses);
        this.resourceAdapter = resourceAdapter;
        this.inboundRecovery = inboundRecovery;
        this.containerID = containerID;
        this.instanceLimit = instanceLimit;
    }


    public void deploy(final BeanContext beanContext, final ActivationSpec activationSpec, final EndpointFactory endpointFactory)
            throws OpenEJBException{
        if (inboundRecovery != null) {
            inboundRecovery.recover(resourceAdapter, activationSpec, containerID.toString());
        }

        final Options options = new Options(beanContext.getProperties());
        final int instanceLimit = options.get("InstanceLimit", this.instanceLimit);

        // Create stats interceptor
        if (StatsInterceptor.isStatsActivated()) {
            final StatsInterceptor stats = new StatsInterceptor(beanContext.getBeanClass());
            beanContext.addFirstSystemInterceptor(stats);

            final MBeanServer server = LocalMBeanServer.get();

            final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
            jmxName.set("J2EEServer", "openejb");
            jmxName.set("J2EEApplication", null);
            jmxName.set("EJBModule", beanContext.getModuleID());
            jmxName.set("StatelessSessionBean", beanContext.getEjbName());
            jmxName.set("j2eeType", "");
            jmxName.set("name", beanContext.getEjbName());

            // register the invocation stats interceptor
            try {
                final ObjectName objectName = jmxName.set("j2eeType", "Invocations").build();
                if (server.isRegistered(objectName)) {
                    server.unregisterMBean(objectName);
                }
                server.registerMBean(new ManagedMBean(stats), objectName);
                endpointFactory.jmxNames.add(objectName);
            } catch (final Exception e) {
                logger.error("Unable to register MBean ", e);
            }
        }

        // activate the endpoint
        try {

            final PoolMdbContainer.MdbActivationContext activationContext = new PoolMdbContainer.MdbActivationContext(Thread.currentThread().getContextClassLoader(), beanContext, resourceAdapter, endpointFactory, activationSpec);
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
    }

    public void undeploy(final BeanContext beanContext){
        final EndpointFactory endpointFactory = (EndpointFactory) beanContext.getContainerData();
        if (endpointFactory != null) {

            final ObjectName jmxBeanToRemove = mbeanNames.remove(beanContext);
            if (jmxBeanToRemove != null) {
                LocalMBeanServer.unregisterSilently(jmxBeanToRemove);
                logger.info("Undeployed MDB control for " + beanContext.getDeploymentID());
            }

            final PoolMdbContainer.MdbActivationContext activationContext = activationContexts.remove(beanContext);
            if (activationContext != null && activationContext.isStarted()) {
                resourceAdapter.endpointDeactivation(endpointFactory, endpointFactory.getActivationSpec());
            }

            final MBeanServer server = LocalMBeanServer.get();
            for (final ObjectName objectName : endpointFactory.jmxNames) {
                try {
                    server.unregisterMBean(objectName);
                } catch (final Exception e) {
                    logger.error("Unable to unregister MBean " + objectName);
                }
            }
        }
    }

    private void addJMxControl(final BeanContext current, final String name, final PoolMdbContainer.MdbActivationContext activationContext) throws ResourceException {
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

        private final PoolMdbContainer.MdbActivationContext activationContext;

        private MdbJmxControl(final PoolMdbContainer.MdbActivationContext activationContext) {
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
