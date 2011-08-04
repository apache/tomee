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
import java.text.DateFormat;
import java.util.Date;

import javax.ejb.TimerConfig;

import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * @version $Rev$ $Date$
 */
public class IntervalTimerData extends TimerData {

    private final long intervalDuration;

    private final Date initialExpiration;

    public IntervalTimerData(long id, EjbTimerServiceImpl timerService, String deploymentId, Object primaryKey, Method timeoutMethod, TimerConfig timerConfig, Date initialExpiration, long intervalDuration) {
        super(id, timerService, deploymentId, primaryKey, timeoutMethod, timerConfig);
        this.initialExpiration = initialExpiration;
        this.intervalDuration = intervalDuration;
    }

    @Override
    public TimerType getType() {
        return TimerType.Interval;
    }

    public long getIntervalDuration() {
        return intervalDuration;
    }

    public Date getInitialExpiration() {
        return initialExpiration;
    }

    @Override
    public Trigger initializeTrigger() {
        SimpleTrigger simpleTrigger = new SimpleTrigger();
        Date startTime = new Date(initialExpiration.getTime() - intervalDuration);
        simpleTrigger.setStartTime(startTime);
        simpleTrigger.setRepeatInterval(intervalDuration);
        simpleTrigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        return simpleTrigger;
    }

    @Override
    public String toString() {
        return TimerType.Interval.name() + " initialExpiration = [" + DateFormat.getDateTimeInstance().format(initialExpiration) + "] intervalDuration = [" + intervalDuration + "]";
    }
}
