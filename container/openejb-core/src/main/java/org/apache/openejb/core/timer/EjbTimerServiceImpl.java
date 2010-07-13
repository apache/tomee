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
package org.apache.openejb.core.timer;

import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.loader.SystemInstance;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.transaction.Status;
import javax.transaction.TransactionManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.lang.reflect.Method;

public class EjbTimerServiceImpl implements EjbTimerService {
    private static final Logger log = Logger.getInstance(LogCategory.TIMER, "org.apache.openejb.util.resources");

    private final TransactionManager transactionManager;
    final DeploymentInfo deployment;
    private final boolean transacted;
    private final int retryAttempts;

    private final TimerStore timerStore;
    private final Executor threadPool;

    private java.util.Timer timer;

    public EjbTimerServiceImpl(DeploymentInfo deployment) {
        this(deployment, getDefaultTransactionManager(), getDefaultExecutor(), new MemoryTimerStore(getDefaultTransactionManager()), 1);
    }

    public static Executor getDefaultExecutor() {
        Executor executor = SystemInstance.get().getComponent(Executor.class);
        if (executor == null) {
            executor = Executors.newFixedThreadPool(10);
            SystemInstance.get().setComponent(Executor.class, executor);
        }
        return executor;
    }

    public static TransactionManager getDefaultTransactionManager() {
        return SystemInstance.get().getComponent(TransactionManager.class);
    }

    public EjbTimerServiceImpl(DeploymentInfo deployment, TransactionManager transactionManager, Executor threadPool, TimerStore timerStore, int retryAttempts) {
        if (deployment.getEjbTimeout() == null) throw new IllegalArgumentException("Ejb does not have an ejbTimeout method " + deployment.getDeploymentID());

        this.deployment = deployment;
        this.transactionManager = transactionManager;
        this.threadPool = threadPool;
        this.timerStore = timerStore;
        TransactionType transactionType = deployment.getTransactionType(deployment.getEjbTimeout());
        this.transacted = transactionType == TransactionType.Required || transactionType == TransactionType.RequiresNew;
        this.retryAttempts = retryAttempts;
    }

    public void start() throws TimerStoreException {
        // load saved timers
        Collection timerDatas = timerStore.loadTimers(this, (String)deployment.getDeploymentID());

        // create a new java.util.Timer
        timer = new java.util.Timer(true);

        // schedule the saved timers
        for (Iterator iterator = timerDatas.iterator(); iterator.hasNext();) {
            TimerData timerData = (TimerData) iterator.next();

            // schedule the timer with the java.util.Timer
            schedule(timerData);
        }
    }

    public void stop() {
        // stop all timers
        for (Iterator iterator = timerStore.getTimers((String)deployment.getDeploymentID()).iterator(); iterator.hasNext();) {
            TimerData timerData = (TimerData) iterator.next();
            timerData.stop();
        }

        // stop the java.util.Timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Called from TimerData and start when a timer should be scheduled with the java.util.Timer.
     * @param timerData the timer to schedule
     */
    public void schedule(TimerData timerData) {
        if (timer == null) throw new IllegalStateException("Timer is stopped");

        try {
            EjbTimeoutTimerTask timerTask = new EjbTimeoutTimerTask(timerData);
            timerData.setTimerTask(timerTask);
            if (timerData.isOneTime()) {
                timer.schedule(timerTask, timerData.getExpiration());
            } else {
                timer.scheduleAtFixedRate(timerTask, timerData.getExpiration(), timerData.getIntervalDuration());
            }
        } catch (Exception e) {
            log.warning("Could not schedule timer " + e.getMessage() + " at (now) " + System.currentTimeMillis() + " for " + timerData.getExpiration().getTime());
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
            log.warning("Could not add timer " + e.getMessage() + " at (now) " + System.currentTimeMillis() + " for " + timerData.getExpiration().getTime());
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
        for (Iterator iterator = timerStore.getTimers((String)deployment.getDeploymentID()).iterator(); iterator.hasNext();) {
            TimerData timerData = (TimerData) iterator.next();
            Timer timer = timerData.getTimer();
            timers.add(timer);
        }
        return timers;
    }

    public Timer createTimer(Object primaryKey, long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        if (duration < 0) throw new IllegalArgumentException("duration is negative: " + duration);
        checkState();

        Date time = new Date(System.currentTimeMillis() + duration);
        try {
            TimerData timerData = createTimerData(primaryKey, time, 0, info);
            return timerData.getTimer();
        } catch (TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    public Timer createTimer(Object primaryKey, long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        if (initialDuration < 0) throw new IllegalArgumentException("initialDuration is negative: " + initialDuration);
        if (intervalDuration < 0) throw new IllegalArgumentException("intervalDuration is negative: " + intervalDuration);
        checkState();


        Date time = new Date(System.currentTimeMillis() + initialDuration);
        try {
            TimerData timerData = createTimerData(primaryKey, time, intervalDuration, info);
            return timerData.getTimer();
        } catch (TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    public Timer createTimer(Object primaryKey, Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        if (expiration == null) throw new IllegalArgumentException("expiration is null");
        if (expiration.getTime() < 0) throw new IllegalArgumentException("expiration is negative: " + expiration.getTime());
        checkState();

        try {
            TimerData timerData = createTimerData(primaryKey, expiration, 0, info);
            return timerData.getTimer();
        } catch (TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    public Timer createTimer(Object primaryKey, Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        if (initialExpiration == null) throw new IllegalArgumentException("initialExpiration is null");
        if (initialExpiration.getTime() < 0) throw new IllegalArgumentException("initialExpiration is negative: " + initialExpiration.getTime());
        if (intervalDuration < 0) throw new IllegalArgumentException("intervalDuration is negative: " + intervalDuration);
        checkState();

        try {
            TimerData timerData = createTimerData(primaryKey, initialExpiration, intervalDuration, info);
            return timerData.getTimer();
        } catch (TimerStoreException e) {
            throw new EJBException(e);
        }
    }

    private TimerData createTimerData(Object primaryKey, Date expiration, long intervalDuration, Object info) throws TimerStoreException {
        TimerData timerData = timerStore.createTimer(this, (String)deployment.getDeploymentID(), primaryKey, info, expiration, intervalDuration);

        // mark this as a new timer... when the transaction completes it will schedule the timer
        timerData.newTimer();

        return timerData;
    }

    /**
     * Insure that timer methods can be invoked for the current operation on this Context.
     */
    private void checkState() throws IllegalStateException {
        final BaseContext context = (BaseContext) deployment.get(EJBContext.class);
        if (!context.isTimerMethodAllowed()) {
            throw new IllegalStateException("TimerService method not permitted for current operation " + ThreadContext.getThreadContext().getCurrentOperation().name());
        }
    }

    /**
     * This method calls the ejbTimeout method and starts a transaction if the timeout is transacted.
     *
     * This method will retry failed ejbTimeout calls until retryAttempts is exceeded.
     *
     * @param timerData the timer to call.
     */
    private void ejbTimeout(TimerData timerData) {
        try {
            Timer timer = getTimer(timerData.getId());
            if (timer == null) {
                return;
            }

            for (int tries = 0; tries < (1 + retryAttempts); tries++) {
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
                    Method ejbTimeout = deployment.getEjbTimeout();
                    container.invoke(deployment.getDeploymentID(), InterfaceType.TIMEOUT, ejbTimeout.getDeclaringClass(), ejbTimeout, new Object[] { timer }, timerData.getPrimaryKey());
                } catch (RuntimeException e) {
                    // exception from a timer does not necessairly mean failure
                    log.warning("RuntimeException from ejbTimeout on " + deployment.getDeploymentID(), e);
                } catch (OpenEJBException e) {
                    log.warning("Exception from ejbTimeout on " + deployment.getDeploymentID(), e);
                } finally {
                    try {
                        if (!transacted || transactionManager.getStatus() == Status.STATUS_ACTIVE) {
                            // clean up the timer store
                            if (timerData.isOneTime()) {
                                timerStore.removeTimer(timerData.getId());
                            } else {
                                timerData.nextTime();
                                timerStore.updateIntervalTimer(timerData);
                            }

                            // commit the tx
                            if (transacted) {
                                transactionManager.commit();
                            }

                            // all is cool
                            //noinspection ReturnInsideFinallyBlock
                            return;
                        } else {
                            // tx was marked rollback, so roll it back
                            if (transacted) {
                                transactionManager.rollback();
                            }
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
            // if this is a single action timer, mark it as cancelled
            if (timerData.isOneTime()) {
                cancelled(timerData);
            }
        }
    }

    /**
     * The timer task registered with the java.util.Timer.  The run method of this class
     * simply adds an execution of the ejbTimeout method to the thread pool.  It is
     * important to use the thread pool, since the java.util.Timer is single threaded.
     */
    private class EjbTimeoutTimerTask extends TimerTask {
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
    }
}
