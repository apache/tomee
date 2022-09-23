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
package org.apache.openejb.util;

import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CachedSupplierTest {

    /**
     * Supplier returns a valid result immediately and there
     * are no delays on the first get.
     *
     * We also assert that calling get multiple times on the
     * CachedSupplier return the cached result and do not get
     * updated results before the refresh occurs.
     */
    @Test
    public void happyPath() {
        final AtomicInteger count = new AtomicInteger();
        final Supplier<Integer> supplier = count::incrementAndGet;
        final CachedSupplier<Integer> cached = CachedSupplier.of(supplier);

        Runner.threads(100)
                .run(() -> assertEquals(1, (int) cached.get()))
                .assertNoExceptions()
                .assertTimesLessThan(5, MILLISECONDS);

        // Assert the supplier was not called more than once
        assertEquals(1, count.get());
    }

    /**
     * Supplier does not immediately return an initial instance, so we
     * block till one is available. We assert that we blocked until get
     * a valid result and no timeout or null is returned.
     */
    @Test
    public void delayedInitialization() {
        final CountDownLatch causeSomeDelays = new CountDownLatch(1);
        final AtomicInteger count = new AtomicInteger();
        final Supplier<Integer> supplier = () -> {
            await(causeSomeDelays);
            sleep(111);
            return count.incrementAndGet();
        };
        final CachedSupplier<Integer> cached = CachedSupplier.of(supplier);

        final Runner runner = Runner.threads(100);

        // Run and expect at least 100 ms of delays
        runner.pre(causeSomeDelays::countDown)
                .run(() -> assertEquals(1, (int) cached.get()))
                .assertNoExceptions()
                .assertTimesGreaterThan(100, MILLISECONDS)
                .assertTimesLessThan(200, MILLISECONDS);

        // Everything is cached now, so runs should be quick
        runner.pre(null)
                .run(() -> assertEquals(1, (int) cached.get()))
                .assertNoExceptions()
                .assertTimesLessThan(5, MILLISECONDS);

        // Assert the supplier was not called more than once
        assertEquals(1, count.get());
    }

    /**
     * Supplier does not immediately return an initial instance
     * and the timeout is reached.  We assert a TimeoutException
     * is thrown.  We also assert that when the Supplier eventually
     * does return a valid result we no longer get a TimeoutException
     * or any blocking.
     */
    @Test
    public void delayedInitializationTimeout() throws InterruptedException {
        final CountDownLatch causeSomeDelays = new CountDownLatch(1);
        final CountDownLatch nearlyThere = new CountDownLatch(1);
        final AtomicInteger count = new AtomicInteger();
        final Supplier<Integer> supplier = () -> {
            await(causeSomeDelays);
            sleep(150);
            nearlyThere.countDown();
            sleep(50);
            try {
                return count.incrementAndGet();
            } finally {
                nearlyThere.countDown();
            }
        };

        final CachedSupplier<Integer> cached = CachedSupplier.builder(supplier)
                .accessTimeout(100, MILLISECONDS)
                .build();

        final Runner runner = Runner.threads(100);

        runner.pre(causeSomeDelays::countDown)
                .run(() -> assertEquals(1, (int) cached.get()))
                .assertExceptions(CachedSupplier.TimeoutException.class)
                .assertTimesGreaterThan(99, MILLISECONDS)
                .assertTimesLessThan(150, MILLISECONDS);

        // Wait for the supplier to get near completion
        assertTrue(nearlyThere.await(1, MINUTES));

        runner.pre(null);

        // Calls should now block a bit, but ultimately succeed with no issues
        runner.run(() -> assertEquals(1, (int) cached.get()))
                .assertNoExceptions()
                .assertTimesGreaterThan(30, MILLISECONDS);

        // Calls should now succeed with no delay
        runner.run(() -> assertEquals(1, (int) cached.get()))
                .assertNoExceptions()
                .assertTimesLessThan(5, MILLISECONDS);

        // Assert the supplier was not called more than once
        assertEquals(1, count.get());
    }

    /**
     * Supplier returns null on the first three calls to get.  On the
     * fourth retry a valid result is returned from get.  We assert
     * the number of times the supplier get is called as well as the
     * time between each call to ensure exponential backoff is working
     */
    @Test
    public void initializationRetryNull() {
        final Long[] calls = new Long[10];
        final AtomicInteger count = new AtomicInteger();
        final Supplier<Integer> supplier = () -> {
            final int i = count.incrementAndGet();
            if (i < calls.length) {
                calls[i] = System.nanoTime();
            }

            // Return null for the first three calls
            // Then return the actual value
            return i < 4 ? null : i;
        };

        final CachedSupplier<Integer> cached = CachedSupplier.builder(supplier)
                .initialRetryDelay(500, MILLISECONDS)
                .accessTimeout(1, MINUTES)
                .build();

        final Runner runner = Runner.threads(100);

        runner.run(() -> assertEquals(4, (int) cached.get()))
                .assertNoExceptions();

        final Long[] tries = Stream.of(calls)
                .filter(Objects::nonNull)
                .toArray(Long[]::new);

        assertEquals(4, tries.length);
        assertEquals(4, count.get());

        long first = NANOSECONDS.toSeconds(tries[1] - tries[0]);
        long second = NANOSECONDS.toSeconds(tries[2] - tries[1]);
        long third = NANOSECONDS.toSeconds(tries[3] - tries[2]);

        assertEquals(1, first);
        assertEquals(2, second);
        assertEquals(4, third);
    }

    /**
     * Supplier returns null on the first three calls to get.  On the
     * fourth retry a valid result is returned from get.  We assert
     * the number of times the supplier get is called as well as the
     * time between each call to ensure exponential backoff is working
     */
    @Test
    public void initializationRetryException() {
        final Long[] calls = new Long[10];
        final AtomicInteger count = new AtomicInteger();
        final Supplier<Integer> supplier = () -> {
            final int i = count.incrementAndGet();
            if (i < calls.length) {
                calls[i] = System.nanoTime();
            }

            // Throw an exception for the first three calls
            // Then return the actual value
            if (i < 4) {
                throw new RuntimeException();
            }

            return i;
        };

        final CachedSupplier<Integer> cached = CachedSupplier.builder(supplier)
                .initialRetryDelay(500, MILLISECONDS)
                .accessTimeout(1, MINUTES)
                .build();

        final Runner runner = Runner.threads(100);

        runner.run(() -> assertEquals(4, (int) cached.get()))
                .assertNoExceptions();

        final Long[] tries = Stream.of(calls)
                .filter(Objects::nonNull)
                .toArray(Long[]::new);

        assertEquals(4, tries.length);
        assertEquals(4, count.get());

        long first = NANOSECONDS.toSeconds(tries[1] - tries[0]);
        long second = NANOSECONDS.toSeconds(tries[2] - tries[1]);
        long third = NANOSECONDS.toSeconds(tries[3] - tries[2]);

        assertEquals(1, first);
        assertEquals(2, second);
        assertEquals(4, third);
    }

    /**
     * Supplier returns null repeatedly on all initialization attempts.
     * We assert that when the max retry time is reached all subsequent
     * retries are at that same time interval and do not continue increasing
     * exponentially.
     */
    @Test
    public void initializationRetryTillMax() {
        final Long[] calls = new Long[10];
        final AtomicInteger count = new AtomicInteger();
        final Supplier<Integer> supplier = () -> {
            final int i = count.incrementAndGet();
            if (i < calls.length) {
                calls[i] = System.nanoTime();
            }

            // Return null for the first three calls
            // Then return the actual value
            return i < 7 ? null : i;
        };

        final CachedSupplier<Integer> cached = CachedSupplier.builder(supplier)
                .initialRetryDelay(500, MILLISECONDS)
                .maxRetryDelay(10, SECONDS)
                .accessTimeout(1, MINUTES)
                .build();

        final Runner runner = Runner.threads(100);

        runner.run(() -> assertEquals(7, (int) cached.get()))
                .assertNoExceptions();

        final Long[] tries = Stream.of(calls)
                .filter(Objects::nonNull)
                .toArray(Long[]::new);

        assertEquals(7, tries.length);
        assertEquals(7, count.get());

        long first = NANOSECONDS.toSeconds(tries[1] - tries[0]);
        long second = NANOSECONDS.toSeconds(tries[2] - tries[1]);
        long third = NANOSECONDS.toSeconds(tries[3] - tries[2]);
        long fourth = NANOSECONDS.toSeconds(tries[4] - tries[3]);
        long fifth = NANOSECONDS.toSeconds(tries[5] - tries[4]);
        long sixth = NANOSECONDS.toSeconds(tries[6] - tries[5]);

        assertEquals(1, first);
        assertEquals(2, second);
        assertEquals(4, third);
        assertEquals(8, fourth);
        assertEquals(10, fifth);
        assertEquals(10, sixth);
    }

    /**
     * Suppler returns a valid result on initialization.  We expect that even
     * when we are not actively calling get() the value will be refreshed
     * according to the refreshInterval.  We wait for at least 3 refreshes
     * to occur and assert the value we get is the most recent value returned
     * from the supplier.  We intentionally check for this expected value
     * while the refresh is currently executing for the fourth time.  We do
     * that to ensure that there is no time values are null, even when we're
     * concurrently trying to refresh it in the background.
     */
    @Test
    public void refreshReliablyCalled() {
        final CountDownLatch thirdCall = new CountDownLatch(1);

        final Long[] calls = new Long[10];
        final AtomicInteger count = new AtomicInteger();
        final Supplier<Integer> supplier = () -> {
            final int i = count.incrementAndGet();
            if (i < calls.length) {
                calls[i] = System.nanoTime();
            }
            if (i == 3) {
                thirdCall.countDown();
            }
            return i;
        };

        final CachedSupplier<Integer> cached = CachedSupplier.builder(supplier)
                .refreshInterval(1, SECONDS)
                .accessTimeout(1, MINUTES)
                .build();

        final Runner runner = Runner.threads(100);

        runner.run(() -> assertEquals(1, (int) cached.get()))
                .assertNoExceptions();

        await(thirdCall);

        runner.run(() -> assertEquals(3, (int) cached.get()))
                .assertNoExceptions();

        // Now loop with our 100 threads till each reaches 6
        runner.run(() -> {
                    int previous = 3;
                    while (true) {
                        final Integer value = cached.get();

                        assertNotNull(value);

                        if (value == previous) {
                            continue;
                        } else if (value == previous + 1) {
                            previous = value;
                        } else {
                            fail("Unexpected value " + value + ", previous was " + previous);
                        }
                        if (value == 6) {
                            return;
                        }
                        assertTrue(value < 7);
                    }
                })
                .assertNoExceptions();

        final Long[] tries = Stream.of(calls)
                .filter(Objects::nonNull)
                .toArray(Long[]::new);

        assertEquals(6, tries.length);
        assertEquals(6, count.get());

        long first = NANOSECONDS.toMillis(tries[1] - tries[0]);
        long second = NANOSECONDS.toMillis(tries[2] - tries[1]);
        long third = NANOSECONDS.toMillis(tries[3] - tries[2]);
        long fourth = NANOSECONDS.toMillis(tries[4] - tries[3]);
        long fifth = NANOSECONDS.toMillis(tries[5] - tries[4]);

        assertRange(first, 900, 1100);
        assertRange(second, 900, 1100);
        assertRange(third, 900, 1100);
        assertRange(fourth, 900, 1100);
        assertRange(fifth, 900, 1100);
    }

    /**
     * On the first refresh the Supplier returns null indicating there is
     * no valid replacement.  We assert that the previous valid value is
     * still in use.
     */
    @Test
    public void refreshFailedWithNull() {
        final AtomicInteger count = new AtomicInteger();
        final Supplier<Integer> supplier = () -> {
            final int i = count.incrementAndGet();
            if (i == 2) return null;
            if (i == 3) return null;
            return i;
        };

        final CachedSupplier<Integer> cached = CachedSupplier.builder(supplier)
                .refreshInterval(1, SECONDS)
                .accessTimeout(1, MINUTES)
                .build();

        final Runner runner = Runner.threads(100);

        // We should see the value of 1 initially
        runner.run(() -> assertEquals(1, (int) cached.get()))
                .assertNoExceptions();

        // Values 2 and 3 fail, so the next value we should see is 4
        runner.run(() -> {
                    while (true) {
                        final Integer value = cached.get();
                        assertNotNull(value);
                        assertTrue(value == 1 || value == 4);
                        if (value == 4) return;
                    }
                })
                .assertNoExceptions();

        runner.run(() -> assertEquals(4, (int) cached.get()))
                .assertNoExceptions();
    }

    /**
     * On the first refresh the Supplier throws an exception, therefore there is
     * no valid replacement.  We assert that the previous valid value is
     * still in use.
     */
    @Test
    public void refreshFailedWithException() {
        final AtomicInteger count = new AtomicInteger();
        final Supplier<Integer> supplier = () -> {
            final int i = count.incrementAndGet();
            if (i == 2) throw new RuntimeException();
            if (i == 3) throw new RuntimeException();
            return i;
        };

        final CachedSupplier<Integer> cached = CachedSupplier.builder(supplier)
                .refreshInterval(1, SECONDS)
                .accessTimeout(1, MINUTES)
                .build();

        final Runner runner = Runner.threads(100);

        // We should see the value of 1 initially
        runner.run(() -> assertEquals(1, (int) cached.get()))
                .assertNoExceptions();

        // Values 2 and 3 fail, so the next value we should see is 4
        runner.run(() -> {
                    while (true) {
                        final Integer value = cached.get();
                        assertNotNull(value);
                        assertTrue(value == 1 || value == 4);
                        if (value == 4) return;
                    }
                })
                .assertNoExceptions();

        runner.run(() -> assertEquals(4, (int) cached.get()))
                .assertNoExceptions();
    }

    private void sleep(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void await(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public void assertRange(final long value, final long min, final long max) {
        assertTrue(value > min);
        assertTrue(value < max);
    }

    static class Timer {
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

    public static class Runner {
        private final int threads;
        private final Executor executor;
        private final Duration timeout = new Duration(1, MINUTES);
        private Runnable before = null;

        public Runner(final int threads) {
            this.threads = threads;
            this.executor = Executors.newFixedThreadPool(threads, new DaemonThreadFactory(Runner.class));
        }

        public static Runner threads(final int threads) {
            return new Runner(threads);
        }

        public Runner pre(final Runnable runnable) {
            this.before = runnable;
            return this;
        }

        public Run run(final Runnable runnable) {
            final Throwable[] failures = new Throwable[threads];
            final Timer.Time[] times = new Timer.Time[threads];

            /*
             * You won't immediately understand these CountDownLatches (look down).
             *
             * Here's the deal: when you launch 100+ threads in a loop as we're
             * about to do it can take 25+ milliseconds.  By the time you get to
             * your 99th thread, the previous 50 are all probably gone. The
             * thread-creation overhead messes with all your timings and threads
             * are executing somewhat serially with very little parallelism.
             *
             * The latches fix this by forcing all the threads to truly run
             * at the same time.
             *
             * Imagine each thread is a runner in a race. What we want is
             * each runner to get on the racetrack, into the starting
             * position (ready.countDown) and wait diligently for the sound
             * of the starting pistol (start.await) before they start running.
             *
             * When all runners are in position (ready.await), we fire the starting
             * pistol (start.countDown). Awesome, they're all truly running at
             * once and competing.
             *
             * As each runner finishes the race we have them call completed.countDown
             * When all runners are finished the completed.await call will unblock
             * and we exit this method with all results in hand.
             *
             * Seems like overkill, but after you've been burned by poor testing
             * covering up thread safety issues you learn to do it right.
             */
            final CountDownLatch ready = new CountDownLatch(threads);
            final CountDownLatch start = new CountDownLatch(1);
            final CountDownLatch completed = new CountDownLatch(threads);

            for (int submitted = 0; submitted < threads; submitted++) {
                final int id = submitted;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        ready.countDown();
                        try {
                            start.await();
                        } catch (InterruptedException e) {
                            return;
                        }

                        /*
                         * If there's anything we'd like to execute
                         * that shouldn't be included in the timings,
                         * do it now.
                         */
                        if (before != null) before.run();

                        /*
                         * Run, Forrest! Run!!
                         */
                        final Timer timer = Timer.start();
                        try {
                            runnable.run();
                        } catch (Throwable t) {
                            failures[id] = t;
                        } finally {
                            times[id] = timer.time();
                            completed.countDown();
                        }
                    }
                });
            }

            // wait for the above threads to be ready
            await(ready, "ready");

            // fire the starting pistol
            start.countDown();

            // wait for them to finish the race
            await(completed, "completed");

            return new Run(threads, failures, times);
        }

        private void await(final CountDownLatch latch, final String state) {
            try {
                if (!latch.await(timeout.getTime(), timeout.getUnit())) {
                    fail(String.format("%s of %s threads not %s after %s",
                            state,
                            threads - latch.getCount(),
                            threads,
                            timeout
                    ));
                }
            } catch (InterruptedException e) {
                fail(String.format("Interrupted while waiting %s state", "ready"));
            }
        }

        public static class Run {
            final int threads;
            final Throwable[] exceptions;
            final Timer.Time[] times;


            public Run(final int threads, final Throwable[] exceptions, final Timer.Time[] times) {
                this.threads = threads;
                this.exceptions = exceptions;
                this.times = times;
            }

            public Run assertNoExceptions() {
                final long failed = Stream.of(exceptions)
                        .filter(Objects::nonNull)
                        .peek(Throwable::printStackTrace)
                        .count();
                if (failed > 0) {
                    final long succeeded = threads - failed;
                    fail(String.format("Succeeded: %s, Failed: %s", succeeded, failed));
                }
                return this;
            }

            public Run assertExceptions(final Class<? extends Throwable> expected) {
                for (final Throwable actual : exceptions) {
                    assertNotNull(actual);
                    try {
                        assertEquals(expected, actual.getClass());
                    } catch (AssertionError e) {
                        actual.printStackTrace();
                        throw e;
                    }
                }
                return this;
            }

            public Run assertTimesLessThan(final long time, final TimeUnit unit) {
                for (final Timer.Time t : times) {
                    t.assertLessThan(time, unit);
                }
                return this;
            }

            public Run assertTimesGreaterThan(final long time, final TimeUnit unit) {
                for (final Timer.Time t : times) {
                    t.assertGreaterThan(time, unit);
                }
                return this;
            }
        }
    }
}
