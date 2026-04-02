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

import java.time.DayOfWeek;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

/**
 * Maps {@link Schedule} annotations to the API-provided {@link CronTrigger}.
 * Similar design pattern to {@link org.apache.openejb.core.timer.EJBCronTrigger}
 * which maps EJB {@code @Schedule} to Quartz triggers. Here the API JAR provides
 * the cron parsing — we just bridge the annotation attributes.
 */
public final class ScheduleHelper {

    private ScheduleHelper() {
        // utility
    }

    /**
     * Converts a single {@link Schedule} annotation to a {@link CronTrigger}.
     * If {@link Schedule#cron()} is non-empty, uses the cron expression directly.
     * Otherwise builds the trigger from individual field attributes.
     */
    public static CronTrigger toCronTrigger(final Schedule schedule) {
        final ZoneId zone = schedule.zone().isEmpty()
                ? ZoneId.systemDefault()
                : ZoneId.of(schedule.zone());

        final String cron = schedule.cron();
        if (!cron.isEmpty()) {
            return new CronTrigger(cron, zone);
        }

        final CronTrigger trigger = new CronTrigger(zone);

        if (schedule.months().length > 0) {
            trigger.months(toMonths(schedule.months()));
        }
        if (schedule.daysOfMonth().length > 0) {
            trigger.daysOfMonth(schedule.daysOfMonth());
        }
        if (schedule.daysOfWeek().length > 0) {
            trigger.daysOfWeek(toDaysOfWeek(schedule.daysOfWeek()));
        }
        if (schedule.hours().length > 0) {
            trigger.hours(schedule.hours());
        }
        if (schedule.minutes().length > 0) {
            trigger.minutes(schedule.minutes());
        }
        if (schedule.seconds().length > 0) {
            trigger.seconds(schedule.seconds());
        }

        return trigger;
    }

    /**
     * Converts one or more {@link Schedule} annotations to a {@link ZonedTrigger}.
     * A single schedule returns a potentially wrapped {@link CronTrigger}.
     * Multiple schedules return a {@link CompositeScheduleTrigger} that picks the
     * earliest next run time.
     *
     * <p>The returned trigger includes {@code skipIfLateBy} logic when configured.</p>
     */
    public static ZonedTrigger toTrigger(final Schedule[] schedules) {
        if (schedules.length == 1) {
            return wrapWithSkipIfLate(toCronTrigger(schedules[0]), schedules[0].skipIfLateBy());
        }

        final ZonedTrigger[] triggers = new ZonedTrigger[schedules.length];
        for (int i = 0; i < schedules.length; i++) {
            triggers[i] = wrapWithSkipIfLate(toCronTrigger(schedules[i]), schedules[i].skipIfLateBy());
        }
        return new CompositeScheduleTrigger(triggers);
    }

    private static ZonedTrigger wrapWithSkipIfLate(final CronTrigger trigger, final long skipIfLateBy) {
        if (skipIfLateBy <= 0) {
            return trigger;
        }
        return new SkipIfLateTrigger(trigger, skipIfLateBy);
    }

    private static Month[] toMonths(final Month[] months) {
        return months;
    }

    private static DayOfWeek[] toDaysOfWeek(final DayOfWeek[] days) {
        return days;
    }

    /**
     * Wraps a {@link ZonedTrigger} to skip executions that are late by more than
     * the configured threshold (in seconds). Per the spec, the default is 600 seconds.
     */
    static class SkipIfLateTrigger implements ZonedTrigger {
        private final ZonedTrigger delegate;
        private final long skipIfLateBySeconds;

        SkipIfLateTrigger(final ZonedTrigger delegate, final long skipIfLateBySeconds) {
            this.delegate = delegate;
            this.skipIfLateBySeconds = skipIfLateBySeconds;
        }

        @Override
        public ZonedDateTime getNextRunTime(final LastExecution lastExecution, final ZonedDateTime taskScheduledTime) {
            return delegate.getNextRunTime(lastExecution, taskScheduledTime);
        }

        @Override
        public ZoneId getZoneId() {
            return delegate.getZoneId();
        }

        @Override
        public boolean skipRun(final LastExecution lastExecution, final ZonedDateTime scheduledRunTime) {
            if (delegate.skipRun(lastExecution, scheduledRunTime)) {
                return true;
            }

            final ZonedDateTime now = ZonedDateTime.now(getZoneId());
            final long lateBySeconds = java.time.Duration.between(scheduledRunTime, now).getSeconds();
            return lateBySeconds > skipIfLateBySeconds;
        }
    }

    /**
     * Combines multiple {@link ZonedTrigger} instances, picking the earliest
     * next run time from all delegates. Used when multiple {@link Schedule}
     * annotations are present on a single method.
     */
    static class CompositeScheduleTrigger implements ZonedTrigger {
        private final ZonedTrigger[] delegates;

        CompositeScheduleTrigger(final ZonedTrigger[] delegates) {
            this.delegates = Arrays.copyOf(delegates, delegates.length);
        }

        @Override
        public ZonedDateTime getNextRunTime(final LastExecution lastExecution, final ZonedDateTime taskScheduledTime) {
            ZonedDateTime earliest = null;
            for (final ZonedTrigger delegate : delegates) {
                final ZonedDateTime next = delegate.getNextRunTime(lastExecution, taskScheduledTime);
                if (next != null && (earliest == null || next.isBefore(earliest))) {
                    earliest = next;
                }
            }
            return earliest;
        }

        @Override
        public ZoneId getZoneId() {
            return delegates[0].getZoneId();
        }

        @Override
        public boolean skipRun(final LastExecution lastExecution, final ZonedDateTime scheduledRunTime) {
            // skip only if ALL delegates would skip
            for (final ZonedTrigger delegate : delegates) {
                if (!delegate.skipRun(lastExecution, scheduledRunTime)) {
                    return false;
                }
            }
            return true;
        }
    }
}
