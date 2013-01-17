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
package org.apache.openejb.core.stateless;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.InstanceContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorInstance;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.timer.TimerServiceWrapper;
import org.apache.openejb.loader.Options;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ManagedMBean;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.monitoring.StatsInterceptor;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.*;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.EJBContext;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StatelessInstanceManager {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    private static final Method removeSessionBeanMethod;

    static { // initialize it only once
        Method foundRemoveMethod;
        try {
            foundRemoveMethod = SessionBean.class.getDeclaredMethod("ejbRemove");
        } catch (NoSuchMethodException e) {
            foundRemoveMethod = null;
        }
        removeSessionBeanMethod = foundRemoveMethod;
    }

    protected Duration accessTimeout;
    protected Duration closeTimeout;
    protected int beanCount = 0;

    protected final SafeToolkit toolkit = SafeToolkit.getToolkit("StatefulInstanceManager");
    private SecurityService securityService;
    private final Pool.Builder poolBuilder;
    private final ThreadPoolExecutor executor;

    public StatelessInstanceManager(final SecurityService securityService, final Duration accessTimeout, final Duration closeTimeout, final Pool.Builder poolBuilder, final int callbackThreads) {
        this.securityService = securityService;
        this.accessTimeout = accessTimeout;
        this.closeTimeout = closeTimeout;
        this.poolBuilder = poolBuilder;

        if (accessTimeout.getUnit() == null) {
            accessTimeout.setUnit(TimeUnit.MILLISECONDS);
        }

        final int qsize = (callbackThreads > 1 ? callbackThreads - 1 : 1);

        executor = new ThreadPoolExecutor(callbackThreads, callbackThreads * 2,
                1L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>(qsize), new ThreadFactory() {

            private final AtomicInteger i = new AtomicInteger(1);

            @Override
            public Thread newThread(final Runnable runable) {
                final Thread t = new Thread(runable, "StatelessPool.worker." + i.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });

        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor tpe) {

                if (null == r || null == tpe || tpe.isShutdown() || tpe.isTerminated() || tpe.isTerminating()) {
                    return;
                }

                try {
                    if (!tpe.getQueue().offer(r, 20, TimeUnit.SECONDS)) {
                        logger.warning("Executor failed to run asynchronous process: " + r);
                    }
                } catch (InterruptedException e) {
                    //Ignore
                }
            }
        });
    }

    private class StatelessSupplier implements Pool.Supplier<Instance> {
        private final BeanContext beanContext;

        private StatelessSupplier(final BeanContext beanContext) {
            this.beanContext = beanContext;
        }

        @Override
        public void discard(final Instance instance, final Pool.Event reason) {
            final ThreadContext ctx = new ThreadContext(beanContext, null);
            final ThreadContext oldCallContext = ThreadContext.enter(ctx);
            try {
                freeInstance(ctx, instance);
            } finally {
                ThreadContext.exit(oldCallContext);
            }
        }

        @Override
        public Instance create() {
            final ThreadContext ctx = new ThreadContext(beanContext, null);
            final ThreadContext oldCallContext = ThreadContext.enter(ctx);
            try {
                return ceateInstance(ctx, ctx.getBeanContext());
            } catch (OpenEJBException e) {
                logger.error("Unable to fill pool: for deployment '" + beanContext.getDeploymentID() + "'", e);
            } finally {
                ThreadContext.exit(oldCallContext);
            }

            return null;
        }
    }

    /**
     * Removes an instance from the pool and returns it for use
     * by the container in business methods.
     * <p/>
     * If the pool is at it's limit the StrictPooling flag will
     * cause this thread to wait.
     * <p/>
     * If StrictPooling is not enabled this method will create a
     * new stateless bean instance performing all required injection
     * and callbacks before returning it in a method ready state.
     *
     * @param callContext ThreadContext
     * @return Object
     * @throws OpenEJBException
     */
    public Object getInstance(final ThreadContext callContext) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();
        final Data data = (Data) beanContext.getContainerData();

        Instance instance = null;
        try {
            final Pool<Instance>.Entry entry = data.poolPop();

            if (entry != null) {
                instance = entry.get();
                instance.setPoolEntry(entry);
            }
        } catch (TimeoutException e) {
            final ConcurrentAccessTimeoutException timeoutException = new ConcurrentAccessTimeoutException("No instances available in Stateless Session Bean pool.  Waited " + data.accessTimeout.toString());
            timeoutException.fillInStackTrace();

            throw new ApplicationException(timeoutException);
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new OpenEJBException("Unexpected Interruption of current thread: ", e);
        }

        if (instance != null) return instance;

        return ceateInstance(callContext, beanContext);
    }

    private Instance ceateInstance(final ThreadContext callContext, final BeanContext beanContext) throws org.apache.openejb.ApplicationException {

        try {

            final InstanceContext context = beanContext.newInstance();

            if (context.getBean() instanceof SessionBean) {

                final Operation originalOperation = callContext.getCurrentOperation();
                try {
                    callContext.setCurrentOperation(Operation.CREATE);
                    final Method create = beanContext.getCreateMethod();
                    final InterceptorStack ejbCreate = new InterceptorStack(context.getBean(), create, Operation.CREATE, new ArrayList<InterceptorData>(), new HashMap<String, Object>());
                    ejbCreate.invoke();
                } finally {
                    callContext.setCurrentOperation(originalOperation);
                }
            }

            return new Instance(context.getBean(), context.getInterceptors(), context.getCreationalContext());
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }
            final String t = "The bean instance " + beanContext.getDeploymentID() + " threw a system exception:" + e;
            logger.error(t, e);
            throw new org.apache.openejb.ApplicationException(new RemoteException("Cannot obtain a free instance.", e));
        }
    }

    /**
     * All instances are removed from the pool in getInstance(...).  They are only
     * returned by the StatelessContainer via this method under two circumstances.
     * <p/>
     * 1.  The business method returns normally
     * 2.  The business method throws an application exception
     * <p/>
     * Instances are not returned to the pool if the business method threw a system
     * exception.
     *
     * @param callContext ThreadContext
     * @param bean        Object
     * @throws OpenEJBException
     */
    public void poolInstance(final ThreadContext callContext, final Object bean) throws OpenEJBException {
        if (bean == null) throw new SystemException("Invalid arguments");
        final Instance instance = Instance.class.cast(bean);

        final BeanContext beanContext = callContext.getBeanContext();
        final Data data = (Data) beanContext.getContainerData();

        final Pool<Instance> pool = data.getPool();

        if (instance.getPoolEntry() != null) {
            pool.push(instance.getPoolEntry());
        } else {
            pool.push(instance);
        }
    }

    /**
     * This method is called to release the semaphore in case of the business method
     * throwing a system exception
     *
     * @param callContext ThreadContext
     * @param bean        Object
     */
    public void discardInstance(final ThreadContext callContext, final Object bean) throws SystemException {
        if (bean == null) throw new SystemException("Invalid arguments");
        final Instance instance = Instance.class.cast(bean);

        final BeanContext beanContext = callContext.getBeanContext();
        final Data data = (Data) beanContext.getContainerData();

        if (null != data) {
            final Pool<Instance> pool = data.getPool();
            pool.discard(instance.getPoolEntry());
        }
    }

    private void freeInstance(final ThreadContext callContext, final Instance instance) {
        try {
            callContext.setCurrentOperation(Operation.PRE_DESTROY);
            final BeanContext beanContext = callContext.getBeanContext();

            final Method remove = instance.bean instanceof SessionBean ? removeSessionBeanMethod : null;

            final List<InterceptorData> callbackInterceptors = beanContext.getCallbackInterceptors();
            final InterceptorStack interceptorStack = new InterceptorStack(instance.bean, remove, Operation.PRE_DESTROY, callbackInterceptors, instance.interceptors);

            interceptorStack.invoke();

            if (instance.creationalContext != null) {
                instance.creationalContext.release();
            }
        } catch (Throwable re) {
            logger.error("The bean instance " + instance + " threw a system exception:" + re, re);
        }

    }

    @SuppressWarnings("unchecked")
    public void deploy(final BeanContext beanContext) throws OpenEJBException {
        final Options options = new Options(beanContext.getProperties());

        Duration accessTimeout = getDuration(options, "Timeout", this.accessTimeout, TimeUnit.MILLISECONDS);
        accessTimeout = getDuration(options, "AccessTimeout", accessTimeout, TimeUnit.MILLISECONDS);
        final Duration closeTimeout = getDuration(options, "CloseTimeout", this.closeTimeout, TimeUnit.MINUTES);

        final ObjectRecipe recipe = PassthroughFactory.recipe(new Pool.Builder(poolBuilder));
        recipe.allow(Option.CASE_INSENSITIVE_FACTORY);
        recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        recipe.setAllProperties(beanContext.getProperties());
        final Pool.Builder builder = (Pool.Builder) recipe.create();

        setDefault(builder.getMaxAge(), TimeUnit.HOURS);
        setDefault(builder.getIdleTimeout(), TimeUnit.MINUTES);
        setDefault(builder.getInterval(), TimeUnit.MINUTES);

        final StatelessSupplier supplier = new StatelessSupplier(beanContext);
        builder.setSupplier(supplier);
        builder.setExecutor(executor);

        final Data data = new Data(builder.build(), accessTimeout, closeTimeout);
        beanContext.setContainerData(data);

        beanContext.set(EJBContext.class, data.sessionContext);

        try {
            final Context context = beanContext.getJndiEnc();
            context.bind("comp/EJBContext", data.sessionContext);
            context.bind("comp/WebServiceContext", new EjbWsContext(data.sessionContext));
            context.bind("comp/TimerService", new TimerServiceWrapper());
        } catch (NamingException e) {
            throw new OpenEJBException("Failed to bind EJBContext/WebServiceContext/TimerService", e);
        }

        final int min = builder.getMin();
        final long maxAge = builder.getMaxAge().getTime(TimeUnit.MILLISECONDS);
        final double maxAgeOffset = builder.getMaxAgeOffset();

        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "openejb");
        jmxName.set("J2EEApplication", null);
        jmxName.set("EJBModule", beanContext.getModuleID());
        jmxName.set("StatelessSessionBean", beanContext.getEjbName());
        jmxName.set("name", beanContext.getEjbName());

        final MBeanServer server = LocalMBeanServer.get();

        // Create stats interceptor
        if (StatsInterceptor.isStatsActivated()) {

            StatsInterceptor stats = null;
            for (final InterceptorInstance interceptor : beanContext.getUserAndSystemInterceptors()) {
                if (interceptor.getInterceptor() instanceof StatsInterceptor) {
                    stats = (StatsInterceptor) interceptor.getInterceptor();
                }
            }
            if (stats == null) { // normally useless
                stats = new StatsInterceptor(beanContext.getBeanClass());
                beanContext.addFirstSystemInterceptor(stats);
            }

            // register the invocation stats interceptor
            try {
                final ObjectName objectName = jmxName.set("j2eeType", "Invocations").build();
                if (server.isRegistered(objectName)) {
                    server.unregisterMBean(objectName);
                }
                server.registerMBean(new ManagedMBean(stats), objectName);
                data.add(objectName);
            } catch (Exception e) {
                logger.error("Unable to register MBean ", e);
            }
        }

        // register the pool
        try {
            final ObjectName objectName = jmxName.set("j2eeType", "Pool").build();
            if (server.isRegistered(objectName)) {
                server.unregisterMBean(objectName);
            }
            server.registerMBean(new ManagedMBean(data.pool), objectName);
            data.add(objectName);
        } catch (Exception e) {
            logger.error("Unable to register MBean ", e);
        }

        // Finally, fill the pool and start it
        if (!options.get("BackgroundStartup", false) && min > 0) {
            final ExecutorService es = Executors.newFixedThreadPool(min);
            for (int i = 0; i < min; i++) {
                es.submit(new InstanceCreatorRunnable(maxAge, i, min, maxAgeOffset, data, supplier));
            }
            es.shutdown();
            try {
                es.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                logger.error("can't fill the stateless pool", e);
            }
        }

        data.getPool().start();
    }

    private void setDefault(final Duration duration, final TimeUnit unit) {
        if (duration.getUnit() == null) duration.setUnit(unit);
    }

    private Duration getDuration(final Options options, final String property, final Duration defaultValue, final TimeUnit defaultUnit) {
        final String s = options.get(property, defaultValue.toString());
        final Duration duration = new Duration(s);
        if (duration.getUnit() == null) duration.setUnit(defaultUnit);
        return duration;
    }

    public void undeploy(final BeanContext beanContext) {
        final Data data = (Data) beanContext.getContainerData();
        if (data == null) return;

        final MBeanServer server = LocalMBeanServer.get();
        for (final ObjectName objectName : data.jmxNames) {
            try {
                server.unregisterMBean(objectName);
            } catch (Exception e) {
                logger.error("Unable to unregister MBean " + objectName);
            }
        }

        try {
            if (!data.closePool()) {
                logger.error("Timed-out waiting for stateless pool to close: for deployment '" + beanContext.getDeploymentID() + "'");
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        beanContext.setContainerData(null);
    }

    private final class Data {
        private final Pool<Instance> pool;
        private final Duration accessTimeout;
        private final Duration closeTimeout;
        private final List<ObjectName> jmxNames = new ArrayList<ObjectName>();
        private final SessionContext sessionContext;

        private Data(final Pool<Instance> pool, final Duration accessTimeout, final Duration closeTimeout) {
            this.pool = pool;
            this.accessTimeout = accessTimeout;
            this.closeTimeout = closeTimeout;
            this.sessionContext = new StatelessContext(securityService, new Flushable() {
                @Override
                public void flush() throws IOException {
                    getPool().flush();
                }
            });
        }

        public Duration getAccessTimeout() {
            return accessTimeout;
        }

        public Pool<Instance>.Entry poolPop() throws InterruptedException, TimeoutException {
            return pool.pop(accessTimeout.getTime(), accessTimeout.getUnit());
        }

        public Pool<Instance> getPool() {
            return pool;
        }

        public boolean closePool() throws InterruptedException {
            return pool.close(closeTimeout.getTime(), closeTimeout.getUnit());
        }

        public ObjectName add(final ObjectName name) {
            jmxNames.add(name);
            return name;
        }
    }

    private class InstanceCreatorRunnable implements Runnable {
        private long maxAge;
        private long iteration;
        private double maxAgeOffset;
        private long min;
        private Data data;
        private StatelessSupplier supplier;

        private InstanceCreatorRunnable(final long maxAge, final long iteration, final long min, final double maxAgeOffset, final Data data, final StatelessSupplier supplier) {
            this.maxAge = maxAge;
            this.iteration = iteration;
            this.min = min;
            this.maxAgeOffset = maxAgeOffset;
            this.data = data;
            this.supplier = supplier;
        }

        @Override
        public void run() {
            final Instance obj = supplier.create();
            if (obj != null) {
                final long offset = maxAge > 0 ? (long) (maxAge / maxAgeOffset * min * iteration) % maxAge : 0l;
                data.getPool().add(obj, offset);
            }
        }
    }
}
