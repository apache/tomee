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

import org.apache.openejb.quartz.SimpleTrigger;
import org.apache.openejb.quartz.impl.triggers.AbstractTrigger;
import org.apache.openejb.quartz.impl.triggers.SimpleTriggerImpl;

import jakarta.ejb.TimerConfig;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;

/**
 * @version $Rev$ $Date$
 */
public class IntervalTimerData extends TimerData {
    private static final long serialVersionUID = 1L;

    private final long intervalDuration;

    private final Date initialExpiration;

    public IntervalTimerData(final long id, final EjbTimerServiceImpl timerService, final String deploymentId, final Object primaryKey, final Method timeoutMethod, final TimerConfig timerConfig, final Date initialExpiration, final long intervalDuration) {
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
    public AbstractTrigger<?> initializeTrigger() {
        final SimpleTriggerImpl simpleTrigger = new SimpleTriggerImpl();
        final Date startTime = new Date(initialExpiration.getTime());
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
