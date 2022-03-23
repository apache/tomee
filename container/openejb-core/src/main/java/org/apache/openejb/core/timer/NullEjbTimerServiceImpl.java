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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;

/**
 * Idempotent EjbTimerServiceImplementation. Used if a Bean does not implement a timeout method or no auto-started timer is configured by annotation or deployment plan.
 * This differs from OpenEJB 2.x behavior, which did not create a TimerService for a bean which did not have a timeout method.
 * There's nothing in the spec which says a timeout-less bean cannot call getTimerService. So, we now have NullEjbTimerServiceImpl, which does not do very much...
 */
public class NullEjbTimerServiceImpl implements EjbTimerService {

    private static final Logger log = Logger.getInstance(LogCategory.TIMER, NullEjbTimerServiceImpl.class);

    public Timer createTimer(final Object primaryKey, final Method timeoutMethod, final ScheduleExpression schedule, final TimerConfig timerConfig) {
        log.error("Null ! TimerService operation not supported for a bean without an ejbTimeout method or auto-started task");
        return null;
    }

    public Timer createTimer(final Object primaryKey, final Method timeoutMethod, final Date initialExpiration, final long intervalDuration, final TimerConfig timerConfig) {
        log.error("Null ! TimerService operation not supported for a bean without an ejbTimeout method  or auto-started task");
        return null;
    }

    public Timer createTimer(final Object primaryKey, final Method timeoutMethod, final Date expiration, final TimerConfig timerConfig) {
        log.error("Null ! TimerService operation not supported for a bean without an ejbTimeout method  or auto-started task");
        return null;
    }

    public Timer createTimer(final Object primaryKey, final Method timeoutMethod, final long initialDuration, final long intervalDuration, final TimerConfig timerConfig) {
        log.error("Null ! TimerService operation not supported for a bean without an ejbTimeout method  or auto-started task");
        return null;
    }

    public Timer createTimer(final Object primaryKey, final Method timeoutMethod, final long duration, final TimerConfig timerConfig) {
        log.error("Null ! TimerService operation not supported for a bean without an ejbTimeout method  or auto-started task");
        return null;
    }

    public Timer getTimer(final long id) {
        log.error("Null ! TimerService operation not supported for a bean without an ejbTimeout method  or auto-started task");
        return null;
    }

    public Collection<Timer> getTimers(final Object primaryKey) {
        log.error("Null ! TimerService operation not supported for a bean without an ejbTimeout method  or auto-started task");
        return null;
    }

    public void start() throws OpenEJBException {
    }

    public void stop() {
    }

    public TimerStore getTimerStore() {
        return null;
    }

    @Override
    public boolean isStarted() {
        return true;
    }
}
