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
import org.apache.openejb.cdi.CdiEjbBean;
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
import org.apache.openejb.util.DaemonThreadFactory;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.PassthroughFactory;
import org.apache.openejb.util.Pool;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import jakarta.ejb.ConcurrentAccessTimeoutException;
import jakarta.ejb.EJBContext;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class StatelessInstanceManager {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    private static final Method removeSessionBeanMethod;

    static { // initialize it only once
        Method foundRemoveMethod;
        try {
            foundRemoveMethod = SessionBean.class.getDeclaredMethod("ejbRemove");
        } catch (final NoSuchMethodException e) {
            foundRemoveMethod = null;
        }
        removeSessionBeanMethod = foundRemoveMethod;
    }

    private final Duration accessTimeout;
    private final Duration closeTimeout;
    private final SecurityService securityService;
    private final Pool.Builder poolBuilder;
    private final ThreadPoolExecutor executor;
    private final ScheduledExecutorService scheduledExecutor;

    public StatelessInstanceManager(final SecurityService securityService,
                                    final Duration accessTimeout, final Duration closeTimeout,
                                    final Pool.Builder poolBuilder, final int callbackThreads,
                                    final ScheduledExecutorService ses) {
        this.securityService = securityService;
        this.accessTimeout = accessTimeout;
        this.closeTimeout = closeTimeout;
        this.poolBuilder = poolBuilder;
        this.scheduledExecutor = ses;
        if (ScheduledThreadPoolExecutor.class.isInstance(ses) && !ScheduledThreadPoolExecutor.class.cast(ses).getRemoveOnCancelPolicy()) {
            ScheduledThreadPoolExecutor.class.cast(ses).setRemoveOnCancelPolicy(true);
        }

        if (accessTimeout.getUnit() == null) {
            accessTimeout.setUnit(TimeUnit.MILLISECONDS);
        }

        final int qsize = callbackThreads > 1 ? callbackThreads - 1 : 1;
        final ThreadFactory threadFactory = new DaemonThreadFactory("StatelessPool.worker.");
        this.executor = new ThreadPoolExecutor(
            callbackThreads, callbackThreads * 2,
            1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>(qsize), threadFactory);

        this.executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor tpe) {

                if (null == r || null == tpe || tpe.isShutdown() || tpe.isTerminated() || tpe.isTerminating()) {
                    return;
                }

                try {
                    if (!tpe.getQueue().offer(r, 20, TimeUnit.SECONDS)) {
                        logger.warning("Executor failed to run asynchronous process: " + r);
                    }
                } catch (final InterruptedException e) {
                    //Ignore
                }
            }
        });
    }

    private final class StatelessSupplier implements Pool.Supplier<Instance> {
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
                return createInstance(ctx, ctx.getBeanContext());
            } catch (final OpenEJBException e) {
                logger.error("Unable to fill pool: for deployment '" + beanContext.getDeploymentID() + "'", e);
            } finally {
                ThreadContext.exit(oldCallContext);
            }
            return null;
        }
    }

    public void destroy() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10000, MILLISECONDS)) {
                    java.util.logging.Logger.getLogger(this.getClass().getName()).log(Level.WARNING, getClass().getSimpleName() + " pool  timeout expired");
                }
            } catch (final InterruptedException e) {
                Thread.interrupted();
            }
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(10000, MILLISECONDS)) {
                    java.util.logging.Logger.getLogger(this.getClass().getName()).log(Level.WARNING, getClass().getSimpleName() + " pool  timeout expired");
                }
            } catch (final InterruptedException e) {
                Thread.interrupted();
            }
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
     * @param callContext ThreadContext
     * @return Object
     * @throws OpenEJBException
     */
    public Instance getInstance(final ThreadContext callContext) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();
        final Data data = (Data) beanContext.getContainerData();

        Instance instance = null;
        try {
            final Pool<Instance>.Entry entry = data.poolPop();

            if (entry != null) {
                instance = entry.get();
                instance.setPoolEntry(entry);
            }
        } catch (final TimeoutException e) {
            final String msg = "No instances available in Stateless Session Bean pool.  Waited " + data.accessTimeout.toString();
            final ConcurrentAccessTimeoutException timeoutException = new ConcurrentAccessTimeoutException(msg);
            timeoutException.fillInStackTrace();
            throw new ApplicationException(timeoutException);
        } catch (final InterruptedException e) {
            Thread.interrupted();
            throw new OpenEJBException("Unexpected Interruption of current thread: ", e);
        }

        if (null == instance) {
            instance = createInstance(callContext, beanContext);
        }

            return instance;
        }

    private Instance createInstance(final ThreadContext callContext, final BeanContext beanContext) throws ApplicationException {
        try {
            final InstanceContext context = beanContext.newInstance();
            return new Instance(context.getBean(), context.getInterceptors(), context.getCreationalContext());

        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
            }
            final String t = "The bean instance " + beanContext.getDeploymentID() + " threw a system exception:" + e;
            logger.error(t, e);
            throw new ApplicationException(new RemoteException("Cannot obtain a free instance.", e));
        }
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
     * @param callContext ThreadContext
     * @param bean        Object
     * @throws OpenEJBException
     */
    public void poolInstance(final ThreadContext callContext, final Object bean) throws OpenEJBException {

        if (bean == null) {
            throw new SystemException("Invalid arguments");
        }

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

        if (bean == null) {
            throw new SystemException("Invalid arguments");
        }

        final Instance instance = Instance.class.cast(bean);
        final BeanContext beanContext = callContext.getBeanContext();
        final Data data = (Data) beanContext.getContainerData();

        if (null != data) {
            final Pool<Instance> pool = data.getPool();
            pool.discard(instance.getPoolEntry());
        }
    }

    @SuppressWarnings("unchecked")
    private void freeInstance(final ThreadContext callContext, final Instance instance) {
        try {
            callContext.setCurrentOperation(Operation.PRE_DESTROY);
            final BeanContext beanContext = callContext.getBeanContext();

            final Method remove = instance.bean instanceof SessionBean ? removeSessionBeanMethod : null;

            final List<InterceptorData> callbackInterceptors = beanContext.getCallbackInterceptors();
            final InterceptorStack interceptorStack = new InterceptorStack(instance.bean, remove, Operation.PRE_DESTROY, callbackInterceptors, instance.interceptors);

            final CdiEjbBean<Object> bean = beanContext.get(CdiEjbBean.class);
            if (bean != null) { // TODO: see if it should be called before or after next call
                bean.getInjectionTarget().preDestroy(instance.bean);
            }
            interceptorStack.invoke();

            if (instance.creationalContext != null) {
                instance.creationalContext.release();
            }
        } catch (final Throwable re) {
            logger.error("The bean instance " + instance + " threw a system exception:" + re, re);
        }

    }

    @SuppressWarnings("unchecked")
    public void deploy(final BeanContext beanContext) throws OpenEJBException {
        final Options options = new Options(beanContext.getProperties());

        final Duration accessTimeout = getDuration(
            options,
            "AccessTimeout",
            getDuration(options, "Timeout", this.accessTimeout, TimeUnit.MILLISECONDS), // default timeout
            TimeUnit.MILLISECONDS
        );
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
        builder.setScheduledExecutor(scheduledExecutor);

        final Data data = new Data(builder.build(), accessTimeout, closeTimeout);
        beanContext.setContainerData(data);

        beanContext.set(EJBContext.class, data.sessionContext);

        try {
            final Context context = beanContext.getJndiEnc();
            context.bind("comp/EJBContext", data.sessionContext);
            context.bind("comp/WebServiceContext", new EjbWsContext(data.sessionContext));
            context.bind("comp/TimerService", new TimerServiceWrapper());
        } catch (final NamingException e) {
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
            } catch (final Exception e) {
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
        } catch (final Exception e) {
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
            } catch (final InterruptedException e) {
                logger.error("can't fill the stateless pool", e);
            }
        }

        data.getPool().start();
    }

    private void setDefault(final Duration duration, final TimeUnit unit) {
        if (duration.getUnit() == null) {
            duration.setUnit(unit);
        }
    }

    private Duration getDuration(final Options options, final String property, final Duration defaultValue, final TimeUnit defaultUnit) {
        final String s = options.get(property, defaultValue.toString());
        final Duration duration = new Duration(s);
        if (duration.getUnit() == null) {
            duration.setUnit(defaultUnit);
        }
        return duration;
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

        try {
            if (!data.closePool()) {
                logger.error("Timed-out waiting for stateless pool to close: for deployment '" + beanContext.getDeploymentID() + "'");
            }

        } catch (final InterruptedException e) {
            Thread.interrupted();
        }

        beanContext.setContainerData(null);
    }

    private final class Data {
        private final Pool<Instance> pool;
        private final Duration accessTimeout;
        private final Duration closeTimeout;
        private final List<ObjectName> jmxNames = new ArrayList<>();
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

    private final class InstanceCreatorRunnable implements Runnable {
        private final long maxAge;
        private final long iteration;
        private final double maxAgeOffset;
        private final long min;
        private final Data data;
        private final StatelessSupplier supplier;

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
                final long offset = maxAge > 0 ? (long) (maxAge / maxAgeOffset * min * iteration) % maxAge : 0L;
                data.getPool().add(obj, offset);
            }
        }
    }
}
