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
package org.apache.openejb.timer;

import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Schedule;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import java.util.Calendar;
import java.util.Collection;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class GetTimerTest {
    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(TimerLister.class).localBean();
    }

    @EJB
    private TimerLister bean;

    @Test
    public void test() throws InterruptedException {
        assertEquals(1, bean.timers().size());
        bean.newTimer();
        assertEquals(2, bean.timers().size());
        bean.awaitTimeout();
        assertEquals(2, bean.timers().size());
    }

    @Singleton
    @Startup
    @Lock(LockType.READ)
    public static class TimerLister {
        @Resource
        private TimerService timerService;

        private Timer timer = null;
        private final Semaphore sema = new Semaphore(0);

        @Timeout
        public void timeout(final Timer timer) {
            System.out.println("@Timeout");
            sema.release();
        }

        public void awaitTimeout() {
            try {
                sema.acquire();
            } catch (final InterruptedException e) {
                // no-op
            }
        }

        public Collection<Timer> timers() {
            return timerService.getTimers();
        }

        @PreDestroy
        public void stop() {
            if (timer != null) {
                timer.cancel();
            }
        }

        @Schedule
        public void justToCheckZeroTimersInListAtStartup() {
            // no-op
        }

        public void newTimer() {
            final TimerConfig tc = new TimerConfig("my-timer", true);
            final ScheduleExpression se = new ScheduleExpression();
            final Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 2);
            se.second(calendar.get(Calendar.SECOND) + "/3");
            se.minute("*");
            se.hour("*");
            se.dayOfMonth("*");
            se.dayOfWeek("*");
            se.month("*");
            timer = timerService.createCalendarTimer(se, tc);
        }
    }
}
