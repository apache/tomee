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

import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;

public interface EjbTimerService {
    Timer getTimer(long id);

    Collection<Timer> getTimers(Object primaryKey);

    Timer createTimer(Object primaryKey, Method timeoutMethod, Date initialExpiration, long intervalDuration, TimerConfig timerConfig);

    Timer createTimer(Object primaryKey, Method timeoutMethod, Date expiration, TimerConfig timerConfig);

    Timer createTimer(Object primaryKey, Method timeoutMethod, long initialDuration, long intervalDuration, TimerConfig timerConfig);

    Timer createTimer(Object primaryKey, Method timeoutMethod, long duration, TimerConfig timerConfig);

    Timer createTimer(Object primaryKey, Method timeoutMethod, ScheduleExpression schedule, TimerConfig timerConfig);

    void start() throws OpenEJBException;

    void stop();

    TimerStore getTimerStore();

    boolean isStarted();
}
