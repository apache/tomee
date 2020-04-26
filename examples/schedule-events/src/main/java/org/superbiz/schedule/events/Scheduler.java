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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.schedule.events;

import jakarta.annotation.Resource;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Singleton;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import jakarta.enterprise.inject.spi.BeanManager;
import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * @version $Revision$ $Date$
 */
@Singleton
@Lock(LockType.READ)
public class Scheduler {

    @Resource
    private TimerService timerService;

    @Resource
    private BeanManager beanManager;

    public void scheduleEvent(ScheduleExpression schedule, Object event, Annotation... qualifiers) {

        timerService.createCalendarTimer(schedule, new TimerConfig(new EventConfig(event, qualifiers), false));
    }

    @Timeout
    private void timeout(Timer timer) {
        final EventConfig config = (EventConfig) timer.getInfo();

        beanManager.fireEvent(config.getEvent(), config.getQualifiers());
    }

    // Doesn't actually need to be serializable, just has to implement it
    private final class EventConfig implements Serializable {

        private final Object event;
        private final Annotation[] qualifiers;

        private EventConfig(Object event, Annotation[] qualifiers) {
            this.event = event;
            this.qualifiers = qualifiers;
        }

        public Object getEvent() {
            return event;
        }

        public Annotation[] getQualifiers() {
            return qualifiers;
        }
    }
}
