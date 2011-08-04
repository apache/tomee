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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

public class TimerServiceImpl implements TimerService {
    private final EjbTimerService ejbTimerService;
    private final Object primaryKey;
    private final Method ejbTimeout;

    public TimerServiceImpl(EjbTimerService ejbTimerService, Object primaryKey, Method ejbTimeout) {
        this.ejbTimerService = ejbTimerService;
        this.primaryKey = primaryKey;
        this.ejbTimeout = ejbTimeout;
    }

    public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return ejbTimerService.createTimer(primaryKey, ejbTimeout, initialExpiration, intervalDuration, new TimerConfig(info, true));
    }

    public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return ejbTimerService.createTimer(primaryKey, ejbTimeout, expiration, new TimerConfig(info, true));
    }

    public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return ejbTimerService.createTimer(primaryKey, ejbTimeout, initialDuration, intervalDuration, new TimerConfig(info, true));
    }

    public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return ejbTimerService.createTimer(primaryKey, ejbTimeout, duration, new TimerConfig(info, true));
    }

    public Collection<Timer> getTimers() throws IllegalStateException, EJBException {
        return ejbTimerService.getTimers(primaryKey);
    }

    public Timer createSingleActionTimer(long duration, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return ejbTimerService.createTimer(primaryKey, ejbTimeout, duration, timerConfig);
    }

    public Timer createSingleActionTimer(Date expiration, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return ejbTimerService.createTimer(primaryKey, ejbTimeout, expiration, timerConfig);
    }

    public Timer createIntervalTimer(long initialDuration, long intervalDuration, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return ejbTimerService.createTimer(primaryKey, ejbTimeout, initialDuration, intervalDuration, timerConfig);
    }

    public Timer createIntervalTimer(Date initialExpiration, long lintervalDuration, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return ejbTimerService.createTimer(primaryKey, ejbTimeout, initialExpiration, lintervalDuration, timerConfig);
    }

    public Timer createCalendarTimer(ScheduleExpression scheduleExpression) throws IllegalArgumentException, IllegalStateException, EJBException {
        
        return ejbTimerService.createTimer(primaryKey, ejbTimeout, copy(scheduleExpression), new TimerConfig(null, true));
    }

    public Timer createCalendarTimer(ScheduleExpression scheduleExpression, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return ejbTimerService.createTimer(primaryKey, ejbTimeout, copy(scheduleExpression), timerConfig);
    }
    
    private ScheduleExpression copy(ScheduleExpression scheduleExpression){
        ScheduleExpression scheduleExpressionCopy = new ScheduleExpression();
        scheduleExpressionCopy.year(scheduleExpression.getYear());
        scheduleExpressionCopy.month(scheduleExpression.getMonth());
        scheduleExpressionCopy.dayOfMonth(scheduleExpression.getDayOfMonth());
        scheduleExpressionCopy.dayOfWeek(scheduleExpression.getDayOfWeek());
        scheduleExpressionCopy.hour(scheduleExpression.getHour());
        scheduleExpressionCopy.minute(scheduleExpression.getMinute());
        scheduleExpressionCopy.second(scheduleExpression.getSecond());
        scheduleExpressionCopy.start(scheduleExpression.getStart());
        scheduleExpressionCopy.end(scheduleExpression.getEnd());
        scheduleExpressionCopy.timezone(scheduleExpression.getTimezone());
        
        return scheduleExpressionCopy;
    }
}
