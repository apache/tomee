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
package org.apache.openejb.observer;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class EventSpeedTest extends Assert {

    private static final String FORMAT = "%10s %5s %5s %3.0f%%";

    @Test
    public void test() throws Exception {

        final long start = System.nanoTime();
        long events = 0;

        System.out.println(String.format(FORMAT, "Events", "Obsvs", "Avg", 0d));
        Stats previous = null;
        for (int i = 1; i < (64 / 4 + 1); i *= 2) {
            final Stats stats = time(i);

            events += stats.getEvents();

            System.out.println(stats.compare(previous));
            previous = stats;
        }

        final long elapsed = System.nanoTime() - start;
        final long seconds = TimeUnit.NANOSECONDS.toSeconds(elapsed);
        System.out.printf("%n  %s million events in %s seconds%n", events / 1000000, seconds);

        assertTrue(seconds < 60);
    }

    private Stats time(final int observerCount) {
        final ObserverManager observers = new ObserverManager();
        for (int i = observerCount; i > 0; i--) {
            observers.addObserver(new One());
            observers.addObserver(new Two());
            observers.addObserver(new Three());
            observers.addObserver(new Four());
        }

        final long start = System.nanoTime();
        final int max = 5000000;
        for (int i = max; i > 0; i--) {
            observers.fireEvent("");
            observers.fireEvent(i);
        }
        final long total = System.nanoTime() - start;

        return new Stats(total, max * 2, observerCount * 4);
    }

    public static class Stats {
        private final long total;
        private final long events;
        private final long observers;

        public Stats(final long total, final long events, final long observers) {
            this.total = total;
            this.events = events;
            this.observers = observers;
        }

        public long eventAverage() {
            return total / events;
        }

        public long observerAverage() {
            return total / observers;
        }

        public long getTotal() {
            return total;
        }

        public long getEvents() {
            return events;
        }

        public long getObservers() {
            return observers;
        }

        @Override
        public String toString() {
            return String.format("%10s %10s %10s %10s", events, observers, eventAverage(), observerAverage());
        }

        public String compare(final Stats previous) {
            final double change = change(previous);

            return String.format(FORMAT, events, observers, eventAverage(), change);
        }

        private double change(final Stats previous) {
            if (previous == null) return 0;
            final double thisAverage = this.eventAverage();
            final double thatAverage = previous.eventAverage();
            return (thisAverage / thatAverage * 100) - 100;
        }
    }

    public static class One {
        public void observe(@Observes final Object event) {
        }

        public void observe(@Observes final Color event) {
        }

        public void observe(@Observes final Green event) {
        }

        public void observe(@Observes final Emerald event) {
        }

        public void observe(@Observes final Integer event) {
        }
    }

    public static class Two {
        public void observe(@Observes final Color event) {
        }

        public void observe(@Observes final Green event) {
        }
    }

    public static class Three {
        public void observe(@Observes final Green event) {
        }

        public void observe(@Observes final Emerald event) {
        }

        public void observe(@Observes final Integer event) {
        }
    }

    public static class Four {
        public void observe(@Observes final Object event) {
//            System.out.println(event);
        }
    }

    public static class Color {
    }

    public static class Green extends Color {
    }

    public static class Emerald extends Green {
    }
}
