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

package org.apache.openejb.core.instance;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.cdi.CdiEjbBean;
import org.apache.openejb.core.InstanceContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.loader.Options;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.DaemonThreadFactory;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Pool;

import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.SessionBean;
import javax.enterprise.context.spi.CreationalContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
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

public abstract class InstanceManager {
    protected static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    protected static final Method removeSessionBeanMethod;

    static { // initialize it only once
        Method foundRemoveMethod;
        try {
            foundRemoveMethod = SessionBean.class.getDeclaredMethod("ejbRemove");
        } catch (final NoSuchMethodException e) {
            foundRemoveMethod = null;
        }
        removeSessionBeanMethod = foundRemoveMethod;
    }

    protected final Duration accessTimeout;
    protected final Duration closeTimeout;
    protected final SecurityService securityService;
    protected final Pool.Builder poolBuilder;
    protected final ThreadPoolExecutor executor;
    protected final ScheduledExecutorService scheduledExecutor;

    public InstanceManager(final SecurityService securityService,
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
                1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(qsize), threadFactory);

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

    protected final class StatelessSupplier implements Pool.Supplier<Instance> {
        private final BeanContext beanContext;

        public StatelessSupplier(final BeanContext beanContext) {
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
    public Instance getInstance(final ThreadContext callContext) throws OpenEJBException {
        final BeanContext beanContext = callContext.getBeanContext();
        final InstanceManagerData data = (InstanceManagerData) beanContext.getContainerData();

        Instance instance = null;
        try {
            final Pool<Instance>.Entry entry = data.poolPop();

            if (entry != null) {
                instance = entry.get();
                instance.setPoolEntry(entry);
            }
        } catch (final TimeoutException e) {
            final String msg = "No instances available in Stateless Session Bean pool.  Waited " + data.getAccessTimeout().toString();
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

        if (bean == null) {
            throw new SystemException("Invalid arguments");
        }

        final Instance instance = Instance.class.cast(bean);
        final BeanContext beanContext = callContext.getBeanContext();
        final InstanceManagerData data = (InstanceManagerData) beanContext.getContainerData();
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
        final InstanceManagerData data = (InstanceManagerData) beanContext.getContainerData();

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

    protected void setDefault(final Duration duration, final TimeUnit unit) {
        if (duration.getUnit() == null) {
            duration.setUnit(unit);
        }
    }

    protected Duration getDuration(final Options options, final String property, final Duration defaultValue, final TimeUnit defaultUnit) {
        final String s = options.get(property, defaultValue.toString());
        final Duration duration = new Duration(s);
        if (duration.getUnit() == null) {
            duration.setUnit(defaultUnit);
        }
        return duration;
    }

    public void undeploy(final BeanContext beanContext) {
        final InstanceManagerData data = (InstanceManagerData) beanContext.getContainerData();
        if (data == null) {
            return;
        }

        final MBeanServer server = LocalMBeanServer.get();
        for (final ObjectName objectName : data.getJmxNames()) {
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


    /**
     * @version $Rev$ $Date$
     */
    public static class Instance {
        public final Object bean;
        public final Map<String, Object> interceptors;
        public final CreationalContext creationalContext;

        private Pool<Instance>.Entry poolEntry;

        public Instance(final Object bean, final Map<String, Object> interceptors, final CreationalContext creationalContext) {
            this.bean = bean;
            this.interceptors = interceptors;
            this.creationalContext = creationalContext;
        }

        public Pool<Instance>.Entry getPoolEntry() {
            return poolEntry;
        }

        public void setPoolEntry(final Pool<Instance>.Entry poolEntry) {
            this.poolEntry = poolEntry;
        }
    }
}
