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

import jakarta.ejb.EJBException;
import jakarta.ejb.NoMoreTimeoutsException;
import jakarta.ejb.NoSuchObjectLocalException;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerHandle;
import java.io.Serializable;
import java.util.Date;

public class TimerImpl implements Timer, Serializable {
    private final TimerData timerData;

    public TimerImpl(final TimerData timerData) {
        this.timerData = timerData;
    }

    public void cancel() throws IllegalStateException, NoSuchObjectLocalException {
        checkState();
        timerData.cancel();
    }

    public long getTimeRemaining() throws IllegalStateException, NoSuchObjectLocalException {
        checkState();
        final Date nextTimeout = timerData.getNextTimeout();
        if (nextTimeout == null) {
            throw new NoMoreTimeoutsException("The timer has no future timeouts");
        }
        return timerData.getTimeRemaining();
    }

    public Date getNextTimeout() throws IllegalStateException, NoSuchObjectLocalException {
        checkState();

        final Date nextTimeout = timerData.getNextTimeout();
        if (nextTimeout == null) {
            throw new NoMoreTimeoutsException("The timer has no future timeouts");
        }
        return timerData.getNextTimeout();
    }

    public Serializable getInfo() throws IllegalStateException, NoSuchObjectLocalException {
        checkState();
        return (Serializable) timerData.getInfo();
    }

    public TimerHandle getHandle() throws IllegalStateException, NoSuchObjectLocalException {
        checkState();
        if (!timerData.isPersistent()) {
            throw new IllegalStateException("can't getHandle for a non-persistent timer");
        }
        return new TimerHandleImpl(timerData.getId(), timerData.getDeploymentId());
    }

    public ScheduleExpression getSchedule() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        if (timerData.getType() == TimerType.Calendar) {
            return ((CalendarTimerData) timerData).getSchedule();
        }
        throw new IllegalStateException("The target timer is not a calendar-based type ");
    }

    public boolean isPersistent() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        return timerData.isPersistent();
    }

    public boolean isCalendarTimer() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        return timerData.getType() == TimerType.Calendar;
    }

    /**
     * Insure that timer methods can be invoked for the current operation on this Context.
     */
    private void checkState() throws IllegalStateException, NoSuchObjectLocalException {
        /* no more mandatory
        final BeanContext beanContext = ThreadContext.getThreadContext().getBeanContext();
        final BaseContext context = (BaseContext) beanContext.get(EJBContext.class);
        context.doCheck(BaseContext.Call.timerMethod);
        */

        if (timerData.isCancelled() && !timerData.isStopped()) {
            throw new NoSuchObjectLocalException("Timer has been cancelled");
        }

        if (timerData.isExpired()) {
            throw new NoSuchObjectLocalException("The timer has expired");
        }
    }

}
