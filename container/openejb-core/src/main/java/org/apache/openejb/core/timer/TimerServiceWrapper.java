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
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import org.apache.openejb.BeanContext;
import org.apache.openejb.MethodContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class TimerServiceWrapper implements TimerService {
    
    private static final Logger log = Logger.getInstance(LogCategory.TIMER, TimerServiceWrapper.class);

    public TimerServiceWrapper() {
    }

    public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createTimer(initialExpiration, intervalDuration, info);
    }

    public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createTimer(expiration, info);
    }

    public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createTimer(initialDuration, intervalDuration, info);
    }

    public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createTimer(duration, info);
    }

    public Collection<Timer> getTimers() throws IllegalStateException, EJBException {
        return getTimerService().getTimers();
    }

    public Timer createSingleActionTimer(long l, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createSingleActionTimer(l, timerConfig);
    }

    public Timer createSingleActionTimer(Date date, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createSingleActionTimer(date, timerConfig);
    }

    public Timer createIntervalTimer(long l, long l1, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createIntervalTimer(l, l1, timerConfig);
    }

    public Timer createIntervalTimer(Date date, long l, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createIntervalTimer(date, l, timerConfig);
    }

    public Timer createCalendarTimer(ScheduleExpression scheduleExpression) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createCalendarTimer(scheduleExpression);
    }

    public Timer createCalendarTimer(ScheduleExpression scheduleExpression, TimerConfig timerConfig) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createCalendarTimer(scheduleExpression, timerConfig);
    }

    private TimerService getTimerService() throws IllegalStateException {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        BeanContext beanContext = threadContext.getBeanContext();
        EjbTimerService timerService = beanContext.getEjbTimerService();
        if (timerService == null) {
            throw new IllegalStateException("This ejb does not support timers " + beanContext.getDeploymentID());
        } else if(beanContext.getEjbTimeout() == null) {
            
            boolean hasSchedules = false;
            
            for (Iterator<Map.Entry<Method, MethodContext>> it = beanContext.iteratorMethodContext(); it.hasNext();) {
                Map.Entry<Method, MethodContext> entry = it.next();
                MethodContext methodContext = entry.getValue();
                if (methodContext.getSchedules().size() > 0) {
                    hasSchedules = true;
                }
            }
            
            if (!hasSchedules) {
                log.error("This ejb does not support timers " + beanContext.getDeploymentID() + " due to no timeout method nor schedules in methodContext is configured");
            }
            
        }
        return new TimerServiceImpl(timerService, threadContext.getPrimaryKey(), beanContext.getEjbTimeout());
    }
}
