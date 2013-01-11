/**
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
package org.apache.openejb.server;

import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.Managed;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Managed
public class ServicePool extends ServerServiceFilter {

    private static final Logger log = Logger.getInstance(LogCategory.SERVICEPOOL, "org.apache.openejb.util.resources");

    private final ThreadPoolExecutor threadPool;
    private final AtomicBoolean stop = new AtomicBoolean();

    public ServicePool(final ServerService next, final Properties properties) {
        //Liberal defaults
        this(next, new Options(properties).get("threadsCore", 2), new Options(properties).get("threads", 50), new Options(properties).get("queue", 50000), new Options(properties).get("block", false), new Options(properties).get("keepAliveTime", 1000 * 60 * 5));
    }

    public ServicePool(final ServerService next, final int threads) {
        this(next, threads, threads, 50000, true, 1000 * 60 * 5);
    }

    public ServicePool(final ServerService next, int threads, int queue, final boolean block) {
        this(next, threads, threads, queue, block, 1000 * 60 * 5);
    }

    public ServicePool(final ServerService next, int threadCore, int threads, int queue, final boolean block, long keepAliveTime) {
        super(next);

        if (keepAliveTime <= 0) {
            keepAliveTime = 1000 * 60 * 5;
        }

        if (threadCore <= 0) {
            threadCore = 100;
        }
        if (threads < threadCore) {
            threads = threadCore;
        }

        if (queue < 1) {
            queue = 1;
        }

        /**
         This thread pool starts with 2 core threads and can grow to the limit defined by 'threads'.
         If a pool thread is idle for more than 1 minute it will be discarded, unless the core size is reached.
         It can accept upto the number of processes defined by 'queue'.
         If the queue is full then an attempt is made to add the process to the queue for 10 seconds.
         Failure to add to the queue in this time will either result in a logged rejection, or if 'block'
         is true then a final attempt is made to run the process in the current thread (the service thread).
         */

        threadPool = new ThreadPoolExecutor(threadCore, threads, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queue));
        threadPool.setThreadFactory(new ThreadFactory() {

            private final AtomicInteger i = new AtomicInteger(0);

            @Override
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r, "OpenEJB." + getName() + "." + i.incrementAndGet());
                t.setDaemon(true);
                t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(final Thread t, final Throwable e) {
                        log.error("Uncaught error in: " + t.getName(), e);
                    }
                });

                return t;
            }

        });

        threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor tpe) {

                if (null == r || null == tpe || tpe.isShutdown() || tpe.isTerminated() || tpe.isTerminating()) {
                    return;
                }

                if (log.isWarningEnabled()) {
                    log.warning("ServicePool at capicity for process: " + r);
                }

                boolean offer = false;
                try {
                    offer = tpe.getQueue().offer(r, 10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    //Ignore
                }

                if (!offer) {
                    log.error("ServicePool failed to run asynchronous process: " + r);

                    if (block) {
                        try {
                            //Last ditch effort to run the process in the current thread
                            r.run();
                        } catch (Throwable e) {
                            log.error("ServicePool failed to run synchronous process: " + r);
                        }
                    }
                }
            }
        });

        SystemInstance.get().setComponent(ServicePool.class, this);
    }

    public ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {

        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        final Runnable ctxCL = new Runnable() {
            @Override
            public void run() {

                ClassLoader cl = null;

                try {
                    cl = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(tccl);

                    if (stop.get()) {
                        return;
                    }

                    ServicePool.super.service(socket);

                } catch (SecurityException e) {
                    final String msg = "ServicePool: Security error: " + e.getMessage();
                    if (log.isDebugEnabled()) {
                        log.error(msg, e);
                    } else {
                        log.error(msg + " - Debug for StackTrace");
                    }
                } catch (IOException e) {
                    final String msg = "ServicePool: Unexpected IO error: " + e.getMessage();
                    if (log.isDebugEnabled()) {
                        log.debug(msg, e);
                    } else {
                        log.warning(msg + " - Debug for StackTrace");
                    }
                } catch (Throwable e) {
                    final String msg = "ServicePool: Unexpected error: " + e.getMessage();
                    if (log.isDebugEnabled()) {
                        log.error(msg, e);
                    } else {
                        log.error(msg + " - Debug for StackTrace");
                    }

                } finally {

                    //Ensure delegated socket is closed here

                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (Throwable t) {
                        final String msg = "ServicePool: Error closing socket";
                        if (log.isDebugEnabled()) {
                            log.debug(msg, t);
                        } else {
                            log.warning(msg);
                        }
                    }

                    Thread.currentThread().setContextClassLoader(cl);
                }
            }
        };

        threadPool.execute(ctxCL);
    }

    @Managed
    private final Pool pool = new Pool();

    @Managed(append = true)
    public class Pool {

        @Managed
        public boolean isShutdown() {
            return threadPool.isShutdown();
        }

        @Managed
        public boolean isTerminating() {
            return threadPool.isTerminating();
        }

        @Managed
        public boolean isTerminated() {
            return threadPool.isTerminated();
        }

        @Managed
        public int getPoolSize() {
            return threadPool.getPoolSize();
        }

        @Managed
        public int getCorePoolSize() {
            return threadPool.getCorePoolSize();
        }

        @Managed
        public int getMaximumPoolSize() {
            return threadPool.getMaximumPoolSize();
        }

        @Managed
        public long getKeepAliveTime(final TimeUnit unit) {
            return threadPool.getKeepAliveTime(unit);
        }

        @Managed
        public int getQueueSize() {
            return threadPool.getQueue().size();
        }

        @Managed
        public int getActiveCount() {
            return threadPool.getActiveCount();
        }

        @Managed
        public int getLargestPoolSize() {
            return threadPool.getLargestPoolSize();
        }

        @Managed
        public long getTaskCount() {
            return threadPool.getTaskCount();
        }

        @Managed
        public long getCompletedTaskCount() {
            return threadPool.getCompletedTaskCount();
        }

        @Managed
        public void setMaximumPoolSize(final int maximumPoolSize) {
            threadPool.setMaximumPoolSize(maximumPoolSize);
        }

        @Managed
        public void setCorePoolSize(final int corePoolSize) {
            getThreadPool().setCorePoolSize(corePoolSize);
        }

        @Managed
        public void allowCoreThreadTimeOut(final boolean value) {
            getThreadPool().allowCoreThreadTimeOut(value);
        }

        @Managed(description = "Sets time in nanoseconds")
        public void setKeepAliveTime(final long time) {
            getThreadPool().setKeepAliveTime(time, TimeUnit.NANOSECONDS);
        }
    }
}
