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
import org.apache.openejb.util.OptionsLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
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
    private static final int KEEP_ALIVE_TIME = 1000 * 60 * 1;

    private final ThreadPoolExecutor threadPool;
    private final AtomicBoolean stop = new AtomicBoolean();
    private final CleanUpThread cleanupThread = new CleanUpThread();
    private long maxResponseSize = Long.MAX_VALUE;
    private int cleanupTimeout = 500;

    public ServicePool(final ServerService next, final Properties properties) {
        /**Defaults.
         * This suggests that 10 core threads should cope with up to 19 runnables (threads + queue, whereby queue = threads - 1).
         * Any more than 19 runnables will spawn a thread to cope if the thread count is less than 150.
         * If 150 threads are processing runnables and the queue is full then block and wait for
         * a slot for up to 10 seconds before rejecting the runnable.
         * If a thread remains idle for more than 1 minute then it will be removed.
         */
        this(next, new Options(properties).get("threadsCore", 10), new Options(properties).get("threads", 150), new Options(properties).get("queue", 0), new Options(properties).get("block", true), new Options(properties).get("keepAliveTime", KEEP_ALIVE_TIME));
    }

    public ServicePool(final ServerService next, final int threads) {
        this(next, threads, threads, 0, true, KEEP_ALIVE_TIME);
    }

    public ServicePool(final ServerService next, final int threads, final int queue, final boolean block) {
        this(next, threads, threads, queue, block, KEEP_ALIVE_TIME);
    }

    public ServicePool(final ServerService next, int threadCore, int threads, int queue, final boolean block, long keepAliveTime) {
        super(next);

        if (keepAliveTime <= 0) {
            keepAliveTime = KEEP_ALIVE_TIME;
        }

        if (threadCore <= 2) {
            threadCore = 2;
        }

        if (threads < threadCore) {
            threads = threadCore;
        }

        if (queue >= threadCore || queue < 1) {
            queue = threadCore - 1;
        }

        /**
         This thread pool starts with 'threadCore' core threads and can grow to the limit defined by 'threads'.
         If a pool thread is idle for more than 1 minute it will be discarded, until the core size is reached.
         It can accept upto the number of runnables defined by 'queue' + 'threadCore' before the pool will grow,
         so the 'queue' should ideally be less than 'threadsCore', but certainly less than 'threads'.
         If the 'queue' and 'threads' are full then an attempt is made to add the runnable to the queue for 10 seconds.
         Failure to add to the queue in this time will either result in a logged rejection, or if 'block'
         is true then a final attempt is made to run the runnable in the current thread (the service thread).
         */

        threadPool = new ThreadPoolExecutor(threadCore, threads, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queue));
        threadPool.setThreadFactory(new ThreadFactory() {

            private final AtomicInteger i = new AtomicInteger(0);

            @Override
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r, "OpenEJB." + ServicePool.this.getName() + "." + i.incrementAndGet());
                t.setDaemon(true);
                t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(final Thread t, final Throwable e) {
                        log.error("ServicePool '" + ServicePool.this.getName() + "': Uncaught error in: " + t.getName(), e);
                    }
                });

                return t;
            }

        });

        final int c = threadCore;
        final int t = threads;
        final int q = queue;

        threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor tpe) {

                if (null == r || null == tpe || tpe.isShutdown() || tpe.isTerminated() || tpe.isTerminating()) {
                    return;
                }

                if (log.isWarningEnabled()) {
                    log.warning(String.format("ServicePool '" + ServicePool.this.getName() + "' with (%1$s) threads is at capicity (%2$s) for queue (%3$s) on process: %4$s"
                                              + "\nConsider increasing the 'threadCore','threads' and 'queue' size properties.", c, t, q, r));
                }

                boolean offer = false;
                try {
                    offer = tpe.getQueue().offer(r, 10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    //Ignore
                }

                if (!offer) {

                    if (block) {
                        try {
                            //Last ditch effort to run the process in the current thread
                            r.run();

                            log.warning("ServicePool '" + ServicePool.this.getName() + "' forced execution on the current server thread: " + r
                                        + "\nIt is highly recommended that the service 'threadCore','threads' and 'queue' size properties are increased!");

                        } catch (Throwable e) {
                            log.error("ServicePool '" + ServicePool.this.getName() + "' failed to run a process in the current server thread: " + r);
                        }
                    } else {
                        log.error("ServicePool '" + ServicePool.this.getName() + "' rejected asynchronous process: " + r
                                  + "\nIt is strongly advised that the 'threadCore', 'threads', 'queue' size and 'block' properties are modified to prevent data loss!");
                    }
                }
            }
        });

        SystemInstance.get().setComponent(ServicePool.class, this);

        if (log.isInfoEnabled()) {
            log.info(String.format("Created ServicePool '%1$s' with (%2$s) core threads, limited to (%3$s) threads with a queue of (%4$s)", getName(), c, t, q));
        }
    }

    public ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }

    @Override
    public void init(final Properties props) throws Exception {
        super.init(props);
        final Options options = new Options(props);
        options.setLogger(new OptionsLog(log));
        cleanupTimeout = options.get("cleanupTimeout", 500);
        maxResponseSize = options.get("maxResponseSize", Long.MAX_VALUE);
    }

    @Override
    public void start() throws ServiceException {
        if (!cleanupThread.isAlive()) {
            cleanupThread.setName("CleanUp Socket Thread");
            cleanupThread.setDaemon(true);
            cleanupThread.start();
        }
    }

    @Override
    public void stop() throws ServiceException {
        if (cleanupThread.isAlive()) {
            cleanupThread.setActive(false);
            try {
                cleanupThread.join(cleanupTimeout);
            } catch (final InterruptedException e) {
                // no-op
            }
            cleanupThread.interrupt();
        }
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {

        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        final Runnable ctxCL = new Runnable() {

            @Override
            public String toString() {
                return "ServicePool." + ServicePool.this.getName() + ".Socket:" + socket.getInetAddress();
            }

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
                    final String msg = "ServicePool '" + ServicePool.this.getName() + "': Security error: " + e.getMessage();
                    if (log.isDebugEnabled()) {
                        log.error(msg, e);
                    } else {
                        log.error(msg + " - Debug for StackTrace");
                    }
                } catch (IOException e) {
                    final String msg = "ServicePool '" + ServicePool.this.getName() + "': Unexpected IO error: " + e.getMessage();
                    if (log.isDebugEnabled()) {
                        log.debug(msg, e);
                    } else {
                        log.warning(msg + " - Debug for StackTrace");
                    }
                } catch (Throwable e) {
                    final String msg = "ServicePool '" + ServicePool.this.getName() + "': Unexpected error: " + e.getMessage();
                    if (log.isDebugEnabled()) {
                        log.error(msg, e);
                    } else {
                        log.error(msg + " - Debug for StackTrace");
                    }

                } finally {

                    //Ensure delegated socket is closed here
                    final Runnable closeTask = new CloseTask(socket);
                    if (!cleanupThread.isAlive() || !cleanupThread.add(closeTask)) {
                        closeTask.run();
                    }

                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (Throwable t) {

                        if (log.isDebugEnabled()) {
                            log.debug("ServicePool '" + ServicePool.this.getName() + "': Error closing socket", t);
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
            return getThreadPool().isShutdown();
        }

        @Managed
        public boolean isTerminating() {
            return getThreadPool().isTerminating();
        }

        @Managed
        public boolean isTerminated() {
            return getThreadPool().isTerminated();
        }

        @Managed
        public int getPoolSize() {
            return getThreadPool().getPoolSize();
        }

        @Managed
        public int getCorePoolSize() {
            return getThreadPool().getCorePoolSize();
        }

        @Managed
        public int getMaximumPoolSize() {
            return getThreadPool().getMaximumPoolSize();
        }

        @Managed
        public long getKeepAliveTime(final TimeUnit unit) {
            return getThreadPool().getKeepAliveTime(unit);
        }

        @Managed
        public int getQueueSize() {
            return getThreadPool().getQueue().size();
        }

        @Managed
        public int getActiveCount() {
            return getThreadPool().getActiveCount();
        }

        @Managed
        public int getLargestPoolSize() {
            return getThreadPool().getLargestPoolSize();
        }

        @Managed
        public long getTaskCount() {
            return getThreadPool().getTaskCount();
        }

        @Managed
        public long getCompletedTaskCount() {
            return getThreadPool().getCompletedTaskCount();
        }

        @Managed
        public void setMaximumPoolSize(final int maximumPoolSize) {
            getThreadPool().setMaximumPoolSize(maximumPoolSize);

            if (log.isInfoEnabled()) {
                log.info(String.format("Set ServicePool '" + ServicePool.this.getName() + "' maximum threads to (%1$s)", maximumPoolSize));
            }
        }

        @Managed
        public void setCorePoolSize(final int corePoolSize) {
            getThreadPool().setCorePoolSize(corePoolSize);

            if (log.isInfoEnabled()) {
                log.info(String.format("Set ServicePool '" + ServicePool.this.getName() + "' core threads to (%1$s)", corePoolSize));
            }
        }

        @Managed
        public void allowCoreThreadTimeOut(final boolean value) {
            getThreadPool().allowCoreThreadTimeOut(value);

            if (log.isInfoEnabled()) {
                log.info(String.format("Set ServicePool '" + ServicePool.this.getName() + "' allow core thread timeout to (%1$s)", value));
            }
        }

        @Managed(description = "Sets time in nanoseconds")
        public void setKeepAliveTime(final long time) {
            getThreadPool().setKeepAliveTime(time, TimeUnit.NANOSECONDS);

            if (log.isInfoEnabled()) {
                log.info(String.format("Set ServicePool '" + ServicePool.this.getName() + "' keep alive time to (%1$s) nanoseconds", time));
            }
        }
    }

    private class CloseTask implements Runnable {
        private final Socket socket;

        public CloseTask(final Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            int count = 0;
            try {
                final InputStream in = socket.getInputStream(); // should throw an exception is socket is closed
                while (in.read() != -1) {
                    // wait client cuts the connection
                    count++;
                    if (count > maxResponseSize && maxResponseSize >= 0) {
                        break;
                    }
                }
                in.close();
            } catch (final Throwable e) {
                //Ignore
            }

            try {
                socket.getOutputStream().close();
            } catch (final Throwable e) {
                //Ignore
            }

            try {
                socket.close();
            } catch (Throwable e) {
                log.error("Encountered problem while closing connection with client: " + e.getMessage());
            }
        }
    }

    private static class CleanUpThread extends Thread {
        private final AtomicBoolean active = new AtomicBoolean(true);
        private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

        @Override
        public void run() {
            while (active.get()) {
                try {
                    queue.take().run();
                } catch (final InterruptedException e) {
                    // no-op
                }
            }
        }

        public void setActive(final boolean value) {
            active.set(value);

            // close all
            if (!value) {
                for (final Runnable r : queue) {
                    r.run();
                }
            }

            queue.add(new NoOpRunnable()); // if queue was empty simply force the loop to stop
        }

        public boolean add(final Runnable run) {
            return queue.add(run);
        }
    }

    private static class NoOpRunnable implements Runnable {
        @Override
        public void run() {
            // no-op
        }
    }
}
