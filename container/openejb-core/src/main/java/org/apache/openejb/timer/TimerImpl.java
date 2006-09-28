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
package org.apache.openejb.timer;

import java.io.Serializable;
import java.util.Date;
import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.management.ObjectName;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.apache.geronimo.timer.WorkInfo;

/**
 * TODO keep track of state so after single-shot firing nothing works.
 *
 * @version $Revision$ $Date$
 */
public class TimerImpl implements Timer {
    private final WorkInfo workInfo;
    private final BasicTimerServiceImpl timerService;
    private final String kernelName;
    private final ObjectName timerSourceName;
    private boolean cancelled = false;

    public TimerImpl(WorkInfo workInfo, BasicTimerServiceImpl timerService, String kernelName, ObjectName timerSourceName) {
        this.workInfo = workInfo;
        this.timerService = timerService;
        this.kernelName = kernelName;
        this.timerSourceName = timerSourceName;
    }

    public void cancel() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        workInfo.getExecutorFeedingTimerTask().cancel();
        cancelled = true;
        try {
            timerService.registerCancelSynchronization(new CancelSynchronization());
        } catch (RollbackException e) {
            throw (IllegalStateException) new IllegalStateException("Transaction is already rolled back").initCause(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public long getTimeRemaining() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        long now = System.currentTimeMillis();
        long then = workInfo.getTime().getTime();
        return then - now;
    }

    public Date getNextTimeout() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        return workInfo.getTime();
    }

    public Serializable getInfo() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        return (Serializable) workInfo.getUserInfo();
    }

    public TimerHandle getHandle() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        return new TimerHandleImpl(workInfo.getId(), kernelName, timerSourceName);
    }

    Object getUserId() {
        return workInfo.getUserId();
    }

    private void checkState() throws NoSuchObjectLocalException {
        if (!TimerState.getTimerState()) {
            throw new IllegalStateException("Timer methods not available");
        }
        if (cancelled) {
            throw new NoSuchObjectLocalException("Timer is cancelled");
        }
    }

    private class CancelSynchronization implements Synchronization {

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
            } else if (status == Status.STATUS_ROLLEDBACK) {
                cancelled = false;
            }  //else???
        }

    }

}
