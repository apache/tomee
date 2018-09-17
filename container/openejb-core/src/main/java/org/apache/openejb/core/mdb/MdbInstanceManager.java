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

package org.apache.openejb.core.mdb;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.cdi.CdiEjbBean;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.InstanceContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.interceptor.InterceptorData;
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

import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.EJBContext;
import javax.ejb.SessionBean;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import static javax.management.MBeanOperationInfo.ACTION;

public class MdbInstanceManager {
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

    private final Duration accessTimeout;
    private final Duration closeTimeout;
    private final Pool.Builder poolBuilder;
    private final ThreadPoolExecutor executor;
    private final ScheduledExecutorService scheduledExecutor;

    private final Map<BeanContext, MdbPoolContainer.MdbActivationContext> activationContexts = new ConcurrentHashMap<>();
    private final Map<BeanContext, ObjectName> mbeanNames = new ConcurrentHashMap<>();
    protected final List<ObjectName> jmxNames = new ArrayList<ObjectName>();
    private final ResourceAdapter resourceAdapter;
    private final InboundRecovery inboundRecovery;
    private final Object containerID;
    private final SecurityService securityService;

    public MdbInstanceManager(final SecurityService securityService,
                              final ResourceAdapter resourceAdapter,
                              final InboundRecovery inboundRecovery,
                              final Object containerID,
                              final Duration accessTimeout, final Duration closeTimeout,
                              final Pool.Builder poolBuilder, final int callbackThreads,
                              final ScheduledExecutorService ses) {
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
        final ThreadFactory threadFactory = new DaemonThreadFactory("InstanceManagerPool.worker.");
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

        this.securityService = securityService;
        this.resourceAdapter = resourceAdapter;
        this.inboundRecovery = inboundRecovery;
        this.containerID = containerID;
    }


    public void deploy(final BeanContext beanContext, final ActivationSpec activationSpec, final EndpointFactory endpointFactory)
            throws OpenEJBException {
        if (inboundRecovery != null) {
            inboundRecovery.recover(resourceAdapter, activationSpec, containerID.toString());
        }

        final ObjectRecipe recipe = PassthroughFactory.recipe(new Pool.Builder(poolBuilder));
        recipe.allow(Option.CASE_INSENSITIVE_FACTORY);
        recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        recipe.setAllProperties(beanContext.getProperties());

        final Pool.Builder builder = (Pool.Builder) recipe.create();
        setDefault(builder.getMaxAge(), TimeUnit.HOURS);
        setDefault(builder.getIdleTimeout(), TimeUnit.MINUTES);
        setDefault(builder.getInterval(), TimeUnit.MINUTES);

        final InstanceSupplier supplier = new InstanceSupplier(beanContext);
        builder.setSupplier(supplier);
        builder.setExecutor(executor);
        builder.setScheduledExecutor(scheduledExecutor);

        final int min = builder.getMin();
        final long maxAge = builder.getMaxAge().getTime(TimeUnit.MILLISECONDS);
        final double maxAgeOffset = builder.getMaxAgeOffset();

        final Data data = new Data(builder.build(), accessTimeout, closeTimeout);

        MdbContext mdbContext = new MdbContext(securityService, new Flushable() {
            @Override
            public void flush() throws IOException {
                data.flush();
            }
        });

        try {
            final Context context = beanContext.getJndiEnc();
            context.bind("comp/EJBContext", mdbContext);
            context.bind("comp/TimerService", new TimerServiceWrapper());
        } catch (final NamingException e) {
            throw new OpenEJBException("Failed to bind EJBContext/TimerService", e);
        }

        beanContext.set(EJBContext.class, mdbContext);
        data.setBaseContext(mdbContext);
        beanContext.setContainerData(data);
        final MBeanServer server = LocalMBeanServer.get();

        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "openejb");
        jmxName.set("J2EEApplication", null);
        jmxName.set("EJBModule", beanContext.getModuleID());
        jmxName.set("MessageDrivenBean", beanContext.getEjbName());
        jmxName.set("j2eeType", "");
        jmxName.set("name", beanContext.getEjbName());

        // Create stats interceptor
        if (StatsInterceptor.isStatsActivated()) {
            final StatsInterceptor stats = new StatsInterceptor(beanContext.getBeanClass());
            beanContext.addFirstSystemInterceptor(stats);

            // register the invocation stats interceptor
            try {
                final ObjectName objectName = jmxName.set("j2eeType", "Invocations").build();
                if (server.isRegistered(objectName)) {
                    server.unregisterMBean(objectName);
                }
                server.registerMBean(new ManagedMBean(stats), objectName);
                jmxNames.add(objectName);
            } catch (final Exception e) {
                logger.error("Unable to register MBean ", e);
            }
        }

        // activate the endpoint
        try {

            final MdbPoolContainer.MdbActivationContext activationContext = new MdbPoolContainer.MdbActivationContext(Thread.currentThread().getContextClassLoader(), beanContext, resourceAdapter, endpointFactory, activationSpec);
            activationContexts.put(beanContext, activationContext);

            boolean activeOnStartup = true;
            String activeOnStartupSetting = beanContext.getActivationProperties().get("MdbActiveOnStartup");

            if (activeOnStartupSetting == null) {
                activeOnStartupSetting = beanContext.getActivationProperties().get("DeliveryActive");
            }

            if (activeOnStartupSetting != null) {
                activeOnStartup = Boolean.parseBoolean(activeOnStartupSetting);
            }

            if (activeOnStartup) {
                activationContext.start();
            } else {
                logger.info("Not auto-activating endpoint for " + beanContext.getDeploymentID());
            }

            String jmxControlName = beanContext.getActivationProperties().get("MdbJMXControl");
            if (jmxControlName == null) {
                jmxControlName = "true";
            }

            addJMxControl(beanContext, jmxControlName, activationContext);

        } catch (final ResourceException e) {
            throw new OpenEJBException(e);
        }

        final Options options = new Options(beanContext.getProperties());
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
                logger.error("can't fill the message driven bean pool", e);
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

        data.getPool().start();
    }

    public void undeploy(final BeanContext beanContext) {
        final MdbPoolContainer.MdbActivationContext actContext = activationContexts.get(beanContext);
        if (actContext == null) {
            return;
        }
        final EndpointFactory endpointFactory = actContext.getEndpointFactory();
        if (endpointFactory != null) {

            final ObjectName jmxBeanToRemove = mbeanNames.remove(beanContext);
            if (jmxBeanToRemove != null) {
                LocalMBeanServer.unregisterSilently(jmxBeanToRemove);
                logger.info("Undeployed MDB control for " + beanContext.getDeploymentID());
            }

            final MdbPoolContainer.MdbActivationContext activationContext = activationContexts.remove(beanContext);
            if (activationContext != null && activationContext.isStarted()) {
                resourceAdapter.endpointDeactivation(endpointFactory, endpointFactory.getActivationSpec());
            }

            final MBeanServer server = LocalMBeanServer.get();
            for (final ObjectName objectName : jmxNames) {
                try {
                    server.unregisterMBean(objectName);
                } catch (final InstanceNotFoundException e) {
                    // ignore it as the object name is gone already
                } catch (final Exception e) {
                    logger.error("Unable to unregister MBean " + objectName, e);
                }
            }
        }
    }

    private void addJMxControl(final BeanContext current, final String name, final MdbPoolContainer.MdbActivationContext activationContext) throws ResourceException {
        if (name == null || "false".equalsIgnoreCase(name)) {
            logger.debug("Not adding JMX control for " + current.getDeploymentID());
            return;
        }

        final ObjectName jmxName;
        try {
            jmxName = "true".equalsIgnoreCase(name) ? new ObjectNameBuilder()
                    .set("J2EEServer", "openejb")
                    .set("J2EEApplication", null)
                    .set("EJBModule", current.getModuleID())
                    .set("StatelessSessionBean", current.getEjbName())
                    .set("j2eeType", "control")
                    .set("name", current.getEjbName())
                    .build() : new ObjectName(name);
        } catch (final MalformedObjectNameException e) {
            throw new IllegalArgumentException(e);
        }
        mbeanNames.put(current, jmxName);

        LocalMBeanServer.registerSilently(new MdbJmxControl(activationContext), jmxName);
        logger.info("Deployed MDB control for " + current.getDeploymentID() + " on " + jmxName);
    }

    public static final class MdbJmxControl implements DynamicMBean {
        private static final AttributeList ATTRIBUTE_LIST = new AttributeList();
        private static final MBeanInfo INFO = new MBeanInfo(
                "org.apache.openejb.resource.activemq.ActiveMQResourceAdapter.MdbJmxControl",
                "Allows to control a MDB (start/stop)",
                new MBeanAttributeInfo[]{
                        new MBeanAttributeInfo("started", "boolean", "started: boolean indicating whether this MDB endpoint has been activated.", true, false, true)
                },
                new MBeanConstructorInfo[0],
                new MBeanOperationInfo[]{
                        new MBeanOperationInfo("start", "Ensure the listener is active.", new MBeanParameterInfo[0], "void", ACTION),
                        new MBeanOperationInfo("stop", "Ensure the listener is not active.", new MBeanParameterInfo[0], "void", ACTION)
                },
                new MBeanNotificationInfo[0]);

        private final MdbPoolContainer.MdbActivationContext activationContext;

        private MdbJmxControl(final MdbPoolContainer.MdbActivationContext activationContext) {
            this.activationContext = activationContext;
        }

        @Override
        public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
            if (actionName.equals("stop")) {
                activationContext.stop();

            } else if (actionName.equals("start")) {
                try {
                    activationContext.start();
                } catch (ResourceException e) {
                    logger.error("Error invoking " + actionName + ": " + e.getMessage());
                    throw new MBeanException(new IllegalStateException(e.getMessage(), e));
                }

            } else {
                throw new MBeanException(new IllegalStateException("unsupported operation: " + actionName));
            }
            return null;
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            return INFO;
        }

        @Override
        public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
            if ("started".equals(attribute)) {
                return activationContext.isStarted();
            }

            throw new AttributeNotFoundException();
        }

        @Override
        public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
            throw new AttributeNotFoundException();
        }

        @Override
        public AttributeList getAttributes(final String[] attributes) {
            return ATTRIBUTE_LIST;
        }

        @Override
        public AttributeList setAttributes(final AttributeList attributes) {
            return ATTRIBUTE_LIST;
        }
    }

    private final class InstanceSupplier implements Pool.Supplier<Instance> {
        private final BeanContext beanContext;

        public InstanceSupplier(final BeanContext beanContext) {
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
                return createInstance(ctx.getBeanContext());
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
                Thread.currentThread().interrupt();
            }
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(10000, MILLISECONDS)) {
                    java.util.logging.Logger.getLogger(this.getClass().getName()).log(Level.WARNING, getClass().getSimpleName() + " pool  timeout expired");
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
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
     * new bean instance performing all required injection
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
            final String msg = "No instances available in Message Driven Bean pool.  Waited " + data.getAccessTimeout().toString();
            final ConcurrentAccessTimeoutException timeoutException = new ConcurrentAccessTimeoutException(msg);
            timeoutException.fillInStackTrace();
            throw new ApplicationException(timeoutException);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenEJBException("Unexpected Interruption of current thread: ", e);
        }

        if (null == instance) {
            instance = createInstance(beanContext);
        }

        return instance;
    }

    private Instance createInstance(final BeanContext beanContext) throws ApplicationException {
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
     * returned by the Container via this method under two circumstances.
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

    private void setDefault(final Duration duration, final TimeUnit unit) {
        if (duration.getUnit() == null) {
            duration.setUnit(unit);
        }
    }

    private final class InstanceCreatorRunnable implements Runnable {

        private final Data data;
        private final InstanceSupplier supplier;
        private final long offset;

        public InstanceCreatorRunnable(final long maxAge, final long iteration, final long min, final double maxAgeOffset,
                                       final Data data, final InstanceSupplier supplier) {
            this.data = data;
            this.supplier = supplier;
            this.offset = maxAge > 0 ? (long) (maxAge / maxAgeOffset * min * iteration) % maxAge : 0l;
        }

        @Override
        public void run() {
            final Instance obj = supplier.create();
            if (obj != null) {
                data.getPool().add(obj, offset);
            }
        }
    }

    private class Data {

        private final Pool<Instance> pool;
        private final Duration accessTimeout;
        private final Duration closeTimeout;
        private final List<ObjectName> jmxNames = new ArrayList<ObjectName>();
        private BaseContext baseContext;

        public Data(final Pool<Instance> pool, final Duration accessTimeout, final Duration closeTimeout) {
            this.pool = pool;
            this.accessTimeout = accessTimeout;
            this.closeTimeout = closeTimeout;
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

        public void flush() {
            this.pool.flush();
        }

        public boolean closePool() throws InterruptedException {
            return pool.close(closeTimeout.getTime(), closeTimeout.getUnit());
        }

        public ObjectName add(final ObjectName name) {
            jmxNames.add(name);
            return name;
        }

        public List<ObjectName> getJmxNames() {
            return jmxNames;
        }

        public BaseContext getBaseContext() {
            return baseContext;
        }

        public void setBaseContext(BaseContext baseContext) {
            this.baseContext = baseContext;
        }
    }
}
