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

import javax.ejb.ScheduleExpression;
import javax.ejb.TimerConfig;

import org.apache.openejb.core.timer.EJBCronTrigger.ParseException;
import org.quartz.Trigger;

/**
 * @version $Rev$ $Date$
 */
public class CalendarTimerData extends TimerData {

    private final ScheduleExpression scheduleExpression;

    public CalendarTimerData(long id, EjbTimerServiceImpl timerService, String deploymentId, Object primaryKey, Method timeoutMethod, TimerConfig timerConfig, ScheduleExpression scheduleExpression) {
        super(id, timerService, deploymentId, primaryKey, timeoutMethod, timerConfig);
        this.scheduleExpression = scheduleExpression;
    }

    @Override
    public TimerType getType() {
        return TimerType.Calendar;
    }

    public ScheduleExpression getSchedule() {
        return scheduleExpression;
    }

    @Override
    public Trigger initializeTrigger() {
        try {
            return new EJBCronTrigger(scheduleExpression);
        } catch (ParseException e) {
            //TODO how to handle the ParseException
            throw new IllegalArgumentException("Fail to parse schedule expression " + scheduleExpression);
        }
    }

    @Override
    public String toString() {
        return TimerType.Calendar.name() + " scheduleExpression = [" + scheduleExpression + "]";
    }
}
