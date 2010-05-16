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
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executor;
import java.io.Flushable;
import java.io.IOException;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.ws.WebServiceContext;
import javax.management.ObjectName;
import javax.management.MBeanServer;

import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.monitoring.StatsInterceptor;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.monitoring.ManagedMBean;
import org.apache.openejb.loader.Options;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.interceptor.InterceptorInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.SafeToolkit;
import org.apache.openejb.util.Pool;
import org.apache.openejb.util.PassthroughFactory;
import org.apache.xbean.recipe.ConstructionException;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

public class StatelessInstanceManager {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    protected Duration accessTimeout;
    protected Duration closeTimeout;
    protected int beanCount = 0;

    protected final SafeToolkit toolkit = SafeToolkit.getToolkit("StatefulInstanceManager");
    private SecurityService securityService;
    private final Pool.Builder poolBuilder;
    private final Executor executor;

    public StatelessInstanceManager(SecurityService securityService, Duration accessTimeout, Duration closeTimeout, Pool.Builder poolBuilder, int callbackThreads) {
        this.securityService = securityService;
        this.accessTimeout = accessTimeout;
        this.closeTimeout = closeTimeout;
        this.poolBuilder = poolBuilder;
        
        if (accessTimeout.getUnit() == null) accessTimeout.setUnit(TimeUnit.MILLISECONDS);

        executor = new ThreadPoolExecutor(callbackThreads, callbackThreads*2,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    private class StatelessSupplier implements Pool.Supplier<Instance> {
        private final CoreDeploymentInfo deploymentInfo;

        private StatelessSupplier(CoreDeploymentInfo deploymentInfo) {
            this.deploymentInfo = deploymentInfo;
        }

        public void discard(Instance instance, Pool.Event reason) {
            ThreadContext ctx = new ThreadContext(deploymentInfo, null);
            ThreadContext oldCallContext = ThreadContext.enter(ctx);
            try {
                freeInstance(ctx, instance);
            } finally {
                 ThreadContext.exit(oldCallContext);
            }
        }

        public Instance create() {
            return createInstance(deploymentInfo);
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

        Instance instance = null;
        try {
            final Pool.Entry<Instance> entry = data.poolPop();

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

            instance = ceateInstance(callContext);
        }
        return instance;
    }

    private Instance ceateInstance(ThreadContext callContext) throws org.apache.openejb.ApplicationException {
        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
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
                    sessionContext = createSessionContext(deploymentInfo);
                    // TODO: This should work
                    ctx.bind("java:comp/EJBContext", sessionContext);
                }
            }
            if (SessionBean.class.isAssignableFrom(beanClass) || hasSetSessionContext(beanClass)) {
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

            // Add the stats interceptor instance and other already created interceptor instances
            for (InterceptorInstance interceptorInstance : deploymentInfo.getSystemInterceptors()) {
                Class clazz = interceptorInstance.getData().getInterceptorClass();
                interceptorInstances.put(clazz.getName(), interceptorInstance.getInterceptor());
            }

            for (InterceptorData interceptorData : deploymentInfo.getInstanceScopedInterceptors()) {
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

            return new Instance(bean, interceptorInstances);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }
            String t = "The bean instance " + deploymentInfo.getDeploymentID() + " threw a system exception:" + e;
            logger.error(t, e);
            throw new org.apache.openejb.ApplicationException(new RemoteException("Cannot obtain a free instance.", e));
        } finally {
            callContext.setCurrentOperation(originalOperation);
            callContext.setCurrentAllowedStates(originalAllowedStates);
        }
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

    private SessionContext createSessionContext(CoreDeploymentInfo deploymentInfo) {
        final Data data = (Data) deploymentInfo.getContainerData();
        
        return new StatelessContext(securityService, new Flushable(){
            public void flush() throws IOException {
                data.getPool().flush();
            }
        });
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
            pool.push(instance.getPoolEntry());
        } else {
            pool.push(instance);
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

        final Pool.Builder builder = new Pool.Builder(poolBuilder);

        String timeString = options.get("Timeout", this.accessTimeout.toString());
        timeString = options.get("AccessTimeout", timeString);
        Duration accessTimeout = new Duration(timeString);

        String s = options.get("CloseTimeout", this.closeTimeout.toString());
        Duration closeTimeout = new Duration(s);

        final ObjectRecipe recipe = PassthroughFactory.recipe(builder);
        recipe.setAllProperties(deploymentInfo.getProperties());

        builder.setSupplier(new StatelessSupplier(deploymentInfo));
        builder.setExecutor(executor);
        
        Data data = new Data(builder.build(), accessTimeout, closeTimeout);
        deploymentInfo.setContainerData(data);

        final int min = builder.getMin();
        long maxAge = builder.getMaxAge().getTime(TimeUnit.MILLISECONDS);
        double maxAgeOffset = builder.getMaxAgeOffset();

        // Create stats interceptor
        StatsInterceptor stats = new StatsInterceptor(deploymentInfo.getBeanClass());
        deploymentInfo.addSystemInterceptor(stats);

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "openejb");
        jmxName.set("J2EEApplication", null);
        jmxName.set("EJBModule", deploymentInfo.getModuleID());
        jmxName.set("StatelessSessionBean", deploymentInfo.getEjbName());
        jmxName.set("j2eeType", "");
        jmxName.set("name", deploymentInfo.getEjbName());

        // register the invocation stats interceptor
        try {
            ObjectName objectName = jmxName.set("j2eeType", "Invocations").build();
            server.registerMBean(new ManagedMBean(stats), objectName);
            data.add(objectName);
        } catch (Exception e) {
            logger.error("Unable to register MBean ", e);
        }

        // register the pool
        try {
            ObjectName objectName = jmxName.set("j2eeType", "Pool").build();
            server.registerMBean(new ManagedMBean(data.pool), objectName);
            data.add(objectName);
        } catch (Exception e) {
            logger.error("Unable to register MBean ", e);
        }

        // Finally, fill the pool and start it
        for (int i = 0; i < min; i++) {
            Instance obj = createInstance(deploymentInfo);

            if (obj == null) continue;

            long offset = maxAge > 0 ? ((long) (maxAge / min * i * maxAgeOffset)) % maxAge : 0l;

            data.getPool().add(obj, offset);
        }

        data.getPool().start();
    }

    private Instance createInstance(CoreDeploymentInfo deploymentInfo) {
        ThreadContext ctx = new ThreadContext(deploymentInfo, null);
        ThreadContext oldCallContext = ThreadContext.enter(ctx);
        try {
            return ceateInstance(ctx);
        } catch (OpenEJBException e) {
            logger.error("Unable to fill pool to mimimum size: for deployment '" + deploymentInfo.getDeploymentID() + "'", e);
        } finally {
             ThreadContext.exit(oldCallContext);
        }

        return null;
    }

    public void undeploy(CoreDeploymentInfo deploymentInfo) {
        Data data = (Data) deploymentInfo.getContainerData();
        if (data == null) return;

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        for (ObjectName objectName : data.jmxNames) {
            try {
                server.unregisterMBean(objectName);
            } catch (Exception e) {
                logger.error("Unable to unregister MBean "+objectName);
            }
        }

        try {
            if (!data.closePool()) {
                logger.error("Timed-out waiting for stateless pool to close: for deployment '" + deploymentInfo.getDeploymentID() + "'");
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        deploymentInfo.setContainerData(null);
    }

    private static final class Data {
        private final Pool<Instance> pool;
        private final Duration accessTimeout;
        private final Duration closeTimeout;
        private final List<ObjectName> jmxNames = new ArrayList<ObjectName>();

        private Data(Pool<Instance> pool, Duration accessTimeout, Duration closeTimeout) {
            this.pool = pool;
            this.accessTimeout = accessTimeout;
            this.closeTimeout = closeTimeout;
        }

        public Duration getAccessTimeout() {
            return accessTimeout;
        }

        public Pool.Entry<Instance> poolPop() throws InterruptedException, TimeoutException {
            return pool.pop(accessTimeout.getTime(), accessTimeout.getUnit());
        }

        public Pool<Instance> getPool() {
            return pool;
        }

        public boolean closePool() throws InterruptedException {
            return pool.close(closeTimeout.getTime(), closeTimeout.getUnit());
        }

        public ObjectName add(ObjectName name) {
            jmxNames.add(name);
            return name;
        }
    }

}
