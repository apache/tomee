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
import org.apache.openejb.Container;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.transaction.TransactionContext;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.log4j.Logger;

import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class MdbContainer implements Container {
    private static final Logger logger = Logger.getLogger("OpenEJB");
    private static final Object[] NO_ARGS = new Object[0];

    private final Object containerID;
    private final TransactionManager transactionManager;

    private final Map<Object, DeploymentInfo> deploymentRegistry = new HashMap<Object, DeploymentInfo>();

    public MdbContainer(Object containerID, TransactionManager transactionManager) {
        this.containerID = containerID;
        this.transactionManager = transactionManager;
    }

    public synchronized DeploymentInfo [] deployments() {
        return deploymentRegistry.values().toArray(new DeploymentInfo[deploymentRegistry.size()]);
    }

    public synchronized DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return deploymentRegistry.get(deploymentID);
    }

    public int getContainerType() {
        return Container.MESSAGE_DRIVEN;
    }

    public Object getContainerID() {
        return containerID;
    }

    public synchronized void deploy(Object deploymentID, DeploymentInfo info) throws OpenEJBException {
        deploymentRegistry.put(deploymentID, info);
        CoreDeploymentInfo di = (CoreDeploymentInfo) info;
        di.setContainer(this);
    }

    public synchronized void undeploy(Object deploymentID) throws OpenEJBException {
        CoreDeploymentInfo di = (CoreDeploymentInfo) deploymentRegistry.remove(deploymentID);
        di.setContainer(null);
    }

    public void beforeDelivery(Object deployId, Object instance, Method method) throws Throwable {
        // get the target deployment (MDB)
        CoreDeploymentInfo deployInfo = (CoreDeploymentInfo) getDeploymentInfo(deployId);

        // obtain the context objects
        ThreadContext callContext = ThreadContext.getThreadContext();
        MdbCallContext mdbCallContext = new MdbCallContext();

        // create the tx data
        mdbCallContext.txPolicy = deployInfo.getTransactionPolicy(method);
        mdbCallContext.txContext = new TransactionContext(callContext, transactionManager);

        // call the tx before method
        mdbCallContext.txPolicy.beforeInvoke(instance, mdbCallContext.txContext);

        // save the tx data into the thread context
        callContext.setDeploymentInfo(deployInfo);
        callContext.setUnspecified(mdbCallContext);
    }

    public Object invoke(Object instance, Method method, Object... args) throws Throwable {
        if (args == null) {
            args = NO_ARGS;
        }

        Object returnValue = null;
        Throwable exception = null;
        try {
            // get the context data
            ThreadContext callContext = ThreadContext.getThreadContext();
            CoreDeploymentInfo deployInfo = callContext.getDeploymentInfo();

            if (logger.isInfoEnabled()) {
                logger.info("invoking method " + method.getName() + " on " + deployInfo.getDeploymentID());
            }

            // determine the target method on the bean instance class
            Method targetMethod = deployInfo.getMatchingBeanMethod(method);

            // ivoke the target method
            returnValue = _invoke(instance, targetMethod, args, (MdbCallContext) callContext.getUnspecified());
            return returnValue;
        } catch (org.apache.openejb.ApplicationException ae) {
            // Application exceptions must be reported dirctly to the client. They
            // do not impact the viability of the proxy.
            exception = (ae.getRootCause() != null) ? ae.getRootCause() : ae;
            throw exception;
        } catch (org.apache.openejb.SystemException se) {
            // A system exception would be highly unusual and would indicate a sever
            // problem with the container system.
            exception = (se.getRootCause() != null) ? se.getRootCause() : se;
            logger.error("The container received an unexpected exception: ", exception);
            throw new RemoteException("Container has suffered a SystemException", exception);
        } catch (org.apache.openejb.OpenEJBException oe) {
            // This is a normal container exception thrown while processing the request
            exception = (oe.getRootCause() != null) ? oe.getRootCause() : oe;
            logger.warn("The container received an unexpected exception: ", exception);
            throw new RemoteException("Unknown Container Exception", oe.getRootCause());
        } finally {
            // Log the invocation results
            if (logger.isDebugEnabled()) {
                if (exception == null) {
                    logger.debug("finished invoking method " + method.getName() + ". Return value:" + returnValue);
                } else {
                    logger.debug("finished invoking method " + method.getName() + " with exception " + exception);
                }
            } else if (logger.isInfoEnabled()) {
                if (exception == null) {
                    logger.debug("finished invoking method " + method.getName());
                } else {
                    logger.debug("finished invoking method " + method.getName() + " with exception " + exception);
                }
            }
        }
    }

    private Object _invoke(Object instance, Method runMethod, Object [] args, MdbCallContext mdbCallContext) throws OpenEJBException {
        try {
            Object returnValue = runMethod.invoke(instance, args);
            return returnValue;
        } catch (java.lang.reflect.InvocationTargetException ite) {// handle exceptions thrown by enterprise bean
            if (ite.getTargetException() instanceof RuntimeException) {
                //
                /// System Exception ****************************
                mdbCallContext.txPolicy.handleSystemException(ite.getTargetException(), instance, mdbCallContext.txContext);
            } else {
                //
                // Application Exception ***********************
                mdbCallContext.txPolicy.handleApplicationException(ite.getTargetException(), mdbCallContext.txContext);
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


    public void afterDelivery(Object instance) throws Throwable {
        // get the mdb call context
        ThreadContext callContext = ThreadContext.getThreadContext();
        MdbCallContext mdbCallContext = (MdbCallContext) callContext.getUnspecified();
        ThreadContext.setThreadContext(null);

        // invoke the tx after method
        mdbCallContext.txPolicy.afterInvoke(instance, mdbCallContext.txContext);
    }

    public void release(Object instance) {
        // get the mdb call context
        ThreadContext callContext = ThreadContext.getThreadContext();
        MdbCallContext mdbCallContext = (MdbCallContext) callContext.getUnspecified();
        ThreadContext.setThreadContext(null);

        // if we have an mdb call context we need to invoke the after invoke method
        if (mdbCallContext != null) {
            try {
                mdbCallContext.txPolicy.afterInvoke(instance, mdbCallContext.txContext);
            } catch (Exception e) {
                logger.error("error while releasing message endpoint", e);
            }
        }
    }

    private static class MdbCallContext {
        private TransactionPolicy txPolicy;
        private TransactionContext txContext;
    }
}
