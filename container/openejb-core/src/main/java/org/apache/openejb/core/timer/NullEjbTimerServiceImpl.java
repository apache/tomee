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
package org.apache.openejb.core.timer;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;

import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;

import org.apache.openejb.OpenEJBException;

/**
 * Idempotent EjbTimerServiceImplementation. Used if a Bean does not implement a timeout method or no auto-started timer is configured by annotation or deployment plan.
 * This differs from OpenEJB 2.x behavior, which did not create a TimerService for a bean which did not have a timeout method.
 * There's nothing in the spec which says a timeout-less bean cannot call getTimerService. So, we now have NullEjbTimerServiceImpl, which does not do very much...
 */
public class NullEjbTimerServiceImpl implements EjbTimerService {

    public Timer createTimer(Object primaryKey, Method timeoutMethod, ScheduleExpression schedule, TimerConfig timerConfig) {
        throw new IllegalStateException("TimerService operation not supported for a bean without an ejbTimeout method or auto-started task");
    }

    public Timer createTimer(Object primaryKey, Method timeoutMethod, Date initialExpiration, long intervalDuration, TimerConfig timerConfig) {
        throw new IllegalStateException("TimerService operation not supported for a bean without an ejbTimeout method  or auto-started task");
    }

    public Timer createTimer(Object primaryKey, Method timeoutMethod, Date expiration, TimerConfig timerConfig) {
        throw new IllegalStateException("TimerService operation not supported for a bean without an ejbTimeout method  or auto-started task");
    }

    public Timer createTimer(Object primaryKey, Method timeoutMethod, long initialDuration, long intervalDuration, TimerConfig timerConfig) {
        throw new IllegalStateException("TimerService operation not supported for a bean without an ejbTimeout method  or auto-started task");
    }

    public Timer createTimer(Object primaryKey, Method timeoutMethod, long duration, TimerConfig timerConfig) {
        throw new IllegalStateException("TimerService operation not supported for a bean without an ejbTimeout method  or auto-started task");
    }

    public Timer getTimer(long id) {
        throw new IllegalStateException("TimerService operation not supported for a bean without an ejbTimeout method  or auto-started task");
    }

    public Collection<Timer> getTimers(Object primaryKey) {
        throw new IllegalStateException("TimerService operation not supported for a bean without an ejbTimeout method  or auto-started task");
    }

    public void start() throws OpenEJBException {
    }

    public void stop() {
    }

}
