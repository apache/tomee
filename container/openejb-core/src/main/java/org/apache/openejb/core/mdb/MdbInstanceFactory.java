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

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.Injection;
import org.apache.openejb.util.Logger;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.StaticRecipe;
import org.apache.xbean.recipe.ConstructionException;

import javax.ejb.MessageDrivenBean;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.resource.spi.UnavailableException;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

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
    private static final Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    private final CoreDeploymentInfo deploymentInfo;
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
    public MdbInstanceFactory(CoreDeploymentInfo deploymentInfo, TransactionManager transactionManager, SecurityService securityService, int instanceLimit) {
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
     * @param ignoreInstanceCount
     */
    public Object createInstance(boolean ignoreInstanceCount) throws UnavailableException {
        if (!ignoreInstanceCount) {
            synchronized (this) {
                // check the instance limit
                if (instanceLimit > 0 && instanceCount >= instanceLimit) {
                    throw new UnavailableException("Only " + instanceLimit + " instances can be created");
                }
                // increment the instance count
                instanceCount++;
            }
        }

        try {
            Object bean = constructBean();
            return bean;
        } catch (UnavailableException e) {
            // decrement the instance count
            if (!ignoreInstanceCount) {
                synchronized (this) {
                    instanceCount--;
                }
            }

            throw e;
        }
    }

    /**
     * Frees an instance no longer needed by the resource adapter.  This method makes all the necessary lifecycle
     * callbacks and decrements the instance count.  This method should not be used to disposed of beans that have
     * thrown a system exception.  Instead the discardInstance method should be called.
     * @param bean the bean instance to free
     * @param ignoredInstanceCount
     */
    public void freeInstance(Object bean, boolean ignoredInstanceCount) {
        if (bean == null) throw new NullPointerException("bean is null");

        // decrement the instance count
        if (!ignoredInstanceCount) {
            synchronized (this) {
                instanceCount--;
            }
        }

        ThreadContext callContext = ThreadContext.getThreadContext();
        Operation originalOperation = callContext.getCurrentOperation();
        try {
            // call post destroy method
            callContext.setCurrentOperation(Operation.REMOVE);

            Method remove = bean instanceof MessageDrivenBean ? MessageDrivenBean.class.getMethod("ejbRemove"): null;

            List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
            ArrayList interceptorDatas = new ArrayList(); // TODO
            HashMap interceptorInstances = new HashMap(); // TODO
            InterceptorStack interceptorStack = new InterceptorStack(bean, remove, Operation.REMOVE, interceptorDatas, interceptorInstances);

            interceptorStack.invoke();
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

        ThreadContext callContext = new ThreadContext(deploymentInfo, null, null, Operation.INJECTION);
        ThreadContext.enter(callContext);
        try {
            Context ctx = deploymentInfo.getJndiEnc();
            // construct the bean instance
            MdbContext mdbContext = null;
            try {
                mdbContext = (MdbContext) ctx.lookup("java:comp/EJBContext");
            } catch (NamingException e) {
                mdbContext = new MdbContext(transactionManager, securityService);
                ctx.bind("java:comp/EJBContext",mdbContext);
            }
            for (Injection injection : deploymentInfo.getInjections()) {
                try {
                    String jndiName = injection.getJndiName();
                    Object object = ctx.lookup("java:comp/env/" + jndiName);
                    if (object instanceof String) {
                        String string = (String) object;
                        // Pass it in raw so it could be potentially converted to
                        // another data type by an xbean-reflect property editor
                        objectRecipe.setProperty(injection.getName(), string);
                    } else {
                        objectRecipe.setProperty(injection.getName(), new StaticRecipe(object));
                    }
                } catch (NamingException e) {
                    logger.warning("Injection data not found in enc: jndiName='"+injection.getJndiName()+"', target="+injection.getTarget()+"/"+injection.getName());
                }
            }
            // only in this case should the callback be used
            callContext.setCurrentOperation(Operation.INJECTION);
            if(MessageDrivenBean.class.isAssignableFrom(beanClass)) {
                objectRecipe.setProperty("messageDrivenContext", new StaticRecipe(mdbContext));
            }
            Object bean = objectRecipe.create();

            HashMap<String, Object> interceptorInstances = new HashMap<String, Object>();
            for (InterceptorData interceptorData : deploymentInfo.getAllInterceptors()) {
                if (interceptorData.getInterceptorClass().equals(beanClass)) continue;

                Class clazz = interceptorData.getInterceptorClass();
                ObjectRecipe interceptorRecipe = new ObjectRecipe(clazz);
                try {
                    Object interceptorInstance = interceptorRecipe.create(clazz.getClassLoader());
                    interceptorInstances.put(clazz.getName(), interceptorInstance);
                } catch (ConstructionException e) {
                    throw new Exception("Failed to create interceptor: " + clazz.getName(), e);
                }
            }

            // TODO: We need to keep these somehwere
            interceptorInstances.put(beanClass.getName(), bean);

            try {
                callContext.setCurrentOperation(Operation.POST_CONSTRUCT);

                List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
                InterceptorStack interceptorStack = new InterceptorStack(bean, null, Operation.POST_CONSTRUCT, callbackInterceptors, interceptorInstances);
                interceptorStack.invoke();
            } catch (Exception e) {
                throw e;
            }

            try {
                if (bean instanceof MessageDrivenBean){
                    callContext.setCurrentOperation(Operation.CREATE);
                    Method create = deploymentInfo.getCreateMethod();
                    InterceptorStack interceptorStack = new InterceptorStack(bean, create, Operation.CREATE, new ArrayList(), new HashMap());
                    interceptorStack.invoke();
                }
            } catch (Exception e) {
                throw e;
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
            ThreadContext.exit(callContext);
        }
    }

}
