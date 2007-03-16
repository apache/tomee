/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.core.timer;

import org.apache.openejb.util.Logger;

import javax.ejb.Timer;
import javax.transaction.Synchronization;
import javax.transaction.Status;
import javax.transaction.Transaction;
import java.util.Date;
import java.util.TimerTask;

public class TimerData {
    private static final Logger log = Logger.getInstance("Timer", "org.apache.openejb.util.resources");
    private final long id;
    private final EjbTimerServiceImpl timerService;
    private final String deploymentId;
    private final Object primaryKey;
    private final Object info;
    private final long intervalDuration;
    private Date expiration;

    // EJB Timer object given to user code
    private final Timer timer;

    // TimerTask object registered with the java.util.timer
    private TimerTask timerTask;

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

    public TimerData(long id, EjbTimerServiceImpl timerService, String deploymentId, Object primaryKey, Object info, Date expiration, long intervalDuration) {
        this.id = id;
        this.timerService = timerService;
        this.deploymentId = deploymentId;
        this.primaryKey = primaryKey;
        this.info = info;
        this.expiration = expiration;
        this.intervalDuration = intervalDuration;
        this.timer = new TimerImpl(this);
    }

    public void stop() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
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

    public Date getExpiration() {
        return expiration;
    }

    public long getIntervalDuration() {
        return intervalDuration;
    }

    public TimerTask getTimerTask() {
        return timerTask;
    }

    public void setTimerTask(TimerTask timerTask) {
        this.timerTask = timerTask;
    }

    public Timer getTimer() {
        return timer;
    }

    public boolean isOneTime() {
        return intervalDuration <= 0;
    }

    void nextTime() {
        if (isOneTime()) {
            throw new IllegalStateException("This is a one-time timerTask");
        }
        expiration = new Date(expiration.getTime() + intervalDuration);
    }

    public boolean isNewTimer() {
        return newTimer;
    }

    public void newTimer() {
        newTimer = true;
        registerTimerDataSynchronization();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        timerService.cancelled(TimerData.this);
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        cancelled = true;
        registerTimerDataSynchronization();
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
        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            synchronizationRegistered = false;
            transactionComplete(status == Status.STATUS_COMMITTED);
        }
    }
}
