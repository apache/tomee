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

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.SetAccessible;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.AbstractTrigger;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;


public class EjbTimerServiceImpl implements EjbTimerService {
    private static final Logger log = Logger.getInstance(LogCategory.TIMER, "org.apache.openejb.util.resources");

    public static final String QUARTZ_THREAD_POOL_ADAPTER = "openejb.org.quartz.threadPool.class";

    public static final String OPENEJB_TIMEOUT_JOB_NAME = "OPENEJB_TIMEOUT_JOB";
    public static final String OPENEJB_TIMEOUT_JOB_GROUP_NAME = "OPENEJB_TIMEOUT_GROUP";
    private final TransactionManager transactionManager;
    final BeanContext deployment;
    private final boolean transacted;
    private final int retryAttempts;

    private final TimerStore timerStore;

    private Scheduler scheduler;

    public EjbTimerServiceImpl(BeanContext deployment) {
        this(deployment, getDefaultTransactionManager(), getDefaultScheduler(), new MemoryTimerStore(getDefaultTransactionManager()), 1);
    }

    public static TransactionManager getDefaultTransactionManager() {
        return SystemInstance.get().getComponent(TransactionManager.class);
    }

    public EjbTimerServiceImpl(BeanContext deployment, TransactionManager transactionManager, Scheduler scheduler, TimerStore timerStore, int retryAttempts) {
        this.deployment = deployment;
        this.transactionManager = transactionManager;
        this.scheduler = scheduler;
        this.timerStore = timerStore;
        TransactionType transactionType = deployment.getTransactionType(deployment.getEjbTimeout());
        this.transacted = transactionType == TransactionType.Required || transactionType == TransactionType.RequiresNew;
        this.retryAttempts = retryAttempts;
    }

    public static synchronized Scheduler getDefaultScheduler() {
        Scheduler scheduler = SystemInstance.get().getComponent(Scheduler.class);
        if (scheduler == null) {
            Properties properties = new Properties();
            properties.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, SystemInstance.get().hasProperty(QUARTZ_THREAD_POOL_ADAPTER) ? SystemInstance.get().getOptions().get(QUARTZ_THREAD_POOL_ADAPTER, "")
                    : DefaultTimerThreadPoolAdapter.class.getName());
            properties.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "OpenEJB-TimerService-Scheduler");
            try {
                scheduler = new StdSchedulerFactory(properties).getScheduler();
                scheduler.start();
                //durability is configured with true, which means that the job will be kept in the store even if no trigger is attached to it.
                //Currently, all the EJB beans share with the same job instance
                JobDetail job = JobBuilder.newJob(EjbTimeoutJob.class)
                        .withIdentity(OPENEJB_TIMEOUT_JOB_NAME, OPENEJB_TIMEOUT_JOB_GROUP_NAME)
                        .storeDurably(true)
                        .requestRecovery(false)
                        .build();
                scheduler.addJob(job, true);
            } catch (SchedulerException e) {
                throw new RuntimeException("Fail to initialize the default scheduler", e);
            }
            SystemInstance.get().setComponent(Scheduler.class, scheduler);
        }
        return scheduler;
    }

    public static void shutdown() {

        Scheduler scheduler = SystemInstance.get().getComponent(Scheduler.class);
        if (scheduler != null) try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            throw new RuntimeException("Unable to shutdown scheduler", e);
        }

    }
    public void start() throws TimerStoreException {
         // load saved timers
        Collection<TimerData> timerDatas = timerStore.loadTimers(this, (String)deployment.getDeploymentID());
        // schedule the saved timers
        for (TimerData timerData : timerDatas) {
            initializeNewTimer(timerData);
        }
    }

    public void stop() {
        // stop all timers
        for (TimerData timerData : timerStore.getTimers((String) deployment.getDeploymentID())) {
            try {
                timerData.stop();
            } catch (EJBException e) {
                //Suppress all the exception as we are in the shutdown process
                log.error("fail to stop timer", e);
            }
        }
        //scheduler.shutdown();
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Called from TimerData and start when a timer should be scheduled with the java.util.Timer.
     * @param timerData the timer to schedule
     */
    public void schedule(TimerData timerData) {
        if (scheduler == null) throw new IllegalStateException("scheduler is configured properly");
        
        timerData.setScheduler(scheduler);
        
        Trigger trigger = timerData.getTrigger();
        if (trigger instanceof AbstractTrigger) { // is the case
            AbstractTrigger<?> atrigger = (AbstractTrigger<?>) trigger;
            atrigger.setJobName(OPENEJB_TIMEOUT_JOB_NAME);
            atrigger.setJobGroup(OPENEJB_TIMEOUT_JOB_GROUP_NAME);
        } else {
            throw new RuntimeException("the trigger was not an AbstractTrigger - it shouldn't be possible");
        }
        JobDataMap triggerDataMap = trigger.getJobDataMap();
        triggerDataMap.put(EjbTimeoutJob.EJB_TIMERS_SERVICE, this);
        triggerDataMap.put(EjbTimeoutJob.TIMER_DATA,timerData);
        try {
            scheduler.scheduleJob(trigger);
        } catch (Exception e) {
            //TODO Any other actions we could do ?
            log.warning("Could not schedule timer " + timerData, e);
        }
    }

    /**
     * Call back from TimerData and ejbTimeout when a timer has been cancelled (or is complete) and should be removed from stores.
     * @param timerData the timer that was cancelled
     */
    public void cancelled(TimerData timerData) {
        // make sure it was removed from the strore
        timerStore.removeTimer(timerData.getId());
    }

    /**
     * Returns a timerData to the TimerStore, if a cancel() is rolled back.
     * @param timerData the timer to be returned to the timer store
     */
    public void addTimerData(TimerData timerData) {
        try {
            timerStore.addTimerData(timerData);
        } catch (Exception e) {
            log.warning("Could not add timer of type "+ timerData.getType().name() + " due to " + e.getMessage());
        }
    }

    public Timer getTimer(long timerId) {
        TimerData timerData = timerStore.getTimer((String)deployment.getDeploymentID(), timerId);
        if (timerData != null) {
            return timerData.getTimer();
        } else {
            return null;
        }
    }

    public Collection<Timer> getTimers(Object primaryKey) throws IllegalStateException {
        checkState();

        Collection<Timer> timers = new ArrayList<Timer>();
        for (TimerData timerData : timerStore.getTimers((String)deployment.getDeploymentID())) {
            timers.add(timerData.getTimer());
        }
        return timers;
    }

    @Override
    public Timer createTimer(Object primaryKey, Method timeoutMethod, long duration, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        if (duration < 0) throw new IllegalArgumentException("duration is negative: " + duration);
        checkState();

        Date expiration = new Date(System.currentTimeMillis() + duration);
        try {
            TimerData timerData = timerStore.createSingleActionTimer(this, (String) deployment.getDeploymentID(), primaryKey, timeoutMethod, expiration, timerConfig);
            initializeNewTimer(timerData);
            return timerData.getTimer();
        } catch (TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    @Override
    public Timer createTimer(Object primaryKey, Method timeoutMethod, long initialDuration, long intervalDuration, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        if (initialDuration < 0) throw new IllegalArgumentException("initialDuration is negative: " + initialDuration);
        if (intervalDuration < 0) throw new IllegalArgumentException("intervalDuration is negative: " + intervalDuration);
        checkState();


        Date initialExpiration = new Date(System.currentTimeMillis() + initialDuration);
        try {
            TimerData timerData = timerStore.createIntervalTimer(this, (String) deployment.getDeploymentID(), primaryKey, timeoutMethod, initialExpiration, intervalDuration, timerConfig);
            initializeNewTimer(timerData);
            return timerData.getTimer();
        } catch (TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    @Override
    public Timer createTimer(Object primaryKey, Method timeoutMethod, Date expiration, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        if (expiration == null) throw new IllegalArgumentException("expiration is null");
        if (expiration.getTime() < 0) throw new IllegalArgumentException("expiration is negative: " + expiration.getTime());
        checkState();

        try {
            TimerData timerData = timerStore.createSingleActionTimer(this, (String) deployment.getDeploymentID(), primaryKey, timeoutMethod, expiration, timerConfig);
            initializeNewTimer(timerData);
            return timerData.getTimer();
        } catch (TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    @Override
    public Timer createTimer(Object primaryKey, Method timeoutMethod, Date initialExpiration, long intervalDuration, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        if (initialExpiration == null) throw new IllegalArgumentException("initialExpiration is null");
        if (initialExpiration.getTime() < 0) throw new IllegalArgumentException("initialExpiration is negative: " + initialExpiration.getTime());
        if (intervalDuration < 0) throw new IllegalArgumentException("intervalDuration is negative: " + intervalDuration);
        checkState();

        try {
            TimerData timerData = timerStore.createIntervalTimer(this, (String) deployment.getDeploymentID(), primaryKey, timeoutMethod, initialExpiration, intervalDuration, timerConfig);
            initializeNewTimer(timerData);
            return timerData.getTimer();
        } catch (TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    @Override
    public Timer createTimer(Object primaryKey, Method timeoutMethod, ScheduleExpression scheduleExpression, TimerConfig timerConfig) {
        if(scheduleExpression == null) {
            throw new IllegalArgumentException("scheduleExpression is null");
        }
        //TODO add more schedule expression validation logic ?
        checkState();
        try {
            TimerData timerData = timerStore.createCalendarTimer(this, (String) deployment.getDeploymentID(), primaryKey, timeoutMethod, scheduleExpression, timerConfig);
            initializeNewTimer(timerData);
            return timerData.getTimer();
        } catch (TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    public TimerStore getTimerStore() {
        return timerStore;
    }

    public Scheduler getScheduler(){
        return scheduler;
    }

    private void initializeNewTimer(TimerData timerData){
        // mark this as a new timer... when the transaction completes it will schedule the timer
        timerData.newTimer();
    }
    /**
     * Insure that timer methods can be invoked for the current operation on this Context.
     */
    private void checkState() throws IllegalStateException {
        final BaseContext context = (BaseContext) deployment.get(EJBContext.class);
        context.check(BaseContext.Call.timerMethod);
    }

    /**
     * This method calls the ejbTimeout method and starts a transaction if the timeout is transacted.
     *
     * This method will retry failed ejbTimeout calls until retryAttempts is exceeded.
     *
     * @param timerData the timer to call.
     */
    public void ejbTimeout(TimerData timerData) {
        try {
            Timer timer = getTimer(timerData.getId());
            if (timer == null) {
                return;
            }
            for (int tries = 0; tries < (1 + retryAttempts); tries++) {
                boolean retry = false;
                // if transacted, begin the transaction
                if (transacted) {
                    try {
                        transactionManager.begin();
                    } catch (Exception e) {
                        log.warning("Exception occured while starting container transaction", e);
                        return;
                    }
                }
                // call the timeout method
                try {
                    RpcContainer container = (RpcContainer) deployment.getContainer();
                    Method ejbTimeout = timerData.getTimeoutMethod();
                    SetAccessible.on(ejbTimeout);
                    container.invoke(deployment.getDeploymentID(), InterfaceType.TIMEOUT, ejbTimeout.getDeclaringClass(), ejbTimeout, new Object[] { timer }, timerData.getPrimaryKey());
                } catch (RuntimeException e) {
                    retry = true;
                    // exception from a timer does not necessairly mean failure
                    log.warning("RuntimeException from ejbTimeout on " + deployment.getDeploymentID(), e);
                    try {
                        transactionManager.setRollbackOnly();
                    } catch (SystemException e1) {
                        log.warning("Exception occured while setting RollbackOnly for container transaction", e1);
                    }
                } catch (OpenEJBException e) {
                    retry = true;
                    log.warning("Exception from ejbTimeout on " + deployment.getDeploymentID(), e);
                    if (transacted) {
                        try {
                            transactionManager.setRollbackOnly();
                        } catch (SystemException e1) {
                            log.warning("Exception occured while setting RollbackOnly for container transaction", e1);
                        }
                    }
                } finally {
                    try {
                        if (!transacted) {
                            if (retry) {
                                continue;
                            } else {
                                return;
                            }
                        } else if (transactionManager.getStatus() == Status.STATUS_ACTIVE) {
                            transactionManager.commit();
                            return;
                        } else {
                            // tx was marked rollback, so roll it back and retry.
                            transactionManager.rollback();
                            continue;
                        }
                    } catch (Exception e) {
                        log.warning("Exception occured while completing container transaction", e);
                    }
                }
            }
            log.warning("Failed to execute ejbTimeout on " + timerData.getDeploymentId() + " successfully within " + retryAttempts + " attempts");
        } catch (RuntimeException e) {
            log.warning("RuntimeException occured while calling ejbTimeout", e);
            throw e;
        } catch (Error e) {
            log.warning("Error occured while calling ejbTimeout", e);
            throw e;
        } finally {
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
}