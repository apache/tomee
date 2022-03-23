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

package org.apache.openejb.core.singleton;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.cdi.CdiEjbBean;
import org.apache.openejb.core.InstanceContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.timer.TimerServiceWrapper;
import org.apache.openejb.core.transaction.EjbTransactionUtil;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ManagedMBean;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.monitoring.StatsInterceptor;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.ejb.EJBContext;
import jakarta.ejb.NoSuchEJBException;
import jakarta.ejb.SessionBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;
import jakarta.xml.ws.WebServiceContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SingletonInstanceManager {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    private final SecurityService securityService;
    private final SingletonContext sessionContext;
    private final WebServiceContext webServiceContext;

    public SingletonInstanceManager(final SecurityService securityService) {
        this.securityService = securityService;
        sessionContext = new SingletonContext(this.securityService);
        webServiceContext = new EjbWsContext(sessionContext);
    }

    protected void start(final BeanContext beanContext) throws OpenEJBException {
        if (beanContext.isLoadOnStartup()) {
            initialize(beanContext);
        }
    }

    private void initialize(final BeanContext beanContext) throws OpenEJBException {
        try {
            final ThreadContext callContext = new ThreadContext(beanContext, null);
            final ThreadContext old = ThreadContext.enter(callContext);
            try {
                getInstance(callContext);
            } finally {
                ThreadContext.exit(old);
            }
        } catch (final OpenEJBException e) {
            throw new OpenEJBException("Singleton startup failed: " + beanContext.getDeploymentID(), e);
        }
    }

    public Instance getInstance(final ThreadContext callContext) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();
        final Data data = (Data) beanContext.getContainerData();
        final AtomicReference<Future<Instance>> singleton = data.singleton;
        try {
            // Has the singleton been created yet?
            // If there is a Future object in the AtomicReference, then
            // it's either been created or is being created now.
            Future<Instance> singletonFuture = singleton.get();
            if (singletonFuture != null) {
                return singletonFuture.get();
            }

            // The singleton has not been created nor is being created
            // We will construct this FutureTask and compete with the
            // other threads for the right to create the singleton
            final FutureTask<Instance> task = new FutureTask<Instance>(new Callable<Instance>() {
                public Instance call() throws Exception {
                    return createInstance(callContext, beanContext);
                }
            });

            do {
                // If our FutureTask was the one to win the slot
                // than we are the ones responsible for creating
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

        } catch (final InterruptedException e) {
            Thread.interrupted();
            throw new ApplicationException(new NoSuchEJBException("Singleton initialization interrupted").initCause(e));
        } catch (final ExecutionException e) {
            final Throwable throwable = e.getCause();
            if (throwable instanceof ApplicationException) {
                throw (ApplicationException) throwable;
            }

            throw new ApplicationException(new NoSuchEJBException("Singleton initialization failed").initCause(e.getCause()));
        }
    }

    private void initializeDependencies(final BeanContext beanContext) throws OpenEJBException {
        final SystemInstance systemInstance = SystemInstance.get();
        final ContainerSystem containerSystem = systemInstance.getComponent(ContainerSystem.class);
        for (final String dependencyId : beanContext.getDependsOn()) {
            final BeanContext dependencyContext = containerSystem.getBeanContext(dependencyId);
            if (dependencyContext == null) {
                throw new OpenEJBException("Deployment does not exist. Deployment(id='" + dependencyContext + "')");
            }

            final Object containerData = dependencyContext.getContainerData();

            // Bean may not be a singleton or may be a singleton
            // managed by a different container implementation
            if (containerData instanceof Data) {
                final Data data = (Data) containerData;

                data.initialize();
            }
        }
    }

    private Instance createInstance(final ThreadContext callContext, final BeanContext beanContext) throws ApplicationException {
        try {
            initializeDependencies(beanContext);

            final InstanceContext context = beanContext.newInstance();

            if (context.getBean() instanceof SessionBean) {

                final Operation originalOperation = callContext.getCurrentOperation();
                try {
                    callContext.setCurrentOperation(Operation.CREATE);
                    final Method create = beanContext.getCreateMethod();
                    final InterceptorStack ejbCreate = new InterceptorStack(context.getBean(), create, Operation.CREATE, new ArrayList<>(), new HashMap());
                    ejbCreate.invoke();
                } finally {
                    callContext.setCurrentOperation(originalOperation);
                }
            }

            final ReadWriteLock lock;
            if (beanContext.isBeanManagedConcurrency()) {
                // Bean-Managed Concurrency
                lock = new BeanManagedLock();
            } else {
                // Container-Managed Concurrency
                lock = new ReentrantReadWriteLock();
            }

            return new Instance(context.getBean(), context.getInterceptors(), context.getCreationalContext(), lock);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }
            final String t = "The bean instance " + beanContext.getDeploymentID() + " threw a system exception:" + e;
            logger.error(t, e);
            throw new ApplicationException(new NoSuchEJBException("Singleton failed to initialize").initCause(e));
        }
    }

    public void freeInstance(final ThreadContext callContext) {
        final BeanContext beanContext = callContext.getBeanContext();
        final Data data = (Data) beanContext.getContainerData();
        final Future<Instance> instanceFuture = data.singleton.get();

        // Possible the instance was never created
        if (instanceFuture == null) {
            return;
        }

        final Instance instance;
        try {
            instance = instanceFuture.get();
        } catch (final InterruptedException e) {
            Thread.interrupted();
            logger.error("Singleton shutdown failed because the thread was interrupted: " + beanContext.getDeploymentID(), e);
            return;
        } catch (final ExecutionException e) {
            // Instance was never initialized
            return;
        }

        try {
            callContext.setCurrentOperation(Operation.PRE_DESTROY);
            callContext.setCurrentAllowedStates(null);

            final Method remove = instance.bean instanceof SessionBean ? beanContext.getCreateMethod() : null;

            final List<InterceptorData> callbackInterceptors = beanContext.getCallbackInterceptors();
            final InterceptorStack interceptorStack = new InterceptorStack(instance.bean, remove, Operation.PRE_DESTROY, callbackInterceptors, instance.interceptors);

            //Transaction Demarcation for Singleton PostConstruct method
            TransactionType transactionType;

            if (beanContext.getComponentType() == BeanType.SINGLETON) {
                final Set<Method> callbacks = callbackInterceptors.get(callbackInterceptors.size() - 1).getPreDestroy();
                if (callbacks.isEmpty()) {
                    transactionType = TransactionType.RequiresNew;
                } else {
                    transactionType = beanContext.getTransactionType(callbacks.iterator().next());
                    if (transactionType == TransactionType.Required) {
                        transactionType = TransactionType.RequiresNew;
                    }
                }
            } else {
                transactionType = beanContext.isBeanManagedTransaction() ? TransactionType.BeanManaged : TransactionType.NotSupported;
            }
            final TransactionPolicy transactionPolicy = EjbTransactionUtil.createTransactionPolicy(transactionType, callContext);
            try {
                //Call the chain
                final CdiEjbBean<Object> bean = beanContext.get(CdiEjbBean.class);
                if (bean != null) { // TODO: see if it should be called before or after next call
                    bean.getInjectionTarget().preDestroy(instance.bean);
                }

                interceptorStack.invoke();
                if (instance.creationalContext != null) {
                    instance.creationalContext.release();
                }
            } catch (final Throwable e) {
                //RollBack Transaction
                EjbTransactionUtil.handleSystemException(transactionPolicy, e, callContext);
            } finally {
                EjbTransactionUtil.afterInvoke(transactionPolicy, callContext);
            }

        } catch (final Throwable re) {
            logger.error("Singleton shutdown failed: " + beanContext.getDeploymentID(), re);
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
    public void discardInstance(final ThreadContext callContext, final Object bean) {

    }

    public void deploy(final BeanContext beanContext) throws OpenEJBException {
        final Data data = new Data(beanContext);
        beanContext.setContainerData(data);

        beanContext.set(EJBContext.class, this.sessionContext);

        // Create stats interceptor
        if (StatsInterceptor.isStatsActivated()) {
            final StatsInterceptor stats = new StatsInterceptor(beanContext.getBeanClass());
            beanContext.addFirstSystemInterceptor(stats);

            final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
            jmxName.set("J2EEServer", "openejb");
            jmxName.set("J2EEApplication", null);
            jmxName.set("EJBModule", beanContext.getModuleID());
            jmxName.set("SingletonSessionBean", beanContext.getEjbName());
            jmxName.set("name", beanContext.getEjbName());
            jmxName.set("j2eeType", "Invocations");

            // register the invocation stats interceptor
            final MBeanServer server = LocalMBeanServer.get();
            try {
                final ObjectName objectName = jmxName.build();
                if (server.isRegistered(objectName)) {
                    server.unregisterMBean(objectName);
                }
                server.registerMBean(new ManagedMBean(stats), objectName);
                data.add(objectName);
            } catch (final Exception e) {
                logger.error("Unable to register MBean ", e);
            }
        }

        try {
            final Context context = beanContext.getJndiEnc();
            context.bind("comp/EJBContext", sessionContext);
            context.bind("comp/WebServiceContext", webServiceContext);
            context.bind("comp/TimerService", new TimerServiceWrapper());
        } catch (final NamingException e) {
            throw new OpenEJBException("Failed to bind EJBContext/WebServiceContext/TimerService", e);
        }
    }

    public void undeploy(final BeanContext beanContext) {
        final Data data = (Data) beanContext.getContainerData();
        if (data == null) {
            return;
        }

        final MBeanServer server = LocalMBeanServer.get();
        for (final ObjectName objectName : data.jmxNames) {
            try {
                server.unregisterMBean(objectName);
            } catch (final Exception e) {
                logger.error("Unable to unregister MBean " + objectName);
            }
        }

        beanContext.setContainerData(null);
    }

    private final class Data {
        private final AtomicReference<Future<Instance>> singleton = new AtomicReference<>();
        private final List<ObjectName> jmxNames = new ArrayList<>();
        private final BeanContext info;

        public Data(final BeanContext info) {
            this.info = info;
        }

        public ObjectName add(final ObjectName name) {
            jmxNames.add(name);
            return name;
        }


        public void initialize() throws OpenEJBException {
            SingletonInstanceManager.this.initialize(info);
        }
    }


    private static class BeanManagedLock implements ReadWriteLock {
        private final Lock lock = new Lock() {
            public void lock() {
            }

            public void lockInterruptibly() {
            }

            public Condition newCondition() {
                throw new UnsupportedOperationException("newCondition()");
            }

            public boolean tryLock() {
                return true;
            }

            public boolean tryLock(final long time, final TimeUnit unit) {
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
