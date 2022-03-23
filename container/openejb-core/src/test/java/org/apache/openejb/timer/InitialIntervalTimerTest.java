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

import org.apache.commons.lang3.time.DateUtils;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.NoMoreTimeoutsException;
import jakarta.ejb.NoSuchObjectLocalException;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
@Classes(innerClassesAsBean = true)
public class InitialIntervalTimerTest {

    @EJB
    private TimerWithDelay bean;

    @EJB
    private TimerNeverExpires scheduleBean;

    @Test
    public void test() throws InterruptedException {
        Thread.sleep(5400);
        assertEquals(3, bean.getOk());
    }

    @Test
    public void endNeverExpires() {
        final Calendar cal = Calendar.getInstance();
        final int currentYear = getForSchedule(Calendar.YEAR, cal);
        cal.add(Calendar.SECOND, 10);
        final ScheduleExpression exp = getPreciseScheduleExpression(cal);
        final Date end = DateUtils.setYears(cal.getTime(), cal.get(Calendar.YEAR) - 1);
        exp.end(end);
        exp.year((currentYear) + " - " + (currentYear + 1));

        final Timer timer = scheduleBean.createTimer(exp);
        scheduleBean.passIfNoMoreTimeouts(timer);
    }

    public static ScheduleExpression getPreciseScheduleExpression(final Calendar... cals) {
        Calendar cal = (cals.length == 0) ? Calendar.getInstance() : cals[0];
        return new ScheduleExpression()
            .year(cal.get(Calendar.YEAR)).month(getForSchedule(Calendar.MONTH, cal))
            .dayOfMonth(getForSchedule(Calendar.DAY_OF_MONTH, cal))
            .hour(getForSchedule(Calendar.HOUR_OF_DAY, cal))
            .minute(getForSchedule(Calendar.MINUTE, cal))
            .second(getForSchedule(Calendar.SECOND, cal));
    }

    public static int getForSchedule(final int field, final Calendar... calendars) {
        int result = 0;
        Calendar cal = null;
        if (calendars.length == 0) {
            cal = Calendar.getInstance();
        } else {
            cal = calendars[0];
        }
        result = cal.get(field);
        if (field == Calendar.DAY_OF_WEEK) {
            result--; // 0 and 7 are both Sunday
            if (result == 0) {
                result = (Math.random() < 0.5) ? 0 : 7;
            }
        } else if (field == Calendar.MONTH) {
            result++;
        }
        return result;
    }

    @Singleton
    @Startup
    @Lock(LockType.READ)
    public static class TimerWithDelay {
        @Resource
        private TimerService ts;

        private Timer timer;
        private int ok = 0;

        @PostConstruct
        public void start() {
            timer = ts.createIntervalTimer(3000, 1000, new TimerConfig(System.currentTimeMillis(), false));
        }

        @Timeout
        public void timeout(final Timer timer) {
            final long actual = System.currentTimeMillis() - ((Long) timer.getInfo() + 1000 * ok + 3000);
            assertEquals(0, actual, 500);
            ok++;
        }

        public int getOk() {
            return ok;
        }

        @PreDestroy
        public void stop() {
            timer.cancel();
        }
    }

    @Singleton
    @Startup
    @Lock(LockType.READ)
    public static class TimerNeverExpires {

        @Resource
        private TimerService timerService;

        private int ok = 0;

        @Timeout
        public void timeout(final Timer timer) {
            ok++;
        }

        public Timer createTimer(final ScheduleExpression exp) {
            return timerService.createCalendarTimer(exp, new TimerConfig("TimerNeverExpires", false));
        }

        public String passIfNoMoreTimeouts(final Timer t) {
            String result = "";
            try {
                Date nextTimeout = t.getNextTimeout();
                throw new RuntimeException(
                    "Expecting NoSuchObjectLocalException or NoMoreTimeoutsException "
                    + "when accessing getNextTimeout, but actual " + nextTimeout);

            } catch (final NoMoreTimeoutsException e) {
                result += " Got expected " + e;

            } catch (final NoSuchObjectLocalException e) {
                result += " Got expected " + e;

            }

            try {
                long timeRemaining = t.getTimeRemaining();
                throw new RuntimeException(
                    "Expecting NoSuchObjectLocalException or NoMoreTimeoutsException "
                    + "when accessing getTimeRemaining, but actual " + timeRemaining);

            } catch (final NoMoreTimeoutsException e) {
                result += " Got expected " + e;

            } catch (final NoSuchObjectLocalException e) {
                result += " Got expected " + e;

            }
            return result;
        }


    }
}
