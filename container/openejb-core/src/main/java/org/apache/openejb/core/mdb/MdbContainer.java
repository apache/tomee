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

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ContainerType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.*;
import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

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
import javax.naming.NamingException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.UnavailableException;
import javax.transaction.xa.XAResource;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.management.MBeanOperationInfo.ACTION;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.afterInvoke;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleApplicationException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleSystemException;

public class MdbContainer implements RpcContainer, BaseMdbContainer {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    private static final ThreadLocal<BeanContext> CURRENT = new ThreadLocal<>();
    private static final Object[] NO_ARGS = new Object[0];

    private final Map<BeanContext, ObjectName> mbeanNames = new ConcurrentHashMap<>();
    private final Map<BeanContext, MdbActivationContext> activationContexts = new ConcurrentHashMap<>();

    private final Object containerID;
    private final SecurityService securityService;
    private final ResourceAdapter resourceAdapter;
    private final Class messageListenerInterface;
    private final Class activationSpecClass;
    private final int instanceLimit;
    private final boolean failOnUnknownActivationSpec;

    private final ConcurrentMap<Object, BeanContext> deployments = new ConcurrentHashMap<>();
    private final XAResourceWrapper xaResourceWrapper;
    private final InboundRecovery inboundRecovery;

    private final Properties properties = new Properties();

    public MdbContainer(final Object containerID, final SecurityService securityService, final ResourceAdapter resourceAdapter,
                        final Class messageListenerInterface, final Class activationSpecClass, final int instanceLimit,
                        final boolean failOnUnknownActivationSpec) {
        this.containerID = containerID;
        this.securityService = securityService;
        this.resourceAdapter = resourceAdapter;
        this.messageListenerInterface = messageListenerInterface;
        this.activationSpecClass = activationSpecClass;
        this.instanceLimit = instanceLimit;
        this.failOnUnknownActivationSpec = failOnUnknownActivationSpec;
        xaResourceWrapper = SystemInstance.get().getComponent(XAResourceWrapper.class);
        inboundRecovery = SystemInstance.get().getComponent(InboundRecovery.class);
    }

    public BeanContext[] getBeanContexts() {
        return deployments.values().toArray(new BeanContext[deployments.size()]);
    }

    public BeanContext getBeanContext(final Object deploymentID) {
        return deployments.get(deploymentID);
    }

    public ContainerType getContainerType() {
        return ContainerType.MESSAGE_DRIVEN;
    }

    public Object getContainerID() {
        return containerID;
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public Class getMessageListenerInterface() {
        return messageListenerInterface;
    }

    public Class getActivationSpecClass() {
        return activationSpecClass;
    }

    public Properties getProperties() {
        return properties;
    }

    public void deploy(final BeanContext beanContext) throws OpenEJBException {
        final Object deploymentId = beanContext.getDeploymentID();
        if (!beanContext.getMdbInterface().equals(messageListenerInterface)) {
            throw new OpenEJBException("Deployment '" + deploymentId + "' has message listener interface " +
                    beanContext.getMdbInterface().getName() + " but this MDB container only supports " +
                    messageListenerInterface);
        }

        // create the activation spec
        final ActivationSpec activationSpec = createActivationSpec(beanContext);

        if (inboundRecovery != null) {
            inboundRecovery.recover(resourceAdapter, activationSpec, containerID.toString());
        }

        final Options options = new Options(beanContext.getProperties());
        final int instanceLimit = options.get("InstanceLimit", this.instanceLimit);
        // create the message endpoint
        final MdbInstanceFactory instanceFactory = new MdbInstanceFactory(beanContext, securityService, instanceLimit);
        final EndpointFactory endpointFactory = new EndpointFactory(activationSpec, this, beanContext, instanceFactory, null, xaResourceWrapper, false);

        // update the data structures
        // this must be done before activating the endpoint since the ra may immedately begin delivering messages
        beanContext.setContainer(this);
        beanContext.setContainerData(endpointFactory);
        deployments.put(deploymentId, beanContext);

        final MBeanServer server = LocalMBeanServer.get();

        // Create stats interceptor
        if (StatsInterceptor.isStatsActivated()) {
            final StatsInterceptor stats = new StatsInterceptor(beanContext.getBeanClass());
            beanContext.addFirstSystemInterceptor(stats);


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
                endpointFactory.jmxNames.add(objectName);
            } catch (final Exception e) {
                logger.error("Unable to register MBean ", e);
            }
        }

        // Expose InstanceLimit/InstanceCount stats through JMX
        {
            final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
            jmxName.set("J2EEServer", "openejb");
            jmxName.set("J2EEApplication", null);
            jmxName.set("EJBModule", beanContext.getModuleID());
            jmxName.set("MessageDrivenBean", beanContext.getEjbName());
            jmxName.set("j2eeType", "");
            jmxName.set("name", beanContext.getEjbName());

            try {
                final ObjectName objectName = jmxName.set("j2eeType", "Instances").build();
                if (server.isRegistered(objectName)) {
                    server.unregisterMBean(objectName);
                }
                server.registerMBean(new ManagedMBean(new InstanceMonitor(instanceFactory)), objectName);
                endpointFactory.jmxNames.add(objectName);
            } catch (final Exception e) {
                logger.error("Unable to register MBean ", e);
            }
        }


        // activate the endpoint
        CURRENT.set(beanContext);
        try {

            final MdbActivationContext activationContext = new MdbActivationContext(Thread.currentThread().getContextClassLoader(), beanContext, resourceAdapter, endpointFactory, activationSpec);
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
            // activation failed... clean up
            beanContext.setContainer(null);
            beanContext.setContainerData(null);
            deployments.remove(deploymentId);

            throw new OpenEJBException(e);
        } finally {
            CURRENT.remove();
        }
    }

    private ActivationSpec createActivationSpec(final BeanContext beanContext) throws OpenEJBException {
        try {
            // initialize the object recipe
            final ObjectRecipe objectRecipe = new ObjectRecipe(activationSpecClass);
            objectRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
            objectRecipe.disallow(Option.FIELD_INJECTION);


            final Map<String, String> activationProperties = beanContext.getActivationProperties();
            for (final Map.Entry<String, String> entry : activationProperties.entrySet()) {
                objectRecipe.setMethodProperty(entry.getKey(), entry.getValue());
            }
            objectRecipe.setMethodProperty("beanClass", beanContext.getBeanClass());


            // create the activationSpec
            final ActivationSpec activationSpec = (ActivationSpec) objectRecipe.create(activationSpecClass.getClassLoader());

            // verify all properties except "destination" and "destinationType" were consumed
            final Set<String> unusedProperties = new TreeSet<>(objectRecipe.getUnsetProperties().keySet());
            unusedProperties.remove("destination");
            unusedProperties.remove("destinationType");
            unusedProperties.remove("destinationLookup");
            unusedProperties.remove("connectionFactoryLookup");
            unusedProperties.remove("beanClass");
            unusedProperties.remove("MdbActiveOnStartup");
            unusedProperties.remove("MdbJMXControl");
            unusedProperties.remove("DeliveryActive");

            if (!unusedProperties.isEmpty()) {
                final String text = "No setter found for the activation spec properties: " + unusedProperties;
                if (failOnUnknownActivationSpec) {
                    throw new IllegalArgumentException(text);
                } else {
                    logger.warning(text);
                }
            }


            // validate the activation spec
            try {
                activationSpec.validate();
            } catch (final UnsupportedOperationException uoe) {
                logger.info("ActivationSpec does not support validate. Implementation of validate is optional");
            }
            // also try validating using Bean Validation if there is a Validator available in the context.
            try {
                final Validator validator = (Validator) beanContext.getJndiContext().lookup("comp/Validator");

                final Set generalSet = validator.validate(activationSpec);
                if (!generalSet.isEmpty()) {
                    throw new ConstraintViolationException("Constraint violation for ActivationSpec " + activationSpecClass.getName(), generalSet);
                }
            } catch (final NamingException e) {
                logger.debug("No Validator bound to JNDI context");
            }

            // set the resource adapter into the activation spec
            activationSpec.setResourceAdapter(resourceAdapter);

            return activationSpec;
        } catch (final Exception e) {
            throw new OpenEJBException("Unable to create activation spec", e);
        }
    }

    public void start(final BeanContext info) throws OpenEJBException {
        final EjbTimerService timerService = info.getEjbTimerService();
        if (timerService != null) {
            timerService.start();
        }
    }

    public void stop(final BeanContext info) throws OpenEJBException {
        info.stop();
    }

    public void undeploy(final BeanContext beanContext) throws OpenEJBException {
        if (!(beanContext instanceof BeanContext)) {
            return;
        }

        try {
            final EndpointFactory endpointFactory = (EndpointFactory) beanContext.getContainerData();
            if (endpointFactory != null) {
                CURRENT.set(beanContext);
                try {

                    final ObjectName jmxBeanToRemove = mbeanNames.remove(beanContext);
                    if (jmxBeanToRemove != null) {
                        LocalMBeanServer.unregisterSilently(jmxBeanToRemove);
                        logger.info("Undeployed MDB control for " + beanContext.getDeploymentID());
                    }

                    final MdbActivationContext activationContext = activationContexts.remove(beanContext);
                    if (activationContext != null && activationContext.isStarted()) {
                        resourceAdapter.endpointDeactivation(endpointFactory, endpointFactory.getActivationSpec());
                    }
                } finally {
                    CURRENT.remove();
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
        } finally {
            beanContext.setContainer(null);
            beanContext.setContainerData(null);
            deployments.remove(beanContext.getDeploymentID());
        }
    }

    public Object invoke(final Object deploymentId, final InterfaceType type, final Class callInterface, final Method method, final Object[] args, final Object primKey) throws OpenEJBException {
        final BeanContext beanContext = getBeanContext(deploymentId);

        final EndpointFactory endpointFactory = (EndpointFactory) beanContext.getContainerData();
        final MdbInstanceFactory instanceFactory = endpointFactory.getInstanceFactory();
        final Instance instance;
        try {
            instance = (Instance) instanceFactory.createInstance(true);
        } catch (final UnavailableException e) {
            throw new SystemException("Unable to create instance for invocation", e);
        }

        try {
            beforeDelivery(beanContext, instance, method, null);
            final Object value = invoke(instance, method, type, args);
            afterDelivery(instance);
            return value;
        } finally {
            instanceFactory.freeInstance(instance, true);
        }
    }

    public void beforeDelivery(final BeanContext deployInfo, final Object instance, final Method method, final XAResource xaResource) throws SystemException {
        // intialize call context
        final ThreadContext callContext = new ThreadContext(deployInfo, null);
        final ThreadContext oldContext = ThreadContext.enter(callContext);

        // create mdb context
        final MdbCallContext mdbCallContext = new MdbCallContext();
        callContext.set(MdbCallContext.class, mdbCallContext);
        mdbCallContext.deliveryMethod = method;
        mdbCallContext.oldCallContext = oldContext;

        // call the tx before method
        try {
            mdbCallContext.txPolicy = createTransactionPolicy(deployInfo.getTransactionType(method), callContext);

            // if we have an xaResource and a transaction was not imported from the adapter, enlist the xaResource
            if (xaResource != null && mdbCallContext.txPolicy.isNewTransaction()) {
                mdbCallContext.txPolicy.enlistResource(xaResource);
            }
        } catch (final ApplicationException e) {
            ThreadContext.exit(oldContext);
            throw new SystemException("Should never get an Application exception", e);
        } catch (final SystemException e) {
            ThreadContext.exit(oldContext);
            throw e;
        } catch (final Exception e) {
            ThreadContext.exit(oldContext);
            throw new SystemException("Unable to enlist xa resource in the transaction", e);
        }
    }

    public Object invoke(final Object instance, final Method method, final InterfaceType type, Object... args) throws SystemException, ApplicationException {
        if (args == null) {
            args = NO_ARGS;
        }

        // get the context data
        final ThreadContext callContext = ThreadContext.getThreadContext();
        final BeanContext deployInfo = callContext.getBeanContext();
        final MdbCallContext mdbCallContext = callContext.get(MdbCallContext.class);

        if (mdbCallContext == null) {
            throw new IllegalStateException("beforeDelivery was not called");
        }

        // verify the delivery method passed to beforeDeliver is the same method that was invoked
        if (!mdbCallContext.deliveryMethod.getName().equals(method.getName()) ||
                !Arrays.deepEquals(mdbCallContext.deliveryMethod.getParameterTypes(), method.getParameterTypes())) {
            throw new IllegalStateException("Delivery method specified in beforeDelivery is not the delivery method called");
        }

        // remember the return value or exception so it can be logged
        Object returnValue = null;
        OpenEJBException openEjbException = null;
        final Operation oldOperation = callContext.getCurrentOperation();
        callContext.setCurrentOperation(type == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS);
        try {
            if (logger.isDebugEnabled()) {
                logger.info("invoking method " + method.getName() + " on " + deployInfo.getDeploymentID());
            }

            // determine the target method on the bean instance class
            final Method targetMethod = deployInfo.getMatchingBeanMethod(method);
            callContext.set(Method.class, targetMethod);

            // invoke the target method
            returnValue = _invoke(instance, targetMethod, args, deployInfo, type, mdbCallContext);
            return returnValue;
        } catch (final ApplicationException | SystemException e) {
            openEjbException = e;
            throw e;
        } finally {
            callContext.setCurrentOperation(oldOperation);
            // Log the invocation results
            if (logger.isDebugEnabled()) {
                if (openEjbException == null) {
                    logger.debug("finished invoking method " + method.getName() + ". Return value:" + returnValue);
                } else {
                    final Throwable exception = openEjbException.getRootCause() != null ? openEjbException.getRootCause() : openEjbException;
                    logger.debug("finished invoking method " + method.getName() + " with exception " + exception);
                }
            }
        }
    }

    private Object _invoke(final Object instance, final Method runMethod, final Object[] args, final BeanContext beanContext, final InterfaceType interfaceType, final MdbCallContext mdbCallContext) throws SystemException,
            ApplicationException {
        final Object returnValue;
        try {
            final List<InterceptorData> interceptors = beanContext.getMethodInterceptors(runMethod);
            final InterceptorStack interceptorStack = new InterceptorStack(((Instance) instance).bean, runMethod, interfaceType == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS,
                    interceptors, ((Instance) instance).interceptors);
            returnValue = interceptorStack.invoke(args);
            return returnValue;
        } catch (Throwable e) {
            // unwrap invocation target exception
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }

            //  Any exception thrown by reflection; not by the enterprise bean. Possible
            //  Exceptions are:
            //    IllegalAccessException - if the underlying method is inaccessible.
            //    IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
            //    NullPointerException - if the specified object is null and the method is an instance method.
            //    ExceptionInInitializerError - if the initialization provoked by this method fails.
            final ExceptionType type = beanContext.getExceptionType(e);
            if (type == ExceptionType.SYSTEM) {
                //
                /// System Exception ****************************
                handleSystemException(mdbCallContext.txPolicy, e, ThreadContext.getThreadContext());
            } else {
                //
                // Application Exception ***********************
                handleApplicationException(mdbCallContext.txPolicy, e, false);
            }
        }
        throw new AssertionError("Should not get here");
    }

    public void afterDelivery(final Object instance) throws SystemException {
        // get the mdb call context
        final ThreadContext callContext = ThreadContext.getThreadContext();
        final MdbCallContext mdbCallContext = callContext.get(MdbCallContext.class);

        // invoke the tx after method
        try {
            afterInvoke(mdbCallContext.txPolicy, callContext);
        } catch (final ApplicationException e) {
            throw new SystemException("Should never get an Application exception", e);
        } finally {
            ThreadContext.exit(mdbCallContext.oldCallContext);
        }
    }

    public void release(final BeanContext deployInfo, final Object instance) {
        // get the mdb call context
        ThreadContext callContext = ThreadContext.getThreadContext();
        boolean contextExitRequired = false;
        if (callContext == null) {
            callContext = new ThreadContext(deployInfo, null);
            ThreadContext.enter(callContext);
            contextExitRequired = true;

        }
        try {
            // if we have an mdb call context we need to invoke the after invoke method
            final MdbCallContext mdbCallContext = callContext.get(MdbCallContext.class);
            if (mdbCallContext != null) {
                try {
                    afterInvoke(mdbCallContext.txPolicy, callContext);
                } catch (final Exception e) {
                    logger.error("error while releasing message endpoint", e);
                } finally {
                    final EndpointFactory endpointFactory = (EndpointFactory) deployInfo.getContainerData();
                    endpointFactory.getInstanceFactory().freeInstance((Instance) instance, false);
                }
            }
        } finally {
            if (contextExitRequired) {
                ThreadContext.exit(callContext);
            }
        }
    }

    private void addJMxControl(final BeanContext current, final String name, final MdbActivationContext activationContext) throws ResourceException {
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

    public static BeanContext current() {
        final BeanContext beanContext = CURRENT.get();
        if (beanContext == null) {
            CURRENT.remove();
        }
        return beanContext;
    }

    private static class MdbCallContext {
        private Method deliveryMethod;
        private TransactionPolicy txPolicy;
        private ThreadContext oldCallContext;
    }

    private static class MdbActivationContext {
        private final ClassLoader classLoader;
        private final BeanContext beanContext;
        private final ResourceAdapter resourceAdapter;
        private final EndpointFactory endpointFactory;
        private final ActivationSpec activationSpec;

        private AtomicBoolean started = new AtomicBoolean(false);

        public MdbActivationContext(final ClassLoader classLoader, final BeanContext beanContext, final ResourceAdapter resourceAdapter, final EndpointFactory endpointFactory, final ActivationSpec activationSpec) {
            this.classLoader = classLoader;
            this.beanContext = beanContext;
            this.resourceAdapter = resourceAdapter;
            this.endpointFactory = endpointFactory;
            this.activationSpec = activationSpec;
        }

        public ResourceAdapter getResourceAdapter() {
            return resourceAdapter;
        }

        public EndpointFactory getEndpointFactory() {
            return endpointFactory;
        }

        public ActivationSpec getActivationSpec() {
            return activationSpec;
        }

        public boolean isStarted() {
            return started.get();
        }

        public void start() throws ResourceException {
            if (!started.compareAndSet(false, true)) {
                return;
            }

            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                resourceAdapter.endpointActivation(endpointFactory, activationSpec);
                logger.info("Activated endpoint for " + beanContext.getDeploymentID());
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }

        }

        public void stop() {
            if (!started.compareAndSet(true, false)) {
                return;
            }

            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                resourceAdapter.endpointDeactivation(endpointFactory, activationSpec);
                logger.info("Deactivated endpoint for " + beanContext.getDeploymentID());
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
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

        private final MdbActivationContext activationContext;

        private MdbJmxControl(final MdbActivationContext activationContext) {
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

    public static class InstanceMonitor {
        private final MdbInstanceFactory instanceFactory;

        public InstanceMonitor(MdbInstanceFactory instanceFactory) {
            this.instanceFactory = instanceFactory;
        }

        @Managed
        public int getInstanceLimit() {
            return instanceFactory.getInstanceLimit();
        }

        @Managed
        public int getInstanceCount() {
            return instanceFactory.getInstanceCount();
        }
    }
}
