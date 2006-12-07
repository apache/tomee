/**
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
package org.apache.openejb.core.stateless;

import org.apache.log4j.Category;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.StaticRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.Injection;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.Operations;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LinkedListStack;
import org.apache.openejb.util.SafeToolkit;
import org.apache.openejb.util.Stack;

import javax.ejb.SessionContext;
import javax.transaction.TransactionManager;
import javax.naming.Context;
import javax.naming.NamingException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;

public class StatelessInstanceManager {

    protected java.util.HashMap poolMap = new HashMap();
    protected int poolLimit = 0;
    protected int beanCount = 0;
    protected boolean strictPooling = false;

    protected PoolQueue poolQueue = null;

    protected final SafeToolkit toolkit = SafeToolkit.getToolkit("StatefulInstanceManager");
    protected final static Category logger = Category.getInstance("OpenEJB");
    private TransactionManager transactionManager;
    private SecurityService securityService;

    public StatelessInstanceManager(TransactionManager transactionManager, SecurityService securityService, int timeout, int poolSize, boolean strictPooling) {
        this.transactionManager = transactionManager;
        this.securityService = securityService;
        this.poolLimit = poolSize;
        this.strictPooling = strictPooling;

        if (strictPooling && poolSize < 1){
            throw new IllegalArgumentException("Cannot use strict pooling with a pool size less than one.  Strict pooling blocks threads till an instance in the pool is available.  Please increase the pool size or set strict pooling to false");
        }

        if (this.strictPooling) {
            poolQueue = new PoolQueue(timeout);
        }
    }

    public Object getInstance(ThreadContext callContext)
            throws OpenEJBException {
        Object bean = null;
        Object deploymentId = callContext.getDeploymentInfo().getDeploymentID();
        Stack pool = (Stack) poolMap.get(deploymentId);
        if (pool == null) {
            pool = new LinkedListStack(poolLimit);
            poolMap.put(deploymentId, pool);
        } else {
            bean = pool.pop();
        }

        while (strictPooling && bean == null && pool.size() >= poolLimit) {
            poolQueue.waitForAvailableInstance();
            bean = pool.pop();
        }

        if (bean == null) {

            CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
            Class beanClass = deploymentInfo.getBeanClass();
            ObjectRecipe objectRecipe = new ObjectRecipe(beanClass);
            objectRecipe.allow(Option.FIELD_INJECTION);
            objectRecipe.allow(Option.PRIVATE_PROPERTIES);
            objectRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);

            byte originalOperation = callContext.getCurrentOperation();

            try {
                Context ctx = deploymentInfo.getJndiEnc();
                for (Injection injection : deploymentInfo.getInjections()) {
                    try {
                        String jndiName = injection.getJndiName();
                        Object object = ctx.lookup("java:comp/env/" + jndiName);
                        objectRecipe.setProperty(injection.getName(), new StaticRecipe(object));
                    } catch (NamingException e) {
                        logger.warn("Injection data not found in enc: jndiName='"+injection.getJndiName()+"', target="+injection.getTarget()+"/"+injection.getName());
                    }
                }

                callContext.setCurrentOperation(Operations.OP_SET_CONTEXT);
                objectRecipe.setProperty("sessionContext", new StaticRecipe(createSessionContext()));
                bean = objectRecipe.create(beanClass.getClassLoader());
                callContext.setCurrentOperation(Operations.OP_CREATE);

                Method postConstruct = deploymentInfo.getPostConstruct();
                if (postConstruct != null){
                    postConstruct.invoke(bean);
                }
            } catch (Throwable e) {
                if (e instanceof java.lang.reflect.InvocationTargetException) {
                    e = ((java.lang.reflect.InvocationTargetException) e).getTargetException();
                }
                String t = "The bean instance " + bean + " threw a system exception:" + e;
                logger.error(t, e);
                throw new org.apache.openejb.ApplicationException(new RemoteException("Can not obtain a free instance.",e));
            } finally {
                callContext.setCurrentOperation(originalOperation);
            }
        }
        return bean;
    }

    private SessionContext createSessionContext() {
        return (SessionContext) new StatelessContext(transactionManager, securityService);
    }

    public void poolInstance(ThreadContext callContext, Object bean) throws OpenEJBException {
        if (bean == null) {
            throw new SystemException("Invalid arguments");
        }

        Object deploymentId = callContext.getDeploymentInfo().getDeploymentID();

        Stack pool = (Stack) poolMap.get(deploymentId);

        if (strictPooling) {
            pool.push(bean);
            poolQueue.notifyWaitingThreads();
        } else {
            if (pool.size() >= poolLimit){
                freeInstance(callContext, bean);
            } else {
                pool.push(bean);
            }
        }
    }

    public void freeInstance(ThreadContext callContext, Object bean) {
        try {
            callContext.setCurrentOperation(Operations.OP_REMOVE);
            Method preDestroy = callContext.getDeploymentInfo().getPreDestroy();
            if (preDestroy != null){
                preDestroy.invoke(bean);
            }
        } catch (Throwable re) {
            logger.error("The bean instance " + bean + " threw a system exception:" + re, re);
        }

    }

    public void discardInstance(ThreadContext callContext, Object bean) {

    }

    static class PoolQueue {
        private final long waitPeriod;

        public PoolQueue(long time) {
            waitPeriod = time;
        }

        public synchronized void waitForAvailableInstance()
                throws org.apache.openejb.InvalidateReferenceException {
            try {
                wait(waitPeriod);
            } catch (InterruptedException ie) {
                throw new org.apache.openejb.InvalidateReferenceException(new RemoteException("No instance avaiable to service request"));
            }
        }

        public synchronized void notifyWaitingThreads() {
            notify();
        }
    }

}
