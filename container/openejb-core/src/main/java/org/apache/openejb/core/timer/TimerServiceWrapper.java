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
import org.apache.openejb.core.ThreadContext;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import jakarta.ejb.EJBException;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;

public class TimerServiceWrapper implements TimerService {
    public Timer createTimer(final Date initialExpiration, final long intervalDuration, final Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createTimer(initialExpiration, intervalDuration, info);
    }

    public Timer createTimer(final Date expiration, final Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createTimer(expiration, info);
    }

    public Timer createTimer(final long initialDuration, final long intervalDuration, final Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createTimer(initialDuration, intervalDuration, info);
    }

    public Timer createTimer(final long duration, final Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createTimer(duration, info);
    }

    public Collection<Timer> getTimers() throws IllegalStateException, EJBException {
        return getTimerService().getTimers();
    }

    @Override
    public Collection<Timer> getAllTimers() throws IllegalStateException, EJBException {
        return Timers.all();
    }

    public Timer createSingleActionTimer(final long l, final TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createSingleActionTimer(l, timerConfig);
    }

    public Timer createSingleActionTimer(final Date date, final TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createSingleActionTimer(date, timerConfig);
    }

    public Timer createIntervalTimer(final long l, final long l1, final TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createIntervalTimer(l, l1, timerConfig);
    }

    public Timer createIntervalTimer(final Date date, final long l, final TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createIntervalTimer(date, l, timerConfig);
    }

    public Timer createCalendarTimer(final ScheduleExpression scheduleExpression) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createCalendarTimer(scheduleExpression);
    }

    public Timer createCalendarTimer(final ScheduleExpression scheduleExpression, final TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createCalendarTimer(scheduleExpression, timerConfig);
    }

    private TimerService getTimerService() throws IllegalStateException {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext beanContext = threadContext.getBeanContext();
        return Timers.getTimerService(threadContext.getPrimaryKey(), beanContext, false);
    }
}
