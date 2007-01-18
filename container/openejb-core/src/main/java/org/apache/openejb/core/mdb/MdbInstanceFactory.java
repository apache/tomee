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

import org.apache.log4j.Logger;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.SecurityService;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.StaticRecipe;
import org.apache.xbean.recipe.Option;

import javax.ejb.MessageDrivenBean;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.resource.spi.UnavailableException;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;

/**
 * A MdbInstanceFactory creates instances of message driven beans for a single instance. This class differs from other
 * instance managers in OpenEJB as it doesn't do pooling and it creates instances for only a single EJB deployment.
 * </p>
 * The MdbContainer assumes that the resouce adapter is pooling message endpoints so a second level of pooling in the
 * container would be inefficient.  This is true of all known resouce adapters in opensource (ActiveMQ), so if this is
 * a poor assumption for your resource adapter, contact the OpenEJB developers.
 * </p>
 * This class can optionally limit the number of bean instances and therefore the message endpoints available to the
 * resource adapter.
 */
public class MdbInstanceFactory {
    private static final Logger logger = Logger.getLogger("OpenEJB");

    private final DeploymentInfo deploymentInfo;
    private final TransactionManager transactionManager;
    private final SecurityService securityService;
    private final int instanceLimit;
    private int instanceCount;

    /**
     * Creates a MdbInstanceFactory for a single specific deployment.
     * @param deploymentInfo the deployment for which instances will be created
     * @param transactionManager the transaction manager for this container system
     * @param securityService the transaction manager for this container system
     * @param instanceLimit the maximal number of instances or <= 0 if unlimited
     */
    public MdbInstanceFactory(DeploymentInfo deploymentInfo, TransactionManager transactionManager, SecurityService securityService, int instanceLimit) {
        this.deploymentInfo = deploymentInfo;
        this.transactionManager = transactionManager;
        this.securityService = securityService;
        this.instanceLimit = instanceLimit;
    }

    /**
     * Gets the maximal number of instances that can exist at any time.
     * @return the maximum number of instances or <= 0 if unlimitied
     */
    public int getInstanceLimit() {
        return instanceLimit;
    }

    /**
     * Gets the current number of created instances.
     * @return the current number of instances created
     */
    public synchronized int getInstanceCount() {
        return instanceCount;
    }

    /**
     * Creates a new mdb instance preforming all necessary lifecycle callbacks
     * @return a new message driven bean instance
     * @throws UnavailableException if the instance limit has been exceeded or
     *   if an exception occurs while creating the bean instance
     */
    public Object createInstance() throws UnavailableException {
        synchronized (this) {
            // check the instance limit
            if (instanceLimit > 0 && instanceCount >= instanceLimit) {
                throw new UnavailableException("Only " + instanceLimit + " instances can be created");
            }
            // increment the instance count
            instanceCount++;
        }

        try {
            Object bean = constructBean();
            return bean;
        } catch (UnavailableException e) {
            // decrement the instance count
            synchronized (this) {
                instanceCount--;
            }

            throw e;
        }
    }

    /**
     * Frees an instance no longer needed by the resource adapter.  This method makes all the necessary lifecycle
     * callbacks and decrements the instance count.  This method should not be used to disposed of beans that have
     * thrown a system exception.  Instead the discardInstance method should be called.
     * @param bean the bean instance to free
     */
    public void freeInstance(Object bean) {
        if (bean == null) throw new NullPointerException("bean is null");

        // decrement the instance count
        synchronized (this) {
            instanceCount--;
        }

        ThreadContext callContext = ThreadContext.getThreadContext();
        Operation originalOperation = callContext.getCurrentOperation();
        try {
            // call post destroy method
            callContext.setCurrentOperation(Operation.REMOVE);
            Method preDestroy = callContext.getDeploymentInfo().getPreDestroy();
            if (preDestroy != null){
                preDestroy.invoke(bean);
            }
        } catch (Throwable re) {
            MdbInstanceFactory.logger.error("The bean instance " + bean + " threw a system exception:" + re, re);
        } finally {
            callContext.setCurrentOperation(originalOperation);
        }
    }

    /**
     * Recreates a bean instance that has thrown a system exception.  As required by the EJB specification, lifecycle
     * callbacks are not invoked.  To normally free a bean instance call the freeInstance method.
     * @param bean the bean instance to discard
     * @return the new replacement bean instance
     */
    public Object recreateInstance(Object bean) throws UnavailableException {
        if (bean == null) throw new NullPointerException("bean is null");
        Object newBean = constructBean();
        return newBean;
    }

    private Object constructBean() throws UnavailableException {
        Class beanClass = deploymentInfo.getBeanClass();
        ObjectRecipe objectRecipe = new ObjectRecipe(beanClass);
        objectRecipe.allow(Option.FIELD_INJECTION);
        objectRecipe.allow(Option.PRIVATE_PROPERTIES);
        objectRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);

        ThreadContext callContext = ThreadContext.getThreadContext();
        Operation originalOperation = callContext.getCurrentOperation();
        try {
            Context ctx = deploymentInfo.getJndiEnc();
            // construct the bean instance
            OldMdbContext mdbContext = null;
            try {
                mdbContext = (OldMdbContext) ctx.lookup("java:comp/EJBContext");
            } catch (NamingException e) {
                mdbContext = new OldMdbContext(transactionManager, securityService);
                ctx.bind("java:comp/EJBContext",mdbContext);
            }
            // only in this case should the callback be used
            if(MessageDrivenBean.class.isAssignableFrom(beanClass)) {
                callContext.setCurrentOperation(Operation.SET_CONTEXT);
                objectRecipe.setProperty("messageDrivenContext", new StaticRecipe(mdbContext));
            }
            Object bean = objectRecipe.create();

            // call the post construct method
            callContext.setCurrentOperation(Operation.CREATE);
            Method postConstruct = deploymentInfo.getPostConstruct();
            if (postConstruct != null){
                postConstruct.invoke(bean);
            }


            return bean;
        } catch (Throwable e) {
            if (e instanceof java.lang.reflect.InvocationTargetException) {
                e = ((java.lang.reflect.InvocationTargetException) e).getTargetException();
            }
            String message = "The bean instance threw a system exception:" + e;
            MdbInstanceFactory.logger.error(message, e);
            throw new UnavailableException(message, e);
        } finally {
            callContext.setCurrentOperation(originalOperation);
        }
    }

}
