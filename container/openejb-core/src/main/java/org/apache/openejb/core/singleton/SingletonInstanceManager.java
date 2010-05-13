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
package org.apache.openejb.core.singleton;

import java.lang.reflect.Method;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.NoSuchEJBException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.ws.WebServiceContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.monitoring.StatsInterceptor;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.monitoring.ManagedMBean;
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
import org.apache.openejb.util.SafeToolkit;
import org.apache.xbean.recipe.ConstructionException;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

public class SingletonInstanceManager {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    protected final SafeToolkit toolkit = SafeToolkit.getToolkit("SingletonInstanceManager");
    private SecurityService securityService;

    public SingletonInstanceManager(SecurityService securityService) {
        this.securityService = securityService;
    }

    public Instance getInstance(final ThreadContext callContext) throws OpenEJBException {
        final CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        Data data = (Data) deploymentInfo.getContainerData();
        AtomicReference<Future<Instance>> singleton = data.singleton;
        try {
            // Has the singleton been created yet?
            // If there is a Future object in the AtomicReference, then
            // it's either been created or is being created now.
            Future<Instance> singletonFuture = singleton.get();
            if (singletonFuture != null) return singletonFuture.get();

            // The singleton has not been created nor is being created
            // We will construct this FutureTask and compete with the
            // other threads for the right to create the singleton
            FutureTask<Instance> task = new FutureTask<Instance>(new Callable<Instance>() {
                public Instance call() throws Exception {
                    return createInstance(callContext, deploymentInfo);
                }
            });


            do {
                // If our FutureTask was the one to win the slot
                // than we are the ones responsisble for creating
                // the singleton while the others wait.
                if (singleton.compareAndSet(null, task)) {
                    task.run();
                }

                // If we didn't win the slot and no other FutureTask
                // has been set by a different thread, than we need
                // to try again.
            } while ((singletonFuture = singleton.get()) == null);


            // At this point we can safely return the singleton
            return singletonFuture.get();

        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new ApplicationException(new NoSuchEJBException("Singleton initialization interrupted").initCause(e));
        } catch (ExecutionException e) {
            Throwable throwable = e.getCause();
            if (throwable instanceof ApplicationException) {
                throw (ApplicationException) throwable;
            }

            throw new ApplicationException(new NoSuchEJBException("Singleton initialization failed").initCause(e.getCause()));
        }
    }

    private Instance createInstance(ThreadContext callContext, CoreDeploymentInfo deploymentInfo) throws org.apache.openejb.ApplicationException {
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

            if (SessionBean.class.isAssignableFrom(beanClass) || hasSetSessionContext(beanClass)) {
                callContext.setCurrentOperation(Operation.INJECTION);
                callContext.setCurrentAllowedStates(SingletonContext.getStates());
                objectRecipe.setProperty("sessionContext", sessionContext);
            }

            // This is a fix for GERONIMO-3444
            synchronized(this){
                try {
                    ctx.lookup("java:comp/WebServiceContext");
                } catch (NamingException e) {
                    WebServiceContext wsContext;
                    wsContext = new EjbWsContext(sessionContext);
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
            callContext.setCurrentAllowedStates(SingletonContext.getStates());

            List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
            InterceptorStack interceptorStack = new InterceptorStack(bean, null, Operation.POST_CONSTRUCT, callbackInterceptors, interceptorInstances);
            interceptorStack.invoke();

            if (bean instanceof SessionBean){
                callContext.setCurrentOperation(Operation.CREATE);
                callContext.setCurrentAllowedStates(SingletonContext.getStates());
                Method create = deploymentInfo.getCreateMethod();
                interceptorStack = new InterceptorStack(bean, create, Operation.CREATE, new ArrayList<InterceptorData>(), new HashMap());
                interceptorStack.invoke();
            }

            ReadWriteLock lock;
            if (deploymentInfo.isBeanManagedConcurrency()){
                // Bean-Managed Concurrency
                lock = new BeanManagedLock();
            } else {
                // Container-Managed Concurrency
                lock = new ReentrantReadWriteLock();
            }

            return new Instance(bean, interceptorInstances, lock);
        } catch (Throwable e) {
            if (e instanceof java.lang.reflect.InvocationTargetException) {
                e = ((java.lang.reflect.InvocationTargetException) e).getTargetException();
            }
            String t = "The bean instance threw a system exception:" + e;
            logger.error(t, e);
            throw new ApplicationException(new NoSuchEJBException("Singleton failed to initialize").initCause(e));
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

    private SessionContext createSessionContext() {
        return new SingletonContext(securityService);
    }

    public void freeInstance(ThreadContext callContext) {
        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
        Data data = (Data) deploymentInfo.getContainerData();
        Future<Instance> instanceFuture = data.singleton.get();

        // Possible the instance was never created
        if (instanceFuture == null) return;

        Instance instance = null;
        try {
            instance = instanceFuture.get();
        } catch (InterruptedException e) {
            Thread.interrupted();
            logger.error("Singleton shutdown failed because the thread was interrupted: "+deploymentInfo.getDeploymentID(), e);
            return;
        } catch (ExecutionException e) {
            // Instance was never initialized
        }


        try {
            callContext.setCurrentOperation(Operation.PRE_DESTROY);
            callContext.setCurrentAllowedStates(SingletonContext.getStates());

            Method remove = instance.bean instanceof SessionBean? deploymentInfo.getCreateMethod(): null;

            List<InterceptorData> callbackInterceptors = deploymentInfo.getCallbackInterceptors();
            InterceptorStack interceptorStack = new InterceptorStack(instance.bean, remove, Operation.PRE_DESTROY, callbackInterceptors, instance.interceptors);

            interceptorStack.invoke();
        } catch (Throwable re) {
            logger.error("Singleton shutdown failed: "+deploymentInfo.getDeploymentID(), re);
        }

    }

    /**
     * This method has no work to do as all instances are removed from
     * the pool on getInstance(...) and not returned via poolInstance(...)
     * if they threw a system exception.
     *
     * @param callContext
     * @param bean
     */
    public void discardInstance(ThreadContext callContext, Object bean) {

    }

    public void deploy(CoreDeploymentInfo deploymentInfo) {
        Data data = new Data();
        deploymentInfo.setContainerData(data);

        // Create stats interceptor
        StatsInterceptor stats = new StatsInterceptor(deploymentInfo.getBeanClass());
        deploymentInfo.addSystemInterceptor(stats);

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "openejb");
        jmxName.set("J2EEApplication", null);
        jmxName.set("EJBModule", deploymentInfo.getModuleID());
        jmxName.set("SingletonSessionBean", deploymentInfo.getEjbName());
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

        deploymentInfo.setContainerData(null);
    }

    private static final class Data {
        private final AtomicReference<Future<Instance>> singleton = new AtomicReference<Future<Instance>>();
        private final List<ObjectName> jmxNames = new ArrayList<ObjectName>();

        public ObjectName add(ObjectName name) {
            jmxNames.add(name);
            return name;
        }
    }


    private static class BeanManagedLock implements ReadWriteLock {
        private final Lock lock =  new Lock(){
            public void lock() {
            }

            public void lockInterruptibly() {
            }

            public Condition newCondition() {
                throw new java.lang.UnsupportedOperationException("newCondition()");
            }

            public boolean tryLock() {
                return true;
            }

            public boolean tryLock(long time, TimeUnit unit) {
                return true;
            }

            public void unlock() {
            }
        };

        public Lock readLock() {
            return lock;
        }

        public Lock writeLock() {
            return lock;
        }
    }
}
