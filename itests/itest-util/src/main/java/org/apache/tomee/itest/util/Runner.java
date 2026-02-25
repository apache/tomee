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

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class Runner {
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

        final CountDownLatch ready = new CountDownLatch(threads);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch completed = new CountDownLatch(threads);

        for (int submitted = 0; submitted < threads; submitted++) {
            final int id = submitted;
            executor.execute(() -> {
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

    static class DaemonThreadFactory implements ThreadFactory {

        private final Class<?> aClass;

        public DaemonThreadFactory(final Class<?> aClass) {
            this.aClass = aClass;
        }

        @Override
        public Thread newThread(final Runnable r) {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(aClass.getSimpleName());
            return thread;
        }
    }
}
