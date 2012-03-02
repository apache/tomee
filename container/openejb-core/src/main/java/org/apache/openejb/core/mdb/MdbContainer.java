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
import org.apache.openejb.SystemException;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.ContainerType;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.StatsInterceptor;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.monitoring.ManagedMBean;
import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.Options;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.transaction.TransactionPolicy;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleApplicationException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleSystemException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.afterInvoke;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.transaction.xa.XAResource;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.UnavailableException;
import javax.resource.ResourceException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.validation.ConstraintViolation; 
import javax.validation.ConstraintViolationException; 
import javax.validation.Validator; 
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

public class MdbContainer implements RpcContainer {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    private static final Object[] NO_ARGS = new Object[0];

    private final Object containerID;
    private final SecurityService securityService;
    private final ResourceAdapter resourceAdapter;
    private final Class messageListenerInterface;
    private final Class activationSpecClass;
    private final int instanceLimit;

    private final ConcurrentMap<Object, BeanContext> deployments = new ConcurrentHashMap<Object, BeanContext>();
    private final XAResourceWrapper xaResourceWrapper;
    private final InboundRecovery inboundRecovery;

    public MdbContainer(Object containerID, SecurityService securityService, ResourceAdapter resourceAdapter, Class messageListenerInterface, Class activationSpecClass, int instanceLimit) {
        this.containerID = containerID;
        this.securityService = securityService;
        this.resourceAdapter = resourceAdapter;
        this.messageListenerInterface = messageListenerInterface;
        this.activationSpecClass = activationSpecClass;
        this.instanceLimit = instanceLimit;
        xaResourceWrapper = SystemInstance.get().getComponent(XAResourceWrapper.class);
        inboundRecovery = SystemInstance.get().getComponent(InboundRecovery.class);
    }

    public BeanContext[] getBeanContexts() {
        return deployments.values().toArray(new BeanContext[deployments.size()]);
    }

    public BeanContext getBeanContext(Object deploymentID) {
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

    public void deploy(BeanContext beanContext) throws OpenEJBException {
        Object deploymentId = beanContext.getDeploymentID();
        if (!beanContext.getMdbInterface().equals(messageListenerInterface)) {
            throw new OpenEJBException("Deployment '" + deploymentId + "' has message listener interface " +
                    beanContext.getMdbInterface().getName() + " but this MDB container only supports " +
                    messageListenerInterface);
        }

        // create the activation spec
        ActivationSpec activationSpec = createActivationSpec(beanContext);

        if (inboundRecovery != null) {
            inboundRecovery.recover(resourceAdapter, activationSpec, containerID.toString());
        }
        
        Options options = new Options(beanContext.getProperties());
        int instanceLimit = options.get("InstanceLimit", this.instanceLimit);
        // create the message endpoint
        MdbInstanceFactory instanceFactory = new MdbInstanceFactory(beanContext, securityService, instanceLimit);
        EndpointFactory endpointFactory = new EndpointFactory(activationSpec, this, beanContext, instanceFactory, xaResourceWrapper);

        // update the data structures
        // this must be done before activating the endpoint since the ra may immedately begin delivering messages
        beanContext.setContainer(this);
        beanContext.setContainerData(endpointFactory);
        deployments.put(deploymentId, beanContext);

        // Create stats interceptor
        StatsInterceptor stats = new StatsInterceptor(beanContext.getBeanClass());
        beanContext.addSystemInterceptor(stats);

        MBeanServer server = LocalMBeanServer.get();

        ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "openejb");
        jmxName.set("J2EEApplication", null);
        jmxName.set("EJBModule", beanContext.getModuleID());
        jmxName.set("StatelessSessionBean", beanContext.getEjbName());
        jmxName.set("j2eeType", "");
        jmxName.set("name", beanContext.getEjbName());

        // register the invocation stats interceptor
        try {
            ObjectName objectName = jmxName.set("j2eeType", "Invocations").build();
            server.registerMBean(new ManagedMBean(stats), objectName);
            endpointFactory.jmxNames.add(objectName);
        } catch (Exception e) {
            logger.error("Unable to register MBean ", e);
        }

        // activate the endpoint
        try {
            resourceAdapter.endpointActivation(endpointFactory, activationSpec);
        } catch (ResourceException e) {
            // activation failed... clean up
            beanContext.setContainer(null);
            beanContext.setContainerData(null);
            deployments.remove(deploymentId);

            throw new OpenEJBException(e);
        }

        // start the timer service
        EjbTimerService timerService = beanContext.getEjbTimerService();
        if (timerService != null) {
            timerService.start();
        }
    }

    private ActivationSpec createActivationSpec(BeanContext beanContext)throws OpenEJBException {
        try {
            // initialize the object recipe
            ObjectRecipe objectRecipe = new ObjectRecipe(activationSpecClass);
            objectRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
            objectRecipe.disallow(Option.FIELD_INJECTION);

            Map<String, String> activationProperties = beanContext.getActivationProperties();
            for (Map.Entry<String, String> entry : activationProperties.entrySet()) {
                objectRecipe.setMethodProperty(entry.getKey(), entry.getValue());
            }

            // create the activationSpec
            ActivationSpec activationSpec = (ActivationSpec) objectRecipe.create(activationSpecClass.getClassLoader());

            // verify all properties except "destination" and "destinationType" were consumed
            Set<String> unusedProperties = new TreeSet<String>(objectRecipe.getUnsetProperties().keySet());
            unusedProperties.remove("destination");
            unusedProperties.remove("destinationType");
            if (!unusedProperties.isEmpty()) {
                throw new IllegalArgumentException("No setter found for the activation spec properties: " + unusedProperties);
            }


            // validate the activation spec
            try {
                activationSpec.validate();
            } catch (UnsupportedOperationException uoe) {
                logger.info("ActivationSpec does not support validate. Implementation of validate is optional");
            }
            // also try validating using Bean Validation if there is a Validator available in the context.
            try {
                Validator validator = (Validator)beanContext.getJndiContext().lookup("comp/Validator");

                Set generalSet = validator.validate(activationSpec);
                if (!generalSet.isEmpty()) {
                    throw new ConstraintViolationException("Constraint violation for ActivationSpec " + activationSpecClass.getName(), generalSet); 
                }
            } catch (NamingException e) {
                logger.debug("No Validator bound to JNDI context");
            }


            // set the resource adapter into the activation spec
            activationSpec.setResourceAdapter(resourceAdapter);

            return activationSpec;
        } catch (Exception e) {
            throw new OpenEJBException("Unable to create activation spec", e);
        }
    }

    public void start(BeanContext info) throws OpenEJBException {
    }
    
    public void stop(BeanContext info) throws OpenEJBException {
    }
    
    public void undeploy(BeanContext beanContext) throws OpenEJBException {
        if (!(beanContext instanceof BeanContext)) {
            return;
        }

        try {
            EndpointFactory endpointFactory = (EndpointFactory) beanContext.getContainerData();
            if (endpointFactory != null) {
                resourceAdapter.endpointDeactivation(endpointFactory, endpointFactory.getActivationSpec());

                MBeanServer server = LocalMBeanServer.get();
                for (ObjectName objectName : endpointFactory.jmxNames) {
                    try {
                        server.unregisterMBean(objectName);
                    } catch (Exception e) {
                        logger.error("Unable to unregister MBean "+objectName);
                    }
                }
            }
        } finally {
            beanContext.setContainer(null);
            beanContext.setContainerData(null);
            deployments.remove(beanContext.getDeploymentID());
        }
    }

    /**
     * @deprecated use invoke signature without 'securityIdentity' argument.
     */
    public Object invoke(Object deployID, Method callMethod, Object[] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        return invoke(deployID, null, callMethod.getDeclaringClass(), callMethod, args, primKey);
    }

    public Object invoke(Object deployID, Class callInterface, Method callMethod, Object[] args, Object primKey) throws OpenEJBException {
        return invoke(deployID, null, callInterface, callMethod, args, primKey);
    }

    public Object invoke(Object deploymentId, InterfaceType type, Class callInterface, Method method, Object[] args, Object primKey) throws OpenEJBException {
        BeanContext beanContext = getBeanContext(deploymentId);

        EndpointFactory endpointFactory = (EndpointFactory) beanContext.getContainerData();
        MdbInstanceFactory instanceFactory = endpointFactory.getInstanceFactory();
        Instance instance;
        try {
            instance = (Instance) instanceFactory.createInstance(true);
        } catch (UnavailableException e) {
            throw new SystemException("Unable to create instance for invocation", e);
        }

        try {
            beforeDelivery(beanContext, instance, method, null);
            Object value = invoke(instance, method, type, args);
            afterDelivery(instance);
            return value;
        } finally {
            instanceFactory.freeInstance(instance, true);
        }
    }

    public void beforeDelivery(BeanContext deployInfo, Object instance, Method method, XAResource xaResource) throws SystemException {
        // intialize call context
        ThreadContext callContext = new ThreadContext(deployInfo, null);
        ThreadContext oldContext = ThreadContext.enter(callContext);

        // create mdb context
        MdbCallContext mdbCallContext = new MdbCallContext();
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
        } catch (ApplicationException e) {
            ThreadContext.exit(oldContext);
            throw new SystemException("Should never get an Application exception", e);
        } catch (SystemException e) {
            ThreadContext.exit(oldContext);
            throw e;
        } catch (Exception e) {
            ThreadContext.exit(oldContext);
            throw new SystemException("Unable to enlist xa resource in the transaction", e);
        }
    }

    public Object invoke(Object instance, Method method, InterfaceType type, Object... args) throws SystemException, ApplicationException {
        if (args == null) {
            args = NO_ARGS;
        }

        // get the context data
        ThreadContext callContext = ThreadContext.getThreadContext();
        BeanContext deployInfo = callContext.getBeanContext();
        MdbCallContext mdbCallContext = callContext.get(MdbCallContext.class);

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
        Operation oldOperation = callContext.getCurrentOperation();
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
        } catch (ApplicationException e) {
            openEjbException = e;
            throw e;
        } catch (SystemException e) {
            openEjbException = e;
            throw e;
        } finally {
            callContext.setCurrentOperation(oldOperation);
            // Log the invocation results
            if (logger.isDebugEnabled()) {
                if (openEjbException == null) {
                    logger.debug("finished invoking method " + method.getName() + ". Return value:" + returnValue);
                } else {
                    Throwable exception = (openEjbException.getRootCause() != null) ? openEjbException.getRootCause() : openEjbException;
                    logger.debug("finished invoking method " + method.getName() + " with exception " + exception);
                }
            }
        }
    }

    private Object _invoke(Object instance, Method runMethod, Object[] args, BeanContext beanContext, InterfaceType interfaceType, MdbCallContext mdbCallContext) throws SystemException,
            ApplicationException {
        Object returnValue;
        try {
            List<InterceptorData> interceptors = beanContext.getMethodInterceptors(runMethod);
            InterceptorStack interceptorStack = new InterceptorStack(((Instance) instance).bean, runMethod, interfaceType == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS,
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
            ExceptionType type = beanContext.getExceptionType(e);
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

    public void afterDelivery(Object instance) throws SystemException {
        // get the mdb call context
        ThreadContext callContext = ThreadContext.getThreadContext();
        MdbCallContext mdbCallContext = callContext.get(MdbCallContext.class);

        // invoke the tx after method
        try {
            afterInvoke(mdbCallContext.txPolicy, callContext);
        } catch (ApplicationException e) {
            throw new SystemException("Should never get an Application exception", e);
        } finally {
            ThreadContext.exit(mdbCallContext.oldCallContext);
        }
    }

    public void release(BeanContext deployInfo, Object instance) {
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
            MdbCallContext mdbCallContext = callContext.get(MdbCallContext.class);
            if (mdbCallContext != null) {
                try {
                    afterInvoke(mdbCallContext.txPolicy, callContext);
                } catch (Exception e) {
                    logger.error("error while releasing message endpoint", e);
                } finally {
                    EndpointFactory endpointFactory = (EndpointFactory) deployInfo.getContainerData();
                    endpointFactory.getInstanceFactory().freeInstance((Instance) instance, false);
                }
            }
        } finally {
            if (contextExitRequired) {
                ThreadContext.exit(callContext);
            }
        }
    }

    private static class MdbCallContext {
        private Method deliveryMethod;
        private TransactionPolicy txPolicy;
        private ThreadContext oldCallContext;
    }
}
