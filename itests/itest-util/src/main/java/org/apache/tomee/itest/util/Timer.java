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
package org.apache.tomee.itest.util;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;

public class Timer {
    private final long start = System.nanoTime();

    public static Timer start() {
        return new Timer();
    }

    public Time time() {
        return new Time(System.nanoTime() - start);
    }

    public static class Time {
        private final long time;
        private final String description;

        public Time(final long timeInNanoseconds) {
            this.time = timeInNanoseconds;
            final long seconds = NANOSECONDS.toSeconds(this.time);
            final long milliseconds = NANOSECONDS.toMillis(this.time) - SECONDS.toMillis(seconds);
            final long nanoseconds = this.time - SECONDS.toNanos(seconds) - MILLISECONDS.toNanos(milliseconds);
            this.description = String.format("%ss, %sms and %sns", seconds, milliseconds, nanoseconds);
        }

        public long getTime() {
            return time;
        }

        public Time assertLessThan(final long time, final TimeUnit unit) {
            final long expected = unit.toNanos(time);
            final long actual = this.time;
            assertTrue("Actual time: " + description, actual < expected);
            return this;
        }

        public Time assertGreaterThan(final long time, final TimeUnit unit) {
            final long expected = unit.toNanos(time);
            final long actual = this.time;
            assertTrue("Actual time: " + description, actual > expected);
            return this;
        }
    }
}
