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

import java.lang.reflect.Method;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

public abstract class TimerData {

    public static final String OPEN_EJB_TIMEOUT_TRIGGER_NAME_PREFIX = "OPEN_EJB_TIMEOUT_TRIGGER_";
    public static final String OPEN_EJB_TIMEOUT_TRIGGER_GROUP_NAME = "OPEN_EJB_TIMEOUT_TRIGGER_GROUP";

    private static final Logger log = Logger.getInstance(LogCategory.TIMER, "org.apache.openejb.util.resources");
    private final long id;
    final EjbTimerServiceImpl timerService;
    private final String deploymentId;
    private final Object primaryKey;
    private final Method timeoutMethod;

    private final Object info;
    private boolean persistent;

    protected Trigger trigger;
    
    protected Scheduler scheduler;

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    // EJB Timer object given to user code
    private final Timer timer;


    /**
     * Is this a new timer?  A new timer must be scheduled with the java.util.Timer
     * when the transaction commits.
     */
    private boolean newTimer = false;

    /**
     * Has this timer been cancelled? A canceled timer must be rescheduled with the
     * java.util.Timer if the transaction is rolled back
     */
    private boolean cancelled = false;

    /**
     * Has this timer been registered with the transaction for callbacks?  We remember
     * when we are registered to avoid multiple registrations.
     */
    private boolean synchronizationRegistered = false;
    
    /**
     *  Used to set timer to expired state after the timeout callback method has been successfully invoked.
     *  only apply to 
     *  1, Single action timer
     *  2, Calendar timer there are no future timeout.
     */
    private boolean expired;    

    public TimerData(long id, EjbTimerServiceImpl timerService, String deploymentId, Object primaryKey, Method timeoutMethod, TimerConfig timerConfig) {
        this.id = id;
        this.timerService = timerService;
        this.deploymentId = deploymentId;
        this.primaryKey = primaryKey;
        this.info = timerConfig == null ? null : timerConfig.getInfo();
        this.persistent = timerConfig == null ? true : timerConfig.isPersistent();
        this.timer = new TimerImpl(this);
        this.timeoutMethod = timeoutMethod;
    }

    public void stop() {
        if (trigger != null) {
            try {
                final Scheduler s = timerService.getScheduler();
                
                if(!s.isShutdown()){                
                    s.unscheduleJob(trigger.getName(), trigger.getGroup());
                }
            } catch (SchedulerException e) {
                throw new EJBException("fail to cancel the timer", e);
            }
        }
        cancelled = true;
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
        trigger = initializeTrigger();
        trigger.computeFirstFireTime(null);
        trigger.setGroup(OPEN_EJB_TIMEOUT_TRIGGER_GROUP_NAME);
        trigger.setName(OPEN_EJB_TIMEOUT_TRIGGER_NAME_PREFIX + deploymentId + "_" + id);
        newTimer = true;
        registerTimerDataSynchronization();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        timerService.cancelled(TimerData.this);
        if (trigger != null) {
            try {
                final Scheduler s = timerService.getScheduler();
                
                if(!s.isShutdown()){
                    s.unscheduleJob(trigger.getName(), trigger.getGroup());
                }
            } catch (SchedulerException e) {
                throw new EJBException("fail to cancel the timer", e);
            }
        }
        cancelled = true;
        registerTimerDataSynchronization();
    }

    public Method getTimeoutMethod() {
        return timeoutMethod;
    }

    private void transactionComplete(boolean committed) {
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

    private void registerTimerDataSynchronization() {
        if (synchronizationRegistered) return;

        try {
            Transaction transaction = timerService.getTransactionManager().getTransaction();
            int status = transaction == null ? Status.STATUS_NO_TRANSACTION : transaction.getStatus();

            if (transaction != null && status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK) {
                transaction.registerSynchronization(new TimerDataSynchronization());
                synchronizationRegistered = true;
                return;
            }
        } catch (Exception e) {
            log.warning("Unable to register timer data transaction synchronization", e);
        }

        // there either wasn't a transaction or registration failed... call transactionComplete directly
        transactionComplete(true);
    }

    private class TimerDataSynchronization implements Synchronization {
        @Override
        public void beforeCompletion() {
        }

        @Override
        public void afterCompletion(int status) {
            synchronizationRegistered = false;
            transactionComplete(status == Status.STATUS_COMMITTED);
        }
    }

    public boolean isPersistent(){
        return persistent;
    }
    
    
    public Trigger getTrigger() {
        
        if (scheduler != null) {
            try {
                if (scheduler.getTrigger(trigger.getName(), trigger.getGroup()) != null) {
                    return scheduler.getTrigger(trigger.getName(), trigger.getGroup());
                }
            } catch (SchedulerException e) {
                return null;
            }
        } 

        return trigger;
    }

    public Date getNextTimeout() {    
        
        try {
            // give the trigger 1 ms to init itself to set correct nextTimeout value.
            Thread.sleep(1);
        } catch (InterruptedException e) {
            log.warning("Interrupted exception when waiting 1ms for the trigger to init", e);
        }
        
        Date nextTimeout = null;
        
        if(getTrigger()!=null){
        
            nextTimeout = getTrigger().getNextFireTime();
        }
        
        return nextTimeout;
    }

    public long getTimeRemaining() {
        Date nextTimeout = getNextTimeout();
        return nextTimeout.getTime() - System.currentTimeMillis();
    }
    
    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired){
        this.expired = expired;
    }

    public abstract TimerType getType();

    protected abstract Trigger initializeTrigger();
}
