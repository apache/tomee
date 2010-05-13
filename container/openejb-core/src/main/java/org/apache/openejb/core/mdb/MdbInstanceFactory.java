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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ejb.MessageDrivenBean;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.resource.spi.UnavailableException;

import org.apache.openejb.Injection;
import org.apache.openejb.InjectionProcessor;
import static org.apache.openejb.InjectionProcessor.unwrap;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.interceptor.InterceptorInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.recipe.ConstructionException;
import org.apache.xbean.recipe.ObjectRecipe;

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
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    private final CoreDeploymentInfo deploymentInfo;
    private final SecurityService securityService;
    private final int instanceLimit;
    private int instanceCount;

    /**
     * Creates a MdbInstanceFactory for a single specific deployment.
     * @param deploymentInfo the deployment for which instances will be created
     * @param securityService the transaction manager for this container system
     * @param instanceLimit the maximal number of instances or <= 0 if unlimited
     */
    public MdbInstanceFactory(CoreDeploymentInfo deploymentInfo, SecurityService securityService, int instanceLimit) {
        this.deploymentInfo = deploymentInfo;
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
     * @param instance the bean instance to free
     * @param ignoredInstanceCount
     */
    public void freeInstance(Instance instance, boolean ignoredInstanceCount) {
        if (instance == null) throw new NullPointerException("bean is null");

        // decrement the instance count
        if (!ignoredInstanceCount) {
            synchronized (this) {
                instanceCount--;
            }
        }

        ThreadContext callContext = ThreadContext.getThreadContext();
        Operation originalOperation = callContext.getCurrentOperation();
        BaseContext.State[] originalAllowedStates = callContext.getCurrentAllowedStates();
        try {
            // call post destroy method
            callContext.setCurrentOperation(Operation.PRE_DESTROY);
            callContext.setCurrentAllowedStates(MdbContext.getStates());
            Method remove = instance.bean instanceof MessageDrivenBean ? MessageDrivenBean.class.getMethod("ejbRemove"): null;
            List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
            InterceptorStack interceptorStack = new InterceptorStack(instance.bean, remove, Operation.PRE_DESTROY, callbackInterceptors, instance.interceptors);
            interceptorStack.invoke();
        } catch (Throwable re) {
            MdbInstanceFactory.logger.error("The bean instance " + instance.bean + " threw a system exception:" + re, re);
        } finally {
            callContext.setCurrentOperation(originalOperation);
            callContext.setCurrentAllowedStates(originalAllowedStates);
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

        ThreadContext callContext = new ThreadContext(deploymentInfo, null, Operation.INJECTION);
        ThreadContext oldContext = ThreadContext.enter(callContext);
        try {
            Context ctx = deploymentInfo.getJndiEnc();
            // construct the bean instance
            MdbContext mdbContext;
            synchronized(this) {
                try {
                    mdbContext = (MdbContext) ctx.lookup("java:comp/EJBContext");
                } catch (NamingException e) {
                    mdbContext = new MdbContext(securityService);
                    ctx.bind("java:comp/EJBContext",mdbContext);
                }
            }

            InjectionProcessor injectionProcessor = new InjectionProcessor(beanClass, deploymentInfo.getInjections(), null, null, unwrap(ctx));

            // only in this case should the callback be used
            callContext.setCurrentOperation(Operation.INJECTION);
            callContext.setCurrentAllowedStates(MdbContext.getStates());
            if(MessageDrivenBean.class.isAssignableFrom(beanClass)) {
                injectionProcessor.setProperty("messageDrivenContext", mdbContext);
            }
            Object bean = injectionProcessor.createInstance();

            HashMap<String, Object> interceptorInstances = new HashMap<String, Object>();

            // Add the stats interceptor instance and other already created interceptor instances
            for (InterceptorInstance interceptorInstance : deploymentInfo.getSystemInterceptors()) {
                Class clazz = interceptorInstance.getData().getInterceptorClass();
                interceptorInstances.put(clazz.getName(), interceptorInstance.getInterceptor());
            }

            for (InterceptorData interceptorData : deploymentInfo.getInstanceScopedInterceptors()) {
                if (interceptorData.getInterceptorClass().equals(beanClass)) continue;

                Class clazz = interceptorData.getInterceptorClass();
                InjectionProcessor interceptorInjector = new InjectionProcessor(clazz, deploymentInfo.getInjections(), unwrap(ctx));

                try {
                    Object interceptorInstance = interceptorInjector.createInstance();
                    interceptorInstances.put(clazz.getName(), interceptorInstance);
                } catch (ConstructionException e) {
                    throw new Exception("Failed to create interceptor: " + clazz.getName(), e);
                }
            }

            // TODO: We need to keep these somehwere
            interceptorInstances.put(beanClass.getName(), bean);
            Instance instance = new Instance(bean,interceptorInstances);
            callContext.setCurrentOperation(Operation.POST_CONSTRUCT);
            callContext.setCurrentAllowedStates(MdbContext.getStates());

            List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
            InterceptorStack interceptorStack = new InterceptorStack(bean, null, Operation.POST_CONSTRUCT, callbackInterceptors, interceptorInstances);
            interceptorStack.invoke();

            if (bean instanceof MessageDrivenBean){
                callContext.setCurrentOperation(Operation.CREATE);
                callContext.setCurrentAllowedStates(MdbContext.getStates());
                Method create = deploymentInfo.getCreateMethod();
                interceptorStack = new InterceptorStack(bean, create, Operation.CREATE, new ArrayList(), new HashMap());
                interceptorStack.invoke();
            }

            return instance;
        } catch (Throwable e) {
            if (e instanceof java.lang.reflect.InvocationTargetException) {
                e = ((java.lang.reflect.InvocationTargetException) e).getTargetException();
            }
            String message = "The bean instance threw a system exception:" + e;
            MdbInstanceFactory.logger.error(message, e);
            throw new UnavailableException(message, e);
        } finally {
            ThreadContext.exit(oldContext);
        }
    }

    private static void fillInjectionProperties(ObjectRecipe objectRecipe, Class clazz, CoreDeploymentInfo deploymentInfo, Context context) {
        for (Injection injection : deploymentInfo.getInjections()) {
            if (!injection.getTarget().isAssignableFrom(clazz)) continue;
            try {
                String jndiName = injection.getJndiName();
                Object object = context.lookup("java:comp/env/" + jndiName);
                if (object instanceof String) {
                    String string = (String) object;
                    // Pass it in raw so it could be potentially converted to
                    // another data type by an xbean-reflect property editor
                    objectRecipe.setProperty(injection.getTarget().getName() + "/" + injection.getName(), string);
                } else {
                    objectRecipe.setProperty(injection.getTarget().getName() + "/" + injection.getName(), object);
                }
            } catch (NamingException e) {
                logger.warning("Injection data not found in enc: jndiName='" + injection.getJndiName() + "', target=" + injection.getTarget() + "/" + injection.getName());
            }
        }
    }

}
