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

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class CachedSupplier<T> implements Supplier<T> {

    private final Logger logger;
    private final Duration initialRetryDelay;
    private final Duration maxRetryDelay;
    private final Duration accessTimeout;
    private final Duration refreshInterval;
    private final Supplier<T> supplier;

    private final AtomicReference<T> value = new AtomicReference<>();
    private final AtomicReference<Accessor<T>> accessor = new AtomicReference<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());

    private CachedSupplier(final Supplier<T> supplier, final Duration initialRetryDelay,
                           final Duration maxRetryDelay, final Duration accessTimeout,
                           final Duration refreshInterval, final Logger logger) {

        Objects.requireNonNull(supplier, "supplier");
        Objects.requireNonNull(initialRetryDelay, "initialRetryDelay");
        Objects.requireNonNull(maxRetryDelay, "maxRetryDelay");
        Objects.requireNonNull(accessTimeout, "accessTimeout");
        Objects.requireNonNull(refreshInterval, "refreshInterval");

        this.supplier = supplier;
        this.initialRetryDelay = initialRetryDelay;
        this.maxRetryDelay = maxRetryDelay;
        this.accessTimeout = accessTimeout;
        this.refreshInterval = refreshInterval;
        this.logger = logger != null ? logger : createLogger(supplier);

        /*
         * This must be last as it starts running code
         * that uses the above settings
         */
        this.accessor.set(new BlockTillInitialized());
    }

    private Logger createLogger(final Supplier<T> supplier) {
        final String simpleName = supplier.getClass().getSimpleName();
        final LogCategory child = LogCategory.OPENEJB.createChild("cache").createChild(simpleName);
        return Logger.getInstance(LogCategory.ACTIVEMQ, CachedSupplier.class);
    }

    @Override
    public T get() {
        final Accessor<T> accessor = this.accessor.get();
        return accessor.get();
    }

    public interface Accessor<T> {
        T get();
    }

    class BlockTillInitialized implements Accessor<T> {
        final CountDownLatch initialized = new CountDownLatch(1);

        public BlockTillInitialized() {
            executor.execute(new Initialize(1, initialRetryDelay));
        }

        @Override
        public T get() {
            try {
                if (initialized.await(accessTimeout.getTime(), accessTimeout.getUnit())) {
                    return value.get();
                }
                logger.debug(String.format("Timeout of %s reached waiting for initial value from supplier: %s", accessTimeout, supplier));
                throw new AccessTimeoutException(accessTimeout, supplier);
            } catch (InterruptedException e) {
                logger.debug(String.format("InterruptedException encountered while waiting for initial value from supplier: %s", supplier), e);
                throw new AccessInterruptedException(supplier);
            }
        }

        class Initialize implements Runnable {
            final int attempts;
            final Duration delay;

            public Initialize(final int attempts, final Duration delay) {
                this.attempts = attempts;
                this.delay = delay;
            }

            public Initialize retry() {
                if (delay.greaterOrEqualTo(maxRetryDelay)) {
                    return new Initialize(attempts + 1, maxRetryDelay);
                } else {
                    return new Initialize(attempts + 1, Duration.min(maxRetryDelay, delay.multiply(2)));
                }
            }

            @Override
            public void run() {
                try {
                    final T t = supplier.get();

                    if (t != null) { // SUCCESS

                        value.set(t);
                        accessor.set(new Initialized());
                        initialized.countDown();

                        logger.debug(String.format("Initialization attempt %s succeeded. Supplier %s returned valid result.",
                                attempts,
                                supplier
                        ));

                        return;

                    } else { // FAILED

                        logger.error(String.format("Initialization attempt %s failed. Supplier %s returned null. Next retry will be in %s",
                                attempts,
                                supplier,
                                retry().delay
                        ));
                    }

                } catch (final Throwable e) {
                    logger.error(String.format("Initialization attempt %s failed. Supplier %s threw an exception. Next retry will be in %s",
                            attempts,
                            supplier,
                            retry().delay
                    ), e);
                }

                /*
                 * Initialization didn't work.  Let's try again
                 */
                final Initialize retry = retry();
                executor.schedule(retry, retry.delay.getTime(), retry.delay.getUnit());
            }
        }
    }

    class Initialized implements Accessor<T> {
        public Initialized() {
            executor.scheduleAtFixedRate(new Refresh(), refreshInterval.getTime(), refreshInterval.getTime(), refreshInterval.getUnit());
        }

        @Override
        public T get() {
            return value.get();
        }

        class Refresh implements Runnable {
            @Override
            public void run() {
                try {
                    final T t = supplier.get();
                    if (t != null) {
                        value.set(t);
                        logger.debug(String.format("Refresh succeeded. Supplier %s returned valid value.  Next refresh will be in %s",
                                supplier,
                                refreshInterval
                        ));
                    } else {
                        logger.error(String.format("Refresh failed. Supplier %s returned null.  Next refresh will be in %s",
                                supplier,
                                refreshInterval
                        ));
                    }
                } catch (Throwable e) {
                    logger.error(String.format("Refresh failed. Supplier %s threw an exception.  Next refresh will be in %s",
                            supplier,
                            refreshInterval
                    ), e);
                }
            }
        }
    }

    public static <T> CachedSupplier<T> of(final Supplier<T> supplier) {
        return new Builder<T>().supplier(supplier).build();
    }

    public static <T> Builder<T> builder(final Supplier<T> supplier) {
        return new Builder<T>().supplier(supplier);
    }

    public static class AccessTimeoutException extends RuntimeException {
        private final Duration timeout;
        private final Supplier<?> supplier;

        public AccessTimeoutException(final Duration timeout, final Supplier<?> supplier) {
            super(String.format("Timeout of %s reached waiting for initial value from supplier: %s", timeout, supplier));
            this.timeout = timeout;
            this.supplier = supplier;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public Supplier<?> getSupplier() {
            return supplier;
        }
    }

    public static class AccessInterruptedException extends RuntimeException {
        private final Supplier<?> supplier;

        public AccessInterruptedException(final Supplier<?> supplier) {
            super(String.format("Interrupted while waiting for initial value from supplier: %s", supplier));
            this.supplier = supplier;
        }

        public Supplier<?> getSupplier() {
            return supplier;
        }
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(final Runnable r) {
            final Thread thread = new Thread(r);
            thread.setName(CachedSupplier.class.getSimpleName() + " Supplier");
            thread.setDaemon(true);
            return thread;
        }
    }

    public static class Builder<T> {
        private Duration initialRetryDelay = new Duration(2, TimeUnit.SECONDS);
        private Duration maxRetryDelay = new Duration(1, TimeUnit.HOURS);
        private Duration accessTimeout = new Duration(30, TimeUnit.SECONDS);
        private Duration refreshInterval = new Duration(1, TimeUnit.DAYS);
        private Supplier<T> supplier;
        private Logger logger;

        public Builder<T> initialRetryDelay(final Duration initialRetryDelay) {
            this.initialRetryDelay = initialRetryDelay;
            return this;
        }

        public Builder<T> initialRetryDelay(final int time, final TimeUnit unit) {
            return initialRetryDelay(new Duration(time, unit));
        }

        public Builder<T> maxRetryDelay(final Duration maxRetryDelay) {
            this.maxRetryDelay = maxRetryDelay;
            return this;
        }

        public Builder<T> maxRetryDelay(final int time, final TimeUnit unit) {
            return maxRetryDelay(new Duration(time, unit));
        }

        public Builder<T> accessTimeout(final Duration accessTimeout) {
            this.accessTimeout = accessTimeout;
            return this;
        }

        public Builder<T> accessTimeout(final int time, final TimeUnit unit) {
            return accessTimeout(new Duration(time, unit));
        }


        public Builder<T> refreshInterval(final Duration refreshInterval) {
            this.refreshInterval = refreshInterval;
            return this;
        }

        public Builder<T> refreshInterval(final int time, final TimeUnit unit) {
            return refreshInterval(new Duration(time, unit));
        }

        public Builder<T> supplier(final Supplier<T> supplier) {
            this.supplier = supplier;
            return this;
        }

        public Builder<T> logger(final Logger logger) {
            this.logger = logger;
            return this;
        }

        public CachedSupplier<T> build() {
            return new CachedSupplier<>(supplier,
                    initialRetryDelay,
                    maxRetryDelay,
                    accessTimeout,
                    refreshInterval,
                    logger);
        }
    }
}
