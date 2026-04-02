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
package org.apache.openejb.cdi.concurrency;

import jakarta.enterprise.concurrent.CronTrigger;
import jakarta.enterprise.concurrent.LastExecution;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.concurrent.ZonedTrigger;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ScheduleHelperTest {

    @Test
    public void cronExpressionTrigger() {
        final Schedule schedule = scheduleWithCron("* * * * *", "");
        final CronTrigger trigger = ScheduleHelper.toCronTrigger(schedule);

        assertNotNull(trigger);
        final ZonedDateTime next = trigger.getNextRunTime(null, ZonedDateTime.now());
        assertNotNull("CronTrigger should compute a next run time", next);
        assertTrue("Next run time should be in the future or now",
                !next.isBefore(ZonedDateTime.now().minusSeconds(1)));
    }

    @Test
    public void cronExpressionWithZone() {
        final Schedule schedule = scheduleWithCron("0 12 * * MON-FRI", "America/New_York");
        final CronTrigger trigger = ScheduleHelper.toCronTrigger(schedule);

        assertNotNull(trigger);
        assertNotNull(trigger.getZoneId());
    }

    @Test
    public void builderStyleTrigger() {
        final Schedule schedule = scheduleWithFields(
                new Month[]{}, new int[]{}, new DayOfWeek[]{},
                new int[]{}, new int[]{0}, new int[]{0},
                "", 600
        );
        final CronTrigger trigger = ScheduleHelper.toCronTrigger(schedule);

        assertNotNull(trigger);
        final ZonedDateTime next = trigger.getNextRunTime(null, ZonedDateTime.now());
        assertNotNull("Builder-style trigger should compute a next run time", next);
    }

    @Test
    public void singleScheduleToTrigger() {
        final Schedule schedule = scheduleWithCron("* * * * *", "");
        final ZonedTrigger trigger = ScheduleHelper.toTrigger(new Schedule[]{schedule});

        assertNotNull(trigger);
        final ZonedDateTime next = trigger.getNextRunTime(null, ZonedDateTime.now());
        assertNotNull(next);
    }

    @Test
    public void compositeSchedulePicksEarliest() {
        // every minute vs every hour — composite should pick the every-minute one
        final Schedule everyMinute = scheduleWithCron("* * * * *", "");
        final Schedule everyHour = scheduleWithCron("0 * * * *", "");

        final ZonedTrigger trigger = ScheduleHelper.toTrigger(new Schedule[]{everyMinute, everyHour});
        assertNotNull(trigger);

        final ZonedDateTime next = trigger.getNextRunTime(null, ZonedDateTime.now());
        assertNotNull("Composite trigger should return a next run time", next);

        // the composite should return the nearest time (every minute)
        final ZonedDateTime everyMinuteNext = new CronTrigger("* * * * *", ZoneId.systemDefault())
                .getNextRunTime(null, ZonedDateTime.now());
        assertTrue("Composite should pick the earlier schedule",
                !next.isAfter(everyMinuteNext.plusSeconds(1)));
    }

    @Test
    public void skipIfLateBySkipsLateExecution() {
        final Schedule schedule = scheduleWithCron("* * * * *", "", 1); // 1 second threshold
        final ZonedTrigger trigger = ScheduleHelper.toTrigger(new Schedule[]{schedule});

        // Simulate a scheduled run time that was 10 seconds ago
        final ZonedDateTime pastScheduledTime = ZonedDateTime.now().minusSeconds(10);
        final boolean shouldSkip = trigger.skipRun(null, pastScheduledTime);
        assertTrue("Should skip execution that is late by more than threshold", shouldSkip);
    }

    @Test
    public void skipIfLateByAllowsOnTimeExecution() {
        final Schedule schedule = scheduleWithCron("* * * * *", "", 600); // 600 second threshold
        final ZonedTrigger trigger = ScheduleHelper.toTrigger(new Schedule[]{schedule});

        // Simulate a scheduled run time that is now
        final ZonedDateTime now = ZonedDateTime.now();
        final boolean shouldSkip = trigger.skipRun(null, now);
        assertFalse("Should not skip execution that is on time", shouldSkip);
    }

    @Test
    public void zeroSkipIfLateByReturnsUnwrappedTrigger() {
        final Schedule schedule = scheduleWithCron("* * * * *", "", 0);
        final ZonedTrigger trigger = ScheduleHelper.toTrigger(new Schedule[]{schedule});

        // With skipIfLateBy=0, should get a plain CronTrigger (no wrapping)
        assertTrue("Zero skipIfLateBy should return CronTrigger directly",
                trigger instanceof CronTrigger);
    }

    @Test
    public void defaultZoneUsedWhenEmpty() {
        final Schedule schedule = scheduleWithCron("* * * * *", "");
        final CronTrigger trigger = ScheduleHelper.toCronTrigger(schedule);

        assertNotNull(trigger.getZoneId());
    }

    // --- Annotation stubs ---

    private static Schedule scheduleWithCron(final String cron, final String zone) {
        return scheduleWithCron(cron, zone, 600);
    }

    private static Schedule scheduleWithCron(final String cron, final String zone, final long skipIfLateBy) {
        return new Schedule() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Schedule.class;
            }

            @Override
            public String cron() {
                return cron;
            }

            @Override
            public Month[] months() {
                return new Month[0];
            }

            @Override
            public int[] daysOfMonth() {
                return new int[0];
            }

            @Override
            public DayOfWeek[] daysOfWeek() {
                return new DayOfWeek[0];
            }

            @Override
            public int[] hours() {
                return new int[0];
            }

            @Override
            public int[] minutes() {
                return new int[0];
            }

            @Override
            public int[] seconds() {
                return new int[0];
            }

            @Override
            public long skipIfLateBy() {
                return skipIfLateBy;
            }

            @Override
            public String zone() {
                return zone;
            }
        };
    }

    private static Schedule scheduleWithFields(final Month[] months, final int[] daysOfMonth,
                                                final DayOfWeek[] daysOfWeek, final int[] hours,
                                                final int[] minutes, final int[] seconds,
                                                final String zone, final long skipIfLateBy) {
        return new Schedule() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Schedule.class;
            }

            @Override
            public String cron() {
                return "";
            }

            @Override
            public Month[] months() {
                return months;
            }

            @Override
            public int[] daysOfMonth() {
                return daysOfMonth;
            }

            @Override
            public DayOfWeek[] daysOfWeek() {
                return daysOfWeek;
            }

            @Override
            public int[] hours() {
                return hours;
            }

            @Override
            public int[] minutes() {
                return minutes;
            }

            @Override
            public int[] seconds() {
                return seconds;
            }

            @Override
            public long skipIfLateBy() {
                return skipIfLateBy;
            }

            @Override
            public String zone() {
                return zone;
            }
        };
    }
}
