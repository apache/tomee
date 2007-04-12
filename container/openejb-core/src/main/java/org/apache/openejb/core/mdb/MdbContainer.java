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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.SystemException;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.ContainerType;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.transaction.TransactionContainer;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Logger;

import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.UnavailableException;
import javax.resource.ResourceException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

public class MdbContainer implements RpcContainer, TransactionContainer {
    private static final Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");
    private static final Object[] NO_ARGS = new Object[0];

    private final Object containerID;
    private final TransactionManager transactionManager;
    private final SecurityService securityService;
    private final ResourceAdapter resourceAdapter;
    private final Class messageListenerInterface;
    private final Class activationSpecClass;
    private final int instanceLimit;

    private final ConcurrentMap<Object, CoreDeploymentInfo> deployments = new ConcurrentHashMap<Object, CoreDeploymentInfo>();

    public MdbContainer(Object containerID, TransactionManager transactionManager, SecurityService securityService, ResourceAdapter resourceAdapter, Class messageListenerInterface, Class activationSpecClass, int instanceLimit) {
        this.containerID = containerID;
        this.transactionManager = transactionManager;
        this.securityService = securityService;
        this.resourceAdapter = resourceAdapter;
        this.messageListenerInterface = messageListenerInterface;
        this.activationSpecClass = activationSpecClass;
        this.instanceLimit = instanceLimit;
    }

    public DeploymentInfo [] deployments() {
        return deployments.values().toArray(new DeploymentInfo[deployments.size()]);
    }

    public DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return deployments.get(deploymentID);
    }

    public ContainerType getContainerType() {
        return ContainerType.MESSAGE_DRIVEN;
    }

    public Object getContainerID() {
        return containerID;
    }

    public void deploy(DeploymentInfo info) throws OpenEJBException {
        CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) info;
        Object deploymentId = deploymentInfo.getDeploymentID();
        if (!deploymentInfo.getMdbInterface().equals(messageListenerInterface)) {
            throw new OpenEJBException("Deployment '" + deploymentId + "' has message listener interface " +
                    deploymentInfo.getMdbInterface().getName() + " but this MDB container only supports " +
                    messageListenerInterface);
        }

        // create the activation spec
        ActivationSpec activationSpec = createActivationSpec(deploymentInfo);

        // create the message endpoint
        MdbInstanceFactory instanceFactory = new MdbInstanceFactory(deploymentInfo, transactionManager, securityService, instanceLimit);
        EndpointFactory endpointFactory = new EndpointFactory(activationSpec, this, deploymentInfo, instanceFactory);

        // update the data structures
        // this must be done before activating the endpoint since the ra may immedately begin delivering messages
        deploymentInfo.setContainer(this);
        deploymentInfo.setContainerData(endpointFactory);
        deployments.put(deploymentId, deploymentInfo);

        // activate the endpoint
        try {
            resourceAdapter.endpointActivation(endpointFactory, activationSpec);
        } catch (ResourceException e) {
            // activation failed... clean up
            deploymentInfo.setContainer(null);
            deploymentInfo.setContainerData(null);
            deployments.remove(deploymentId);

            throw new OpenEJBException(e);
        }
    }

    private ActivationSpec createActivationSpec(DeploymentInfo deploymentInfo)throws OpenEJBException {
        try {
            // initialize the object recipe
            ObjectRecipe objectRecipe = new ObjectRecipe(activationSpecClass);
            objectRecipe.disallow(Option.FIELD_INJECTION);

            Map<String, String> activationProperties = deploymentInfo.getActivationProperties();
            for (Map.Entry<String, String> entry : activationProperties.entrySet()) {
                objectRecipe.setMethodProperty(entry.getKey(), entry.getValue());
            }

            // create the activationSpec
            ActivationSpec activationSpec = (ActivationSpec) objectRecipe.create(deploymentInfo.getClassLoader());

            // validate the activation spec
            activationSpec.validate();

            // set the resource adapter into the activation spec
            activationSpec.setResourceAdapter(resourceAdapter);

            return activationSpec;
        } catch (Exception e) {
            throw new OpenEJBException("Unable to create activation spec", e);
        }
    }

    public void undeploy(DeploymentInfo info) throws OpenEJBException {
        if (!(info instanceof CoreDeploymentInfo)) {
            return;
        }

        CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) info;
        try {
            EndpointFactory endpointFactory = (EndpointFactory) deploymentInfo.getContainerData();
            if (endpointFactory != null) {
                resourceAdapter.endpointDeactivation(endpointFactory, endpointFactory.getActivationSpec());
            }
        } finally {
            deploymentInfo.setContainer(null);
            deploymentInfo.setContainerData(null);
            deployments.remove(deploymentInfo.getDeploymentID());
        }
    }

    /**
     * @deprecated use invoke signature without 'securityIdentity' argument.
     */
    public Object invoke(Object deployID, Method callMethod, Object[] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        return invoke(deployID, callMethod, args, primKey);
    }

    public Object invoke(Object deploymentId, Method method, Object[] args, Object primKey) throws OpenEJBException {
        // get the target deployment (MDB)
        CoreDeploymentInfo deployInfo = (CoreDeploymentInfo) getDeploymentInfo(deploymentId);
        if (deployInfo == null) throw new SystemException("Unknown deployment " + deploymentId);

        // create instance
        Object instance = null;
        try {
            EndpointFactory endpointFactory = (EndpointFactory) deployInfo.getContainerData();
            instance = endpointFactory.getInstanceFactory().createInstance(true);
        } catch (UnavailableException e) {
            throw new SystemException("Unable to create new MDB instance: ", e);
        }

        try {
            // before
            beforeDelivery(deployInfo, instance, method, null);

            boolean exceptionThrown = false;
            try {
                // deliver the message
                return invoke(instance, method, args);
            } catch (OpenEJBException e) {
                exceptionThrown = true;
                throw e;
            } finally {
                try {
                    // after
                    afterDelivery(instance);
                } catch (SystemException e) {
                    if (!exceptionThrown) {
                        //noinspection ThrowFromFinallyBlock
                        throw e;
                    }
                    logger.error("Unexpected exception from afterDelivery");
                }
            }
        } finally {
            release(deployInfo, instance);
        }
    }

    public void beforeDelivery(CoreDeploymentInfo deployInfo, Object instance, Method method, XAResource xaResource) throws SystemException {
        // intialize call context
        ThreadContext callContext = new ThreadContext(deployInfo, null);
        ThreadContext oldContext = ThreadContext.enter(callContext);

        // create mdb context
        MdbCallContext mdbCallContext = new MdbCallContext();
        callContext.set(MdbCallContext.class, mdbCallContext);
        mdbCallContext.deliveryMethod = method;
        mdbCallContext.oldCallContext = oldContext;

        // add tx data
        mdbCallContext.txPolicy = deployInfo.getTransactionPolicy(method);
        mdbCallContext.txContext = new TransactionContext(callContext, transactionManager);

        // call the tx before method
        try {
            mdbCallContext.txPolicy.beforeInvoke(instance, mdbCallContext.txContext);

            // enrole the xaresource in the transaction
            if (xaResource != null) {
                Transaction transaction = transactionManager.getTransaction();
                if (transaction != null) {
                    transaction.enlistResource(xaResource);
                }
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

    public Object invoke(Object instance, Method method, Object... args) throws SystemException, ApplicationException {
        if (args == null) {
            args = NO_ARGS;
        }

        // get the context data
        ThreadContext callContext = ThreadContext.getThreadContext();
        CoreDeploymentInfo deployInfo = callContext.getDeploymentInfo();
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
        callContext.setCurrentOperation(Operation.BUSINESS);
        BaseContext.State[] originalStates = callContext.setCurrentAllowedStates(MdbContext.getStates());
        try {
            if (logger.isInfoEnabled()) {
                logger.info("invoking method " + method.getName() + " on " + deployInfo.getDeploymentID());
            }

            // determine the target method on the bean instance class
            Method targetMethod = deployInfo.getMatchingBeanMethod(method);


            callContext.set(Method.class, targetMethod);

            // invoke the target method
            returnValue = _invoke(instance, targetMethod, args, deployInfo, mdbCallContext);
            return returnValue;
        } catch (ApplicationException e) {
            openEjbException = e;
            throw e;
        } catch (SystemException e) {
            openEjbException = e;
            throw e;
        } finally {
            callContext.setCurrentOperation(oldOperation);
            callContext.setCurrentAllowedStates(originalStates);
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

    private Object _invoke(Object instance, Method runMethod, Object [] args, DeploymentInfo deploymentInfo, MdbCallContext mdbCallContext) throws SystemException, ApplicationException {
        Object returnValue = null;
        try {
            List<InterceptorData> interceptors = deploymentInfo.getMethodInterceptors(runMethod);
            InterceptorStack interceptorStack = new InterceptorStack(((Instance)instance).bean, runMethod, Operation.BUSINESS, interceptors, ((Instance)instance).interceptors);
            returnValue = interceptorStack.invoke(args);            
            return returnValue;
        } catch (java.lang.reflect.InvocationTargetException ite) {// handle exceptions thrown by enterprise bean
            if (!isApplicationException(deploymentInfo, ite.getTargetException())) {
                //
                /// System Exception ****************************
                mdbCallContext.txPolicy.handleSystemException(ite.getTargetException(), instance, mdbCallContext.txContext);
            } else {
                //
                // Application Exception ***********************
                mdbCallContext.txPolicy.handleApplicationException(ite.getTargetException(), false, mdbCallContext.txContext);
            }
        } catch (Throwable re) {// handle reflection exception
            //  Any exception thrown by reflection; not by the enterprise bean. Possible
            //  Exceptions are:
            //    IllegalAccessException - if the underlying method is inaccessible.
            //    IllegalArgumentException - if the number of actual and formal parameters differ, or if an unwrapping conversion fails.
            //    NullPointerException - if the specified object is null and the method is an instance method.
            //    ExceptionInInitializerError - if the initialization provoked by this method fails.
            mdbCallContext.txPolicy.handleSystemException(re, instance, mdbCallContext.txContext);
        }
        throw new AssertionError("Should not get here");
    }

    private boolean isApplicationException(DeploymentInfo deploymentInfo, Throwable e) {
        return e instanceof Exception && !(e instanceof RuntimeException);
    }

    public void afterDelivery(Object instance) throws SystemException {
        // get the mdb call context
        ThreadContext callContext = ThreadContext.getThreadContext();
        MdbCallContext mdbCallContext = callContext.get(MdbCallContext.class);

        // invoke the tx after method
        try {
            mdbCallContext.txPolicy.afterInvoke(instance, mdbCallContext.txContext);
        } catch (ApplicationException e) {
            throw new SystemException("Should never get an Application exception", e);
        } finally {
            ThreadContext.exit(mdbCallContext.oldCallContext);
        }
    }

    public void release(CoreDeploymentInfo deployInfo, Object instance) {
        // get the mdb call context
        ThreadContext callContext = ThreadContext.getThreadContext();
        if (callContext == null) {
            callContext = new ThreadContext(deployInfo, null);
            ThreadContext.enter(callContext);

        }

        // if we have an mdb call context we need to invoke the after invoke method
        MdbCallContext mdbCallContext = callContext.get(MdbCallContext.class);
        if (mdbCallContext != null) {
            try {
                mdbCallContext.txPolicy.afterInvoke(instance, mdbCallContext.txContext);
            } catch (Exception e) {
                logger.error("error while releasing message endpoint", e);
            } finally {
                EndpointFactory endpointFactory = (EndpointFactory) deployInfo.getContainerData();
                endpointFactory.getInstanceFactory().freeInstance((Instance)instance, false);
                ThreadContext.exit(mdbCallContext.oldCallContext);
            }
        }

    }

    private static class MdbCallContext {
        private Method deliveryMethod;
        private TransactionPolicy txPolicy;
        private TransactionContext txContext;
        private ThreadContext oldCallContext;
    }

    public void discardInstance(Object instance, ThreadContext context) {
    }
}
