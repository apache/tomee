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
package org.apache.openejb.server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Rev$ $Date$
 */
public class Test {

    static final AtomicInteger existing = new AtomicInteger();

    public static void main(String[] args) {

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 5, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(50));
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.out.println("Rejecting " + r);
            }
        });
        executor.setThreadFactory(new ThreadFactory() {
            private AtomicInteger ids = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Thread-" + ids.incrementAndGet()) {
                    {
                        System.out.println("Creating Thread " + getName());
                        existing.incrementAndGet();
                    }

                    @Override
                    public void run() {
                        System.out.println("Running " + getName() + " : ");
                        super.run();
                    }

                    protected void finalize() throws Throwable {
                        existing.decrementAndGet();
                        System.out.println("Finalize " + getName());
                        super.finalize();
                    }

                };
            }
        });

        final Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    report(executor);
                    System.gc();
                    Test.sleep(5, TimeUnit.SECONDS);
                }
            }
        };
        thread.start();

        report(executor);

        byte[] list = new byte[100];
        final CountDownLatch latch = new CountDownLatch(1);
        int i = 0;
        for (byte b : list) {
            final int id = ++i;
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Executing " + this + " in thread: " + Thread.currentThread().getName());
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public String toString() {
                    return "Runnable-" + id;
                }
            };

            System.out.println("Adding " + runnable);
            executor.execute(runnable);
            report(executor);
        }

        latch.countDown();

    }

    private static void report(ThreadPoolExecutor executor) {
        final String format = String.format("CorePoolSize %s, PoolSize %s, LargestPoolSize %s, Existing %s", executor.getCorePoolSize(), executor.getPoolSize(), executor.getLargestPoolSize(), existing.get());
        System.out.println(format);
    }

    private static void sleep(long time, TimeUnit unit) {
        try {
            Thread.sleep(unit.toMillis(time));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
