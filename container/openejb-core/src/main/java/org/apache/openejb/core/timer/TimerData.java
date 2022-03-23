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
import org.apache.openejb.MethodContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.quartz.Scheduler;
import org.apache.openejb.quartz.SchedulerException;
import org.apache.openejb.quartz.Trigger;
import org.apache.openejb.quartz.TriggerKey;
import org.apache.openejb.quartz.impl.triggers.AbstractTrigger;

import jakarta.ejb.EJBException;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transaction;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public abstract class TimerData implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String OPEN_EJB_TIMEOUT_TRIGGER_NAME_PREFIX = "OPEN_EJB_TIMEOUT_TRIGGER_";
    public static final String OPEN_EJB_TIMEOUT_TRIGGER_GROUP_NAME = "OPEN_EJB_TIMEOUT_TRIGGER_GROUP";

    private static final Logger log = Logger.getInstance(LogCategory.TIMER, "org.apache.openejb.util.resources");
    private long id;
    private EjbTimerServiceImpl timerService;
    private String deploymentId;
    private Object primaryKey;
    private Method timeoutMethod;

    private Object info;
    private boolean persistent;
    private boolean autoScheduled;

    protected AbstractTrigger<?> trigger;

    protected Scheduler scheduler;

    public void setScheduler(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    // EJB Timer object given to user code
    private Timer timer;

    /**
     * Is this a new timer?  A new timer must be scheduled with the java.util.Timer
     * when the transaction commits.
     */
    private boolean newTimer;

    /**
     * Has this timer been cancelled? A canceled timer must be rescheduled with the
     * java.util.Timer if the transaction is rolled back
     */
    private boolean cancelled;

    private boolean stopped;

    /**
     * Has this timer been registered with the transaction for callbacks?  We remember
     * when we are registered to avoid multiple registrations.
     */
    private boolean synchronizationRegistered;

    /**
     * Used to set timer to expired state after the timeout callback method has been successfully invoked.
     * only apply to
     * 1, Single action timer
     * 2, Calendar timer there are no future timeout.
     */
    private boolean expired;

    public TimerData(final long id,
                     final EjbTimerServiceImpl timerService,
                     final String deploymentId,
                     final Object primaryKey,
                     final Method timeoutMethod,
                     final TimerConfig timerConfig) {
        this.id = id;
        this.timerService = timerService;
        this.deploymentId = deploymentId;
        this.primaryKey = primaryKey;
        this.info = timerConfig == null ? null : timerConfig.getInfo();
        this.persistent = timerConfig == null || timerConfig.isPersistent();
        this.timer = new TimerImpl(this);
        this.timeoutMethod = timeoutMethod;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        doWriteObject(out);
    }

    protected void doWriteObject(final ObjectOutputStream out) throws IOException {
        out.writeLong(id);
        out.writeUTF(deploymentId);
        out.writeBoolean(persistent);
        out.writeBoolean(autoScheduled);
        out.writeObject(timer);
        out.writeObject(primaryKey);
        out.writeObject(timerService);
        out.writeObject(info);
        out.writeObject(trigger);
        out.writeUTF(timeoutMethod.getName());
    }

    private void readObject(final ObjectInputStream in) throws IOException {
        doReadObject(in);
    }

    protected void doReadObject(final ObjectInputStream in) throws IOException {
        id = in.readLong();
        deploymentId = in.readUTF();
        persistent = in.readBoolean();
        autoScheduled = in.readBoolean();

        try {
            timer = (Timer) in.readObject();
            primaryKey = in.readObject();
            timerService = (EjbTimerServiceImpl) in.readObject();
            info = in.readObject();
            trigger = AbstractTrigger.class.cast(in.readObject());
        } catch (final ClassNotFoundException e) {
            throw new IOException(e);
        }

        final String mtd = in.readUTF();
        final BeanContext beanContext = SystemInstance.get().getComponent(ContainerSystem.class).getBeanContext(deploymentId);
        scheduler = timerService.getScheduler();
        for (final Iterator<Map.Entry<Method, MethodContext>> it = beanContext.iteratorMethodContext(); it.hasNext(); ) {
            final MethodContext methodContext = it.next().getValue();
            /* this doesn't work in all cases
            if (methodContext.getSchedules().isEmpty()) {
                continue;
            }
            */

            final Method method = methodContext.getBeanMethod();
            if (method != null && method.getName().equals(mtd)) { // maybe we should check parameters too
                setTimeoutMethod(method);
                break;
            }
        }
    }

    public void stop() {
        if (trigger != null) {
            try {
                final Scheduler s = timerService.getScheduler();

                if (!s.isShutdown()) {
                    if (!isPersistent()) {
                        s.unscheduleJob(trigger.getKey());
                    } else {
                        s.pauseTrigger(trigger.getKey());
                    }
                }
            } catch (final SchedulerException e) {
                throw new EJBException("fail to cancel the timer", e);
            }
        }
        cancelled = true;
        stopped = true;
    }

    public long getId() {
        return id;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public Object getInfo() {
        return info;
    }

    public Timer getTimer() {
        return timer;
    }

    public boolean isNewTimer() {
        return newTimer;
    }

    public void newTimer() {
        //Initialize the Quartz Trigger
        try {
            trigger = initializeTrigger();
            trigger.computeFirstFireTime(null);
            trigger.setGroup(OPEN_EJB_TIMEOUT_TRIGGER_GROUP_NAME);
            trigger.setName(OPEN_EJB_TIMEOUT_TRIGGER_NAME_PREFIX + deploymentId + "_" + id);
            newTimer = true;

            registerTimerDataSynchronization();

        } catch (final TimerExpiredException e) {
            setExpired(true);
            log.warning("Timer " + trigger + " is expired and will never trigger.");

        } catch (final TimerStoreException e) {
            throw new EJBException("Failed to register new timer data synchronization", e);
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        if (stopped) {
            return;
        }

        timerService.cancelled(TimerData.this);
        if (trigger != null) {
            try {
                final Scheduler s = timerService.getScheduler();

                if (!s.isShutdown()) {
                    s.unscheduleJob(trigger.getKey());
                }
            } catch (final SchedulerException e) {
                throw new EJBException("fail to cancel the timer", e);
            }
        }
        cancelled = true;
        try {
            registerTimerDataSynchronization();
        } catch (final TimerStoreException e) {
            throw new EJBException("Failed to register timer data synchronization on cancel", e);
        }
    }

    private void setTimeoutMethod(final Method timeoutMethod) {
        this.timeoutMethod = timeoutMethod;
    }

    public Method getTimeoutMethod() {
        return timeoutMethod;
    }

    private void transactionComplete(final boolean committed) throws TimerStoreException {
        if (newTimer) {
            // you are only a new timer once no matter what
            newTimer = false;

            // if our new timer was not canceled and the transaction committed
            if (!isCancelled() && committed) {
                // schedule the timer with the java.util.Timer
                timerService.schedule(TimerData.this);
            }
        } else {
            // if the tx was rolled back, reschedule the timer with the java.util.Timer
            if (!committed) {
                cancelled = false;
                timerService.addTimerData(TimerData.this);
                timerService.schedule(TimerData.this);
            }
        }
    }

    private void registerTimerDataSynchronization() throws TimerStoreException {
        if (synchronizationRegistered) {
            return;
        }

        try {
            final Transaction transaction = timerService.getTransactionManager().getTransaction();
            final int status = transaction == null ? Status.STATUS_NO_TRANSACTION : transaction.getStatus();

            if (transaction != null && status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK) {
                transaction.registerSynchronization(new TimerDataSynchronization());
                synchronizationRegistered = true;
                return;
            }
        } catch (final Exception e) {
            log.warning("Unable to register timer data transaction synchronization", e);
        }

        // there either wasn't a transaction or registration failed... call transactionComplete directly
        transactionComplete(true);
    }

    public boolean isStopped() {
        return stopped;
    }

    private class TimerDataSynchronization implements Synchronization {

        @Override
        public void beforeCompletion() {
        }

        @Override
        public void afterCompletion(final int status) {
            synchronizationRegistered = false;
            try {
                transactionComplete(status == Status.STATUS_COMMITTED);
            } catch (final TimerStoreException e) {
                throw new EJBException("Failed on afterCompletion", e);
            }
        }
    }

    public boolean isPersistent() {
        return persistent;
    }

    public Trigger getTrigger() {

        if (scheduler != null) {
            try {
                final TriggerKey key = new TriggerKey(trigger.getName(), trigger.getGroup());
                if (scheduler.checkExists(key)) {
                    return scheduler.getTrigger(key);
                }
            } catch (final SchedulerException e) {
                log.warning(e.getLocalizedMessage(), e);
                return null;
            }
        }

        return trigger;
    }

    public Date getNextTimeout() {

        try {
            // give the trigger 1 ms to init itself to set correct nextTimeout value.
            Thread.sleep(1);
        } catch (final InterruptedException e) {
            log.warning("Interrupted exception when waiting 1ms for the trigger to init", e);
        }

        Date nextTimeout = null;

        if (getTrigger() != null) {

            nextTimeout = getTrigger().getNextFireTime();
        }

        return nextTimeout;
    }

    public long getTimeRemaining() {
        final Date nextTimeout = getNextTimeout();
        return nextTimeout.getTime() - System.currentTimeMillis();
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(final boolean expired) {
        this.expired = expired;
    }

    public abstract TimerType getType();

    protected abstract AbstractTrigger<?> initializeTrigger();
}
