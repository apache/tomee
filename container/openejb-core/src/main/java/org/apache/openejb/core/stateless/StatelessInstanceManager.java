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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.ws.WebServiceContext;

import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.loader.Options;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.SafeToolkit;
import org.apache.openejb.util.Pool;
import org.apache.xbean.recipe.ConstructionException;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

public class StatelessInstanceManager {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    protected int poolLimit = 0;
    protected Duration timeout;
    protected int beanCount = 0;
    protected boolean strictPooling = false;

    protected final SafeToolkit toolkit = SafeToolkit.getToolkit("StatefulInstanceManager");
    private SecurityService securityService;
    private int poolMin = 0;

    public StatelessInstanceManager(SecurityService securityService, Duration timeout, int poolMin, int poolMax, boolean strictPooling) {
        this.securityService = securityService;
        this.poolLimit = poolMax;
        this.strictPooling = strictPooling;
        this.timeout = timeout;
        this.poolMin = poolMin;

        if (timeout.getUnit() == null) timeout.setUnit(TimeUnit.MILLISECONDS);
        if (this.poolMin > poolLimit) {
            throw new IllegalArgumentException("Minimum pool size cannot be larger than the maximum pool size: min="+ this.poolMin +", max="+poolLimit);
        }
        
        if (strictPooling && poolMax < 1) {
            throw new IllegalArgumentException("Cannot use strict pooling with a pool size less than one.  Strict pooling blocks threads till an instance in the pool is available.  Please increase the pool size or set strict pooling to false");
        }

    }

    /**
     * Removes an instance from the pool and returns it for use
     * by the container in business methods.
     *
     * If the pool is at it's limit the StrictPooling flag will
     * cause this thread to wait.
     *
     * If StrictPooling is not enabled this method will create a
     * new stateless bean instance performing all required injection
     * and callbacks before returning it in a method ready state.
     * 
     * @param callContext
     * @return
     * @throws OpenEJBException
     */
    public Object getInstance(ThreadContext callContext)
            throws OpenEJBException {
        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        Data data = (Data) deploymentInfo.getContainerData();

        final Pool<Instance> pool = data.getPool();

        Instance instance = null;
        try {
            final Pool.Entry<Instance> entry = pool.pop(timeout.getTime(), timeout.getUnit());

            if (entry != null){
                instance = entry.get();
                instance.setPoolEntry(entry);
            }
        } catch (TimeoutException e) {
            throw new IllegalStateException("An invocation of the Stateless Session Bean "+deploymentInfo.getEjbName()+" has timed-out");
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new OpenEJBException("Unexpected Interruption of current thread: ", e);
        }

        if (instance == null) {

            Class beanClass = deploymentInfo.getBeanClass();
            ObjectRecipe objectRecipe = new ObjectRecipe(beanClass);
            objectRecipe.allow(Option.FIELD_INJECTION);
            objectRecipe.allow(Option.PRIVATE_PROPERTIES);
            objectRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
            objectRecipe.allow(Option.NAMED_PARAMETERS);

            Operation originalOperation = callContext.getCurrentOperation();
            BaseContext.State[] originalAllowedStates = callContext.getCurrentAllowedStates();

            try {
                Context ctx = deploymentInfo.getJndiEnc();
                SessionContext sessionContext;
                // This needs to be synchronized as this code is multi-threaded.
                // In between the lookup and the bind a bind may take place in another Thread.
                // This is a fix for GERONIMO-3444
                synchronized(this){
                    try {
                        sessionContext = (SessionContext) ctx.lookup("java:comp/EJBContext");
                    } catch (NamingException e1) {
                        sessionContext = createSessionContext();
                        // TODO: This should work
                        ctx.bind("java:comp/EJBContext", sessionContext);
                    }
                }
                if (javax.ejb.SessionBean.class.isAssignableFrom(beanClass) || hasSetSessionContext(beanClass)) {
                    callContext.setCurrentOperation(Operation.INJECTION);
                    callContext.setCurrentAllowedStates(StatelessContext.getStates());
                    objectRecipe.setProperty("sessionContext", sessionContext);
                }

                // This is a fix for GERONIMO-3444
                synchronized(this){
                    try {
                        ctx.lookup("java:comp/WebServiceContext");
                    } catch (NamingException e) {
                        WebServiceContext wsContext = new EjbWsContext(sessionContext);
                        ctx.bind("java:comp/WebServiceContext", wsContext);
                    }
                }

                fillInjectionProperties(objectRecipe, beanClass, deploymentInfo, ctx);

                Object bean = objectRecipe.create(beanClass.getClassLoader());
                Map unsetProperties = objectRecipe.getUnsetProperties();
                if (unsetProperties.size() > 0) {
                    for (Object property : unsetProperties.keySet()) {
                        logger.warning("Injection: No such property '" + property + "' in class " + beanClass.getName());
                    }
                }

                HashMap<String, Object> interceptorInstances = new HashMap<String, Object>();
                for (InterceptorData interceptorData : deploymentInfo.getAllInterceptors()) {
                    if (interceptorData.getInterceptorClass().equals(beanClass)) continue;

                    Class clazz = interceptorData.getInterceptorClass();
                    ObjectRecipe interceptorRecipe = new ObjectRecipe(clazz);
                    interceptorRecipe.allow(Option.FIELD_INJECTION);
                    interceptorRecipe.allow(Option.PRIVATE_PROPERTIES);
                    interceptorRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
                    interceptorRecipe.allow(Option.NAMED_PARAMETERS);

                    fillInjectionProperties(interceptorRecipe, clazz, deploymentInfo, ctx);

                    try {
                        Object interceptorInstance = interceptorRecipe.create(clazz.getClassLoader());
                        interceptorInstances.put(clazz.getName(), interceptorInstance);
                    } catch (ConstructionException e) {
                        throw new Exception("Failed to create interceptor: " + clazz.getName(), e);
                    }
                }

                interceptorInstances.put(beanClass.getName(), bean);

                callContext.setCurrentOperation(Operation.POST_CONSTRUCT);
                callContext.setCurrentAllowedStates(StatelessContext.getStates());
                List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
                InterceptorStack interceptorStack = new InterceptorStack(bean, null, Operation.POST_CONSTRUCT, callbackInterceptors, interceptorInstances);
                interceptorStack.invoke();

                if (bean instanceof SessionBean){
                    callContext.setCurrentOperation(Operation.CREATE);
                    callContext.setCurrentAllowedStates(StatelessContext.getStates());
                    Method create = deploymentInfo.getCreateMethod();
                    interceptorStack = new InterceptorStack(bean, create, Operation.CREATE, new ArrayList<InterceptorData>(), new HashMap());
                    interceptorStack.invoke();
                }

                instance = new Instance(bean, interceptorInstances);
            } catch (Throwable e) {
                if (e instanceof InvocationTargetException) {
                    e = ((InvocationTargetException) e).getTargetException();
                }
                String t = "The bean instance " + instance + " threw a system exception:" + e;
                logger.error(t, e);
                throw new org.apache.openejb.ApplicationException(new RemoteException("Cannot obtain a free instance.", e));
            } finally {
                callContext.setCurrentOperation(originalOperation);
                callContext.setCurrentAllowedStates(originalAllowedStates);
            }
        }
        return instance;
    }

    private static void fillInjectionProperties(ObjectRecipe objectRecipe, Class clazz, CoreDeploymentInfo deploymentInfo, Context context) {
        boolean usePrefix = true;

        try {
            clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            // Using constructor injection
            // xbean can't handle the prefix yet
            usePrefix = false;
        }

        for (Injection injection : deploymentInfo.getInjections()) {
            if (!injection.getTarget().isAssignableFrom(clazz)) continue;
            try {
                String jndiName = injection.getJndiName();
                Object object = context.lookup("java:comp/env/" + jndiName);
                String prefix;
                if (usePrefix) {
                    prefix = injection.getTarget().getName() + "/";
                } else {
                    prefix = "";
                }

                if (object instanceof String) {
                    String string = (String) object;
                    // Pass it in raw so it could be potentially converted to
                    // another data type by an xbean-reflect property editor
                    objectRecipe.setProperty(prefix + injection.getName(), string);
                } else {
                    objectRecipe.setProperty(prefix + injection.getName(), object);
                }
            } catch (NamingException e) {
                logger.warning("Injection data not found in enc: jndiName='" + injection.getJndiName() + "', target=" + injection.getTarget() + "/" + injection.getName());
            }
        }
    }

    private boolean hasSetSessionContext(Class beanClass) {
        try {
            beanClass.getMethod("setSessionContext", SessionContext.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private SessionContext createSessionContext() {
        return new StatelessContext(securityService);
    }

    /**
     * All instances are removed from the pool in getInstance(...).  They are only
     * returned by the StatelessContainer via this method under two circumstances.
     *
     * 1.  The business method returns normally
     * 2.  The business method throws an application exception
     *
     * Instances are not returned to the pool if the business method threw a system
     * exception.
     *
     * @param callContext
     * @param bean
     * @throws OpenEJBException
     */
    public void poolInstance(ThreadContext callContext, Object bean) throws OpenEJBException {
        if (bean == null) throw new SystemException("Invalid arguments");
        Instance instance = Instance.class.cast(bean);

        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        Data data = (Data) deploymentInfo.getContainerData();

        Pool<Instance> pool = data.getPool();

        if (instance.getPoolEntry() != null){
            if (!pool.push(instance.getPoolEntry())) freeInstance(callContext, instance);
        } else {
            if (!pool.push(instance)) freeInstance(callContext, instance);
        }
    }
    
    /**
     * This method is called to release the semaphore in case of the business method 
     * throwing a system exception
     * 
     * @param callContext
     * @param bean
     */
    public void discardInstance(ThreadContext callContext, Object bean) throws SystemException {
        if (bean == null) throw new SystemException("Invalid arguments");
        Instance instance = Instance.class.cast(bean);

        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        Data data = (Data) deploymentInfo.getContainerData();

        Pool<Instance> pool = data.getPool();

        pool.discard(instance.getPoolEntry());
    }

    private void freeInstance(ThreadContext callContext, Instance instance) {
        try {
            callContext.setCurrentOperation(Operation.PRE_DESTROY);
            callContext.setCurrentAllowedStates(StatelessContext.getStates());
            CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

            Method remove = instance.bean instanceof SessionBean? deploymentInfo.getCreateMethod(): null;

            List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
            InterceptorStack interceptorStack = new InterceptorStack(instance.bean, remove, Operation.PRE_DESTROY, callbackInterceptors, instance.interceptors);

            interceptorStack.invoke();
        } catch (Throwable re) {
            logger.error("The bean instance " + instance + " threw a system exception:" + re, re);
        }

    }

    public void deploy(CoreDeploymentInfo deploymentInfo) {
        Options options = new Options(deploymentInfo.getProperties());

        int max = options.get("PoolSize", poolLimit);
        boolean strict = options.get("StrictPooling", this.strictPooling);
        int min = options.get("PoolMin", poolMin);

        Data data = new Data(max, strict, min);
        deploymentInfo.setContainerData(data);      
    }

    public void undeploy(CoreDeploymentInfo deploymentInfo) {
        Data data = (Data) deploymentInfo.getContainerData();
        if (data == null) return;
        //TODO ejbRemove on each bean in pool.
        //clean pool
        deploymentInfo.setContainerData(null);
    }

    private static final class Data {
        private final Pool<Instance> pool;

        public Data(int poolLimit, boolean strictPooling, int min) {
            pool = new Pool<Instance>(poolLimit, min, strictPooling);
        }

        public Pool<Instance> getPool() {
            return pool;
        }
    }

}
