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

package org.apache.openejb.core.timer;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.timer.quartz.PatchedStdJDBCDelegate;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.quartz.JobBuilder;
import org.apache.openejb.quartz.JobDataMap;
import org.apache.openejb.quartz.JobDetail;
import org.apache.openejb.quartz.Scheduler;
import org.apache.openejb.quartz.SchedulerException;
import org.apache.openejb.quartz.Trigger;
import org.apache.openejb.quartz.TriggerKey;
import org.apache.openejb.quartz.impl.StdSchedulerFactory;
import org.apache.openejb.quartz.impl.jdbcjobstore.JobStoreSupport;
import org.apache.openejb.quartz.impl.jdbcjobstore.StdJDBCDelegate;
import org.apache.openejb.quartz.impl.triggers.AbstractTrigger;
import org.apache.openejb.quartz.listeners.SchedulerListenerSupport;
import org.apache.openejb.quartz.simpl.RAMJobStore;
import org.apache.openejb.resource.quartz.QuartzResourceAdapter;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.SetAccessible;

import jakarta.ejb.EJBContext;
import jakarta.ejb.EJBException;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class EjbTimerServiceImpl implements EjbTimerService, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getInstance(LogCategory.TIMER, "org.apache.openejb.util.resources");

    public static final String QUARTZ_JMX = "org.apache.openejb.quartz.scheduler.jmx.export";
    public static final String QUARTZ_MAKE_SCHEDULER_THREAD_DAEMON = "org.apache.openejb.quartz.scheduler.makeSchedulerThreadDaemon";

    public static final String OPENEJB_TIMEOUT_JOB_NAME = "OPENEJB_TIMEOUT_JOB";
    public static final String OPENEJB_TIMEOUT_JOB_GROUP_NAME = "OPENEJB_TIMEOUT_GROUP";

    public static final String EJB_TIMER_RETRY_ATTEMPTS = "EjbTimer.RetryAttempts";
    public static final String OPENEJB_QUARTZ_USE_TCCL = "openejb.quartz.use-TCCL";

    private boolean transacted;
    private int retryAttempts;

    private transient TransactionManager transactionManager;
    private transient BeanContext deployment;
    private transient TimerStore timerStore;
    private transient Scheduler scheduler;

    public EjbTimerServiceImpl(final BeanContext deployment, final TimerStore timerStore) {
        this(deployment, getDefaultTransactionManager(), timerStore, -1);
        log.isDebugEnabled(); // touch logger to force it to be initialized
    }

    public static TransactionManager getDefaultTransactionManager() {
        return SystemInstance.get().getComponent(TransactionManager.class);
    }

    public EjbTimerServiceImpl(final BeanContext deployment, final TransactionManager transactionManager, final TimerStore timerStore, final int retryAttempts) {
        this.deployment = deployment;
        this.transactionManager = transactionManager;
        this.timerStore = timerStore;
        final TransactionType transactionType = deployment.getTransactionType(deployment.getEjbTimeout());
        this.transacted = transactionType == TransactionType.Required || transactionType == TransactionType.RequiresNew;
        this.retryAttempts = retryAttempts;
        if (retryAttempts < 0) {
            this.retryAttempts = deployment.getOptions().get(EJB_TIMER_RETRY_ATTEMPTS, 1);
        }
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeUTF(deployment.getDeploymentID().toString());
        out.writeBoolean(transacted);
        out.writeInt(retryAttempts);
    }

    private void readObject(final ObjectInputStream in) throws IOException {
        final String dId = in.readUTF();
        transacted = in.readBoolean();
        retryAttempts = in.readInt();

        deployment = SystemInstance.get().getComponent(ContainerSystem.class).getBeanContext(dId);
        transactionManager = getDefaultTransactionManager();
        timerStore = deployment.getEjbTimerService().getTimerStore();
        scheduler = (Scheduler) Proxy.newProxyInstance(deployment.getClassLoader(), new Class<?>[]{Scheduler.class}, new LazyScheduler(deployment));
    }

    public static synchronized Scheduler getDefaultScheduler(final BeanContext deployment) {
        Scheduler scheduler = deployment.get(Scheduler.class);
        if (scheduler != null) {
            boolean valid;
            try {
                valid = !scheduler.isShutdown();
            } catch (final Exception ignored) {
                valid = false;
            }
            if (valid) {
                return scheduler;
            }
        }

        Scheduler thisScheduler;
        synchronized (deployment.getId()) { // should be done only once so no perf issues
            scheduler = deployment.get(Scheduler.class);
            if (scheduler != null) {
                return scheduler;
            }

            final Properties properties = new Properties();
            int quartzProps = 0;
            quartzProps += putAll(properties, SystemInstance.get().getProperties());
            quartzProps += putAll(properties, deployment.getModuleContext().getAppContext().getProperties());
            quartzProps += putAll(properties, deployment.getModuleContext().getProperties());
            quartzProps += putAll(properties, deployment.getProperties());

            // custom config -> don't use default/global scheduler
            // if one day we want to keep a global config for a global scheduler (SystemInstance.get().getProperties()) we'll need to manage resume/pause etc correctly by app
            // since we have a scheduler by ejb today in such a case we don't need
            final boolean newInstance = quartzProps > 0;

            final SystemInstance systemInstance = SystemInstance.get();

            scheduler = systemInstance.getComponent(Scheduler.class);

            if (scheduler == null || newInstance) {
                final boolean useTccl = "true".equalsIgnoreCase(properties.getProperty(OPENEJB_QUARTZ_USE_TCCL, "false"));

                defaultQuartzConfiguration(properties, deployment, newInstance, useTccl);

                try {
                    // start in container context to avoid thread leaks
                    final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
                    if (useTccl) {
                        Thread.currentThread().setContextClassLoader(deployment.getClassLoader());
                    } else {
                        Thread.currentThread().setContextClassLoader(EjbTimerServiceImpl.class.getClassLoader());
                    }
                    try {
                        thisScheduler = new StdSchedulerFactory(properties).getScheduler();
                        thisScheduler.start();
                    } finally {
                        Thread.currentThread().setContextClassLoader(oldCl);
                    }

                    //durability is configured with true, which means that the job will be kept in the store even if no trigger is attached to it.
                    //Currently, all the EJB beans share with the same job instance
                    final JobDetail job = JobBuilder.newJob(EjbTimeoutJob.class)
                        .withIdentity(OPENEJB_TIMEOUT_JOB_NAME, OPENEJB_TIMEOUT_JOB_GROUP_NAME)
                        .storeDurably(true)
                        .requestRecovery(false)
                        .build();
                    thisScheduler.addJob(job, true);
                } catch (final SchedulerException e) {
                    throw new OpenEJBRuntimeException("Fail to initialize the default scheduler", e);
                }

                if (!newInstance) {
                    systemInstance.setComponent(Scheduler.class, thisScheduler);
                }
            } else {
                thisScheduler = scheduler;
            }

            deployment.set(Scheduler.class, thisScheduler);
        }

        return thisScheduler;
    }

    private static void defaultQuartzConfiguration(final Properties properties, final BeanContext deployment, final boolean newInstance, final boolean tccl) {
        final String defaultThreadPool = DefaultTimerThreadPoolAdapter.class.getName();
        if (!properties.containsKey(StdSchedulerFactory.PROP_THREAD_POOL_CLASS)) {
            properties.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, defaultThreadPool);
        }
        if (!properties.containsKey(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME)) {
            properties.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "OpenEJB-TimerService-Scheduler");
        }
        if (!properties.containsKey("org.terracotta.quartz.skipUpdateCheck")) {
            properties.put("org.terracotta.quartz.skipUpdateCheck", "true");
        }
        if (!properties.containsKey(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN)) {
            properties.put(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN, "true");
        }
        if (!properties.containsKey(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN_WITH_WAIT)) {
            properties.put(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN_WITH_WAIT, "true");
        }
        if (!properties.containsKey(QUARTZ_MAKE_SCHEDULER_THREAD_DAEMON)) {
            properties.put(QUARTZ_MAKE_SCHEDULER_THREAD_DAEMON, "true");
        }
        if (!properties.containsKey(QUARTZ_JMX) && LocalMBeanServer.isJMXActive()) {
            properties.put(QUARTZ_JMX, "true");
        }
        if (!properties.containsKey(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID)) {
            if (!newInstance) {
                properties.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID, "OpenEJB");
            } else {
                properties.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID, deployment.getDeploymentID().toString());
            }
        }

        if (!tccl) {
            final String driverDelegate = properties.getProperty("org.apache.openejb.quartz.jobStore.driverDelegateClass");
            if (driverDelegate != null && StdJDBCDelegate.class.getName().equals(driverDelegate)) {
                properties.put("org.apache.openejb.quartz.jobStore.driverDelegateClass", PatchedStdJDBCDelegate.class.getName());
            } else if (driverDelegate != null) {
                log.info("You use " + driverDelegate + " driver delegate with quartz, ensure it doesn't use ObjectInputStream otherwise your custom TimerData can induce some issues");
            }

            // adding our custom persister
            if (properties.containsKey("org.apache.openejb.quartz.jobStore.class") && !properties.containsKey("org.apache.openejb.quartz.jobStore.driverDelegateInitString")) {
                try {
                    final Class<?> clazz = EjbTimerServiceImpl.class.getClassLoader().loadClass(properties.getProperty("org.apache.openejb.quartz.jobStore.class"));
                    if (JobStoreSupport.class.isAssignableFrom(clazz)) {
                        properties.put("org.apache.openejb.quartz.jobStore.driverDelegateInitString",
                            "triggerPersistenceDelegateClasses=" + EJBCronTriggerPersistenceDelegate.class.getName());
                    }
                } catch (final Throwable th) {
                    // no-op
                }
            }
        }

        if (defaultThreadPool.equals(properties.get(StdSchedulerFactory.PROP_THREAD_POOL_CLASS))) {
            if (properties.containsKey("org.apache.openejb.quartz.threadPool.threadCount")
                && !properties.containsKey(DefaultTimerThreadPoolAdapter.OPENEJB_TIMER_POOL_SIZE)) {
                log.info("Found property 'org.apache.openejb.quartz.threadPool.threadCount' for default thread pool, please use '"
                    + DefaultTimerThreadPoolAdapter.OPENEJB_TIMER_POOL_SIZE + "' instead");
                properties.put(DefaultTimerThreadPoolAdapter.OPENEJB_TIMER_POOL_SIZE, properties.getProperty("org.apache.openejb.quartz.threadPool.threadCount"));
            }
            if (properties.containsKey("org.quartz.threadPool.threadCount")
                && !properties.containsKey(DefaultTimerThreadPoolAdapter.OPENEJB_TIMER_POOL_SIZE)) {
                log.info("Found property 'org.quartz.threadPool.threadCount' for default thread pool, please use '"
                    + DefaultTimerThreadPoolAdapter.OPENEJB_TIMER_POOL_SIZE + "' instead");
                properties.put(DefaultTimerThreadPoolAdapter.OPENEJB_TIMER_POOL_SIZE, properties.getProperty("org.quartz.threadPool.threadCount"));
            }
        }

        // to ensure we can shutdown correctly, default doesn't support such a configuration
        if (!properties.getProperty(StdSchedulerFactory.PROP_JOB_STORE_CLASS, RAMJobStore.class.getName()).equals(RAMJobStore.class.getName())) {
            properties.put("org.apache.openejb.quartz.jobStore.makeThreadsDaemons", properties.getProperty("org.apache.openejb.quartz.jobStore.makeThreadsDaemon", "true"));
        }
    }

    private static int putAll(final Properties a, final Properties b) {
        int number = 0;
        for (final Map.Entry<Object, Object> entry : b.entrySet()) {
            final String key = entry.getKey().toString();
            if (key.startsWith("org.quartz.")
                || key.startsWith("org.apache.openejb.quartz.")
                || key.startsWith("openejb.quartz.")
                || DefaultTimerThreadPoolAdapter.OPENEJB_TIMER_POOL_SIZE.equals(key)
                || "org.terracotta.quartz.skipUpdateCheck".equals(key)) {
                number++;
            }

            final Object value = entry.getValue();
            if (String.class.isInstance(value)) {
                if (!key.startsWith("org.quartz")) {
                    a.put(key, value);
                } else {
                    a.put("org.apache.openejb.quartz" + key.substring("org.quartz".length()), value);
                }
            }
        }
        return number;
    }

    @Override
    public void stop() {
        cleanTimerData();
        shutdownMyScheduler();
    }

    private void cleanTimerData() {
        if (timerStore == null || scheduler == null || deployment == null) {
            return;
        }

        final Collection<TimerData> timerDatas = timerStore.getTimers(deployment.getDeploymentID().toString());
        if (timerDatas == null) {
            return;
        }

        for (final TimerData data : timerDatas) {
            final Trigger trigger = data.getTrigger();
            if (trigger == null) {
                continue;
            }

            final TriggerKey key = trigger.getKey();
            try {
                data.stop();
            } catch (final EJBException ignored) {
                log.warning("An error occured deleting trigger '" + key + "' on bean " + deployment.getDeploymentID());
            }
        }
    }

    private void shutdownMyScheduler() {
        if (scheduler == null) {
            return;
        }

        boolean defaultScheduler = false;
        final Scheduler ds = SystemInstance.get().getComponent(Scheduler.class);
        try { // == is the faster way to test, we rely on name (key in quartz registry) only for serialization
            defaultScheduler = ds == scheduler || scheduler.getSchedulerName().equals(ds.getSchedulerName());
        } catch (final Exception e) {
            // no-op: default should be fine
        }

        // if specific instance
        if (!defaultScheduler) {
            shutdown(scheduler);
        }
    }

    public static void shutdown() {
        shutdown(SystemInstance.get().getComponent(Scheduler.class));
    }

    private static void shutdown(final Scheduler s) throws OpenEJBRuntimeException {

        try {
            if (null != s && !s.isShutdown() && s.isStarted()) {

                try {
                    s.pauseAll();
                } catch (final SchedulerException e) {
                    // no-op
                }

                long timeout = SystemInstance.get().getOptions().get(QuartzResourceAdapter.OPENEJB_QUARTZ_TIMEOUT, 10000L);

                if (timeout < 1000L) {
                    timeout = 1000L;
                }

                final CountDownLatch shutdownWait = new CountDownLatch(1);
                final AtomicReference<Throwable> ex = new AtomicReference<>();

                String n = "Unknown";
                try {
                    n = s.getSchedulerName();
                } catch (final SchedulerException e) {
                    log.warning("EjbTimerService scheduler has no name");
                }

                final String name = n;

                Thread stopThread = new Thread(name + " shutdown wait") {

                    @Override
                    public void run() {
                        try {
                            s.getListenerManager().addSchedulerListener(new SchedulerListenerSupport() {
                                @Override
                                public void schedulerShutdown() {
                                    shutdownWait.countDown();
                                }
                            });

                            //Shutdown, but give running jobs a chance to complete.
                            //User scheduled jobs should really implement InterruptableJob
                            s.shutdown(true);
                        } catch (final Throwable e) {
                            ex.set(e);
                            shutdownWait.countDown();
                        }
                    }
                };

                stopThread.setDaemon(true);
                stopThread.start();

                boolean stopped = false;
                try {
                    stopped = shutdownWait.await(timeout, TimeUnit.MILLISECONDS);
                } catch (final InterruptedException e) {
                    //Ignore
                }

                try {
                    if (!stopped || !s.isShutdown()) {

                        stopThread = new Thread(name + " shutdown forced") {

                            @Override
                            public void run() {
                                try {
                                    //Force a shutdown without waiting for jobs to complete.
                                    s.shutdown(false);
                                    log.warning("Forced " + name + " shutdown - Jobs may be incomplete");
                                } catch (final Throwable e) {
                                    ex.set(e);
                                }
                            }
                        };

                        stopThread.setDaemon(true);
                        stopThread.start();

                        try {
                            //Give the forced shutdown a chance to complete
                            stopThread.join(timeout);
                        } catch (final InterruptedException e) {
                            //Ignore
                        }
                    }
                } catch (final Throwable e) {
                    ex.set(e);
                }

                final Throwable t = ex.get();
                if (null != t) {
                    throw new OpenEJBRuntimeException("Unable to shutdown " + name + " scheduler", t);
                }
            }
        } catch (final SchedulerException e) {
            //Ignore - This can only be a shutdown issue that we have no control over.
        }
    }

    @Override
    public void start() throws TimerStoreException {

        if (isStarted()) {
            return;
        }

        scheduler = getDefaultScheduler(deployment);

        // load saved timers
        final Collection<TimerData> timerDatas = timerStore.loadTimers(this, (String) deployment.getDeploymentID());
        // schedule the saved timers
        for (final TimerData timerData : timerDatas) {
            initializeNewTimer(timerData);
        }
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Called from TimerData and start when a timer should be scheduled with the java.util.Timer.
     *
     * @param timerData the timer to schedule
     */
    public void schedule(final TimerData timerData) throws TimerStoreException {

        start();

        if (scheduler == null) {
            throw new TimerStoreException("Scheduler is not configured properly");
        }

        timerData.setScheduler(scheduler);

        final Trigger trigger = timerData.getTrigger();

        if (null == trigger) {

            try {
                if (!scheduler.isShutdown()) {
                    log.warning("Failed to schedule: " + timerData.getInfo());
                }
            } catch (final SchedulerException e) {
                //Ignore
            }
        }

        final AbstractTrigger<?> atrigger;
        if (trigger instanceof AbstractTrigger) { // is the case
            atrigger = (AbstractTrigger<?>) trigger;
            atrigger.setJobName(OPENEJB_TIMEOUT_JOB_NAME);
            atrigger.setJobGroup(OPENEJB_TIMEOUT_JOB_GROUP_NAME);
        } else {
            throw new OpenEJBRuntimeException("the trigger was not an AbstractTrigger - Should not be possible: " + trigger);
        }

        final JobDataMap triggerDataMap = trigger.getJobDataMap();
        triggerDataMap.put(EjbTimeoutJob.EJB_TIMERS_SERVICE, this);
        triggerDataMap.put(EjbTimeoutJob.TIMER_DATA, timerData);

        try {
            final TriggerKey triggerKey = new TriggerKey(atrigger.getName(), atrigger.getGroup());
            if (!scheduler.checkExists(triggerKey)) {
                scheduler.scheduleJob(trigger);
            } else if (Trigger.TriggerState.PAUSED.equals(scheduler.getTriggerState(triggerKey))) { // redeployment
                // more consistent in the semantic than a resume but resume would maybe be more relevant here
                scheduler.unscheduleJob(triggerKey);
                scheduler.scheduleJob(trigger);
            }
        } catch (final Exception e) {
            //TODO Any other actions we could do ?
            log.error("Could not schedule timer " + timerData, e);
        }
    }

    /**
     * Call back from TimerData and ejbTimeout when a timer has been cancelled (or is complete) and should be removed from stores.
     *
     * @param timerData the timer that was cancelled
     */
    public void cancelled(final TimerData timerData) {
        // make sure it was removed from the store
        timerStore.removeTimer(timerData.getId());
    }

    /**
     * Returns a timerData to the TimerStore, if a cancel() is rolled back.
     *
     * @param timerData the timer to be returned to the timer store
     */
    public void addTimerData(final TimerData timerData) {
        try {
            timerStore.addTimerData(timerData);
        } catch (final Exception e) {
            log.warning("Could not add timer of type " + timerData.getType().name() + " due to " + e.getMessage());
        }
    }

    @Override
    public Timer getTimer(final long timerId) {
        final TimerData timerData = timerStore.getTimer((String) deployment.getDeploymentID(), timerId);
        if (timerData != null) {
            return timerData.getTimer();
        } else {
            return null;
        }
    }

    @Override
    public Collection<Timer> getTimers(final Object primaryKey) throws IllegalStateException {
        checkState();

        final Collection<Timer> timers = new ArrayList<>();
        for (final TimerData timerData : timerStore.getTimers((String) deployment.getDeploymentID())) {
            // Returns all active timers associated with this bean.
            if (timerData.isCancelled() || timerData.isExpired() || timerData.isStopped()) {
                continue;
            }
            timers.add(timerData.getTimer());
        }
        return timers;
    }

    @Override
    public Timer createTimer(final Object primaryKey,
                             final Method timeoutMethod,
                             final long duration,
                             final TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        if (duration < 0) {
            throw new IllegalArgumentException("duration is negative: " + duration);
        }
        checkState();

        final Date expiration = new Date(System.currentTimeMillis() + duration);
        try {
            final TimerData timerData = timerStore.createSingleActionTimer(this, (String) deployment.getDeploymentID(), primaryKey, timeoutMethod, expiration, timerConfig);
            initializeNewTimer(timerData);
            return timerData.getTimer();
        } catch (final TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    @Override
    public Timer createTimer(final Object primaryKey,
                             final Method timeoutMethod,
                             final long initialDuration,
                             final long intervalDuration,
                             final TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        if (initialDuration < 0) {
            throw new IllegalArgumentException("initialDuration is negative: " + initialDuration);
        }
        if (intervalDuration < 0) {
            throw new IllegalArgumentException("intervalDuration is negative: " + intervalDuration);
        }
        checkState();

        final Date initialExpiration = new Date(System.currentTimeMillis() + initialDuration);
        try {
            final TimerData timerData = timerStore.createIntervalTimer(this,
                (String) deployment.getDeploymentID(),
                primaryKey,
                timeoutMethod,
                initialExpiration,
                intervalDuration,
                timerConfig);
            initializeNewTimer(timerData);
            return timerData.getTimer();
        } catch (final TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    @Override
    public Timer createTimer(final Object primaryKey,
                             final Method timeoutMethod,
                             final Date expiration,
                             final TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        if (expiration == null) {
            throw new IllegalArgumentException("expiration is null");
        }
        if (expiration.getTime() < 0) {
            throw new IllegalArgumentException("expiration is negative: " + expiration.getTime());
        }
        checkState();

        try {
            final TimerData timerData = timerStore.createSingleActionTimer(this, (String) deployment.getDeploymentID(), primaryKey, timeoutMethod, expiration, timerConfig);
            initializeNewTimer(timerData);
            return timerData.getTimer();
        } catch (final TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    @Override
    public Timer createTimer(final Object primaryKey,
                             final Method timeoutMethod,
                             final Date initialExpiration,
                             final long intervalDuration,
                             final TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        if (initialExpiration == null) {
            throw new IllegalArgumentException("initialExpiration is null");
        }
        if (initialExpiration.getTime() < 0) {
            throw new IllegalArgumentException("initialExpiration is negative: " + initialExpiration.getTime());
        }
        if (intervalDuration < 0) {
            throw new IllegalArgumentException("intervalDuration is negative: " + intervalDuration);
        }
        checkState();

        try {
            final TimerData timerData = timerStore.createIntervalTimer(this,
                (String) deployment.getDeploymentID(),
                primaryKey,
                timeoutMethod,
                initialExpiration,
                intervalDuration,
                timerConfig);
            initializeNewTimer(timerData);
            return timerData.getTimer();
        } catch (final TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    @Override
    public Timer createTimer(final Object primaryKey, final Method timeoutMethod, final ScheduleExpression scheduleExpression, final TimerConfig timerConfig) {
        if (scheduleExpression == null) {
            throw new IllegalArgumentException("scheduleExpression is null");
        }
        //TODO add more schedule expression validation logic ?
        checkState();
        try {
            final TimerData timerData = timerStore.createCalendarTimer(this,
                (String) deployment.getDeploymentID(),
                primaryKey,
                timeoutMethod,
                scheduleExpression,
                timerConfig,
                false);
            initializeNewTimer(timerData);
            return timerData.getTimer();
        } catch (final TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    @Override
    public TimerStore getTimerStore() {
        return timerStore;
    }

    @Override
    public boolean isStarted() {
        return scheduler != null;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    private void initializeNewTimer(final TimerData timerData) {
        // mark this as a new timer... when the transaction completes it will schedule the timer
        timerData.newTimer();
    }

    /**
     * Insure that timer methods can be invoked for the current operation on this Context.
     */
    private void checkState() throws IllegalStateException {
        final BaseContext context = (BaseContext) deployment.get(EJBContext.class);
        context.doCheck(BaseContext.Call.timerMethod);
    }

    /**
     * This method calls the ejbTimeout method and starts a transaction if the timeout is transacted.
     *
     * This method will retry failed ejbTimeout calls until retryAttempts is exceeded.
     *
     * @param timerData the timer to call.
     */
    @SuppressWarnings("ReturnInsideFinallyBlock")
    public void ejbTimeout(final TimerData timerData) {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = thread.getContextClassLoader(); // container loader
        try {
            Timer timer = getTimer(timerData.getId());
            // quartz can be backed by some advanced config (jdbc for instance)
            if (timer == null && timerStore instanceof MemoryTimerStore && timerData.getTimer() != null) {
                try {
                    timerStore.addTimerData(timerData);
                    timer = timerData.getTimer(); // TODO: replace memoryjobstore by the db one?
                } catch (final TimerStoreException e) {
                    // shouldn't occur
                }
                // return;
            }
            for (int tries = 0; tries < 1 + retryAttempts; tries++) {
                boolean retry = false;
                // if transacted, begin the transaction
                if (transacted) {
                    try {
                        transactionManager.begin();
                    } catch (final Exception e) {
                        log.warning("Exception occurred while starting container transaction", e);
                        return;
                    }
                }
                // call the timeout method
                try {
                    final RpcContainer container = (RpcContainer) deployment.getContainer();
                    if (container == null) {
                        return;
                    }

                    final Method ejbTimeout = timerData.getTimeoutMethod();
                    if (ejbTimeout == null) {
                        return;
                    }

                    // if app registered Synchronization we need it for commit()/rollback()
                    // so forcing it and not relying on container for it
                    thread.setContextClassLoader(deployment.getClassLoader() != null ? deployment.getClassLoader() : loader);

                    SetAccessible.on(ejbTimeout);
                    container.invoke(deployment.getDeploymentID(),
                        InterfaceType.TIMEOUT,
                        ejbTimeout.getDeclaringClass(),
                        ejbTimeout,
                        new Object[]{timer},
                        timerData.getPrimaryKey());
                } catch (final RuntimeException e) {
                    retry = true;
                    // exception from a timer does not necessairly mean failure
                    log.warning("RuntimeException from ejbTimeout on " + deployment.getDeploymentID(), e);
                    try {
                        transactionManager.setRollbackOnly();
                    } catch (final SystemException e1) {
                        log.warning("Exception occured while setting RollbackOnly for container transaction", e1);
                    }
                } catch (final OpenEJBException e) {
                    retry = true;
                    if (ApplicationException.class.isInstance(e)) { // we don't want to pollute logs
                        log.debug("Exception from ejbTimeout on " + deployment.getDeploymentID(), e);
                    } else {
                        log.warning("Exception from ejbTimeout on " + deployment.getDeploymentID(), e);
                    }
                    if (transacted) {
                        try {
                            transactionManager.setRollbackOnly();
                        } catch (final SystemException e1) {
                            log.warning("Exception occured while setting RollbackOnly for container transaction", e1);
                        }
                    }
                } finally {
                    try {
                        if (!transacted) {
                            if (!retry) {
                                return;
                            }
                        } else if (transactionManager.getStatus() == Status.STATUS_ACTIVE) {
                            transactionManager.commit();
                            return;
                        } else {
                            // tx was marked rollback, so roll it back and retry.
                            transactionManager.rollback();
                        }
                    } catch (final Exception e) {
                        log.warning("Exception occured while completing container transaction", e);
                    }
                }
            }
            log.warning("Failed to execute ejbTimeout on " + timerData.getDeploymentId() + " successfully within " + retryAttempts + " attempts");
        } catch (final RuntimeException e) {
            log.warning("RuntimeException occured while calling ejbTimeout", e);
            throw e;
        } catch (final Error e) {
            log.warning("Error occured while calling ejbTimeout", e);
            throw e;
        } finally {
            thread.setContextClassLoader(loader);

            // clean up the timer store
            //TODO shall we do all this via Quartz listener ???
            if (timerData.getType() == TimerType.SingleAction) {
                timerStore.removeTimer(timerData.getId());
                timerData.setExpired(true);
            } else if (timerData.getType() == TimerType.Calendar && timerData.getNextTimeout() == null) {
                timerStore.removeTimer(timerData.getId());
                timerData.setExpired(true);
            } else {
                timerStore.updateIntervalTimer(timerData);
            }
        }
    }

    /**
     * The timer task registered with the java.util.Timer.  The run method of this class
     * simply adds an execution of the ejbTimeout method to the thread pool.  It is
     * important to use the thread pool, since the java.util.Timer is single threaded.
     */
    /*private class EjbTimeoutTimerTask extends TimerTask {
        private final TimerData timerData;

        public EjbTimeoutTimerTask(TimerData timerData) {
            this.timerData = timerData;
        }

        public void run() {
            threadPool.execute(new Runnable() {
                public void run() {
                    ejbTimeout(timerData);
                }
            });
        }
    }*/

    private static class LazyScheduler implements InvocationHandler {

        private final BeanContext ejb;

        public LazyScheduler(final BeanContext deployment) {
            ejb = deployment;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            return method.invoke(getDefaultScheduler(ejb), args);
        }
    }
}
