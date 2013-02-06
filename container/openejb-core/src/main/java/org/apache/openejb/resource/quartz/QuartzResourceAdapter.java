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
package org.apache.openejb.resource.quartz;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.listeners.SchedulerListenerSupport;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @version $Rev$ $Date$
 */
public class QuartzResourceAdapter implements javax.resource.spi.ResourceAdapter {

    public static final String OPENEJB_QUARTZ_TIMEOUT = "openejb.quartz.timeout";

    //Start and stop may be called from different threads so use atomics
    private final AtomicReference<Exception> ex = new AtomicReference<Exception>();
    private final AtomicReference<Scheduler> scheduler = new AtomicReference<Scheduler>();
    private final AtomicReference<BootstrapContext> bootstrapContext = new AtomicReference<BootstrapContext>();
    private final AtomicReference<Thread> startThread = new AtomicReference<Thread>();

    @Override
    public void start(final BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {

        if (null != this.bootstrapContext.getAndSet(bootstrapContext)) {
            org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").warning("QuartzResourceAdapter is already starting");
            return;
        }

        final CountDownLatch signal = new CountDownLatch(1);
        long timeout = SystemInstance.get().getOptions().get(QuartzResourceAdapter.OPENEJB_QUARTZ_TIMEOUT, 10000L);

        if (timeout < 1000L) {
            timeout = 1000L;
        }

        if (timeout > 60000L) {
            timeout = 60000L;
        }

        //Allow org.quartz.InterruptableJob implementors to be interrupted on shutdown
        System.setProperty(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN
                              , System.getProperty(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN, "true"));
        System.setProperty(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN_WITH_WAIT
                              , System.getProperty(StdSchedulerFactory.PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN_WITH_WAIT, "true"));

        //Let the user enable this if they really want it
        System.setProperty(StdSchedulerFactory.PROP_SCHED_SKIP_UPDATE_CHECK
                              , System.getProperty(StdSchedulerFactory.PROP_SCHED_SKIP_UPDATE_CHECK, "true"));
        System.setProperty("org.terracotta.quartz.skipUpdateCheck"
                              , System.getProperty("org.terracotta.quartz.skipUpdateCheck", "true"));

        startThread.set(new Thread("Quartz Scheduler Start") {

            @Override
            public void run() {

                try {
                    QuartzResourceAdapter.this.scheduler.set(StdSchedulerFactory.getDefaultScheduler());
                } catch (Exception e) {
                    QuartzResourceAdapter.this.ex.set(e);
                    return;
                }

                try {
                    QuartzResourceAdapter.this.scheduler.get().getListenerManager().addSchedulerListener(new SchedulerListenerSupport() {
                        @Override
                        public void schedulerStarted() {
                            signal.countDown();
                        }
                    });

                    QuartzResourceAdapter.this.scheduler.get().start();

                } catch (Exception e) {
                    QuartzResourceAdapter.this.ex.set(e);
                    signal.countDown();
                }
            }
        });

        startThread.get().setDaemon(true);
        startThread.get().start();

        boolean started = false;
        try {
            started = signal.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            //Ignore
        }

        final Exception exception = ex.get();
        if (null != exception) {
            final String err = "Error creating Quartz Scheduler";
            org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").error(err, exception);
            throw new ResourceAdapterInternalException(err, exception);
        }

        if (started) {
            org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").info("Started Quartz Scheduler");
        } else {
            org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").warning("Failed to start Quartz Scheduler within defined timeout, status unknown");
        }
    }

    public Scheduler getScheduler() {
        return scheduler.get();
    }

    public BootstrapContext getBootstrapContext() {
        return bootstrapContext.get();
    }

    @Override
    public void stop() {

        final Scheduler s = scheduler.getAndSet(null);

        if (null != s) {

            if (null != startThread.get()) {
                startThread.get().interrupt();
            }

            long timeout = SystemInstance.get().getOptions().get(QuartzResourceAdapter.OPENEJB_QUARTZ_TIMEOUT, 10000L);

            if (timeout < 1000L) {
                timeout = 1000L;
            }

            if (timeout > 60000L) {
                timeout = 60000L;
            }

            final CountDownLatch shutdownWait = new CountDownLatch(1);

            Thread stopThread = new Thread("Quartz Scheduler Requested Stop") {

                @Override
                public void run() {
                    try {
                        s.getListenerManager().addSchedulerListener(new SchedulerListenerSupport() {

                            @Override
                            public void schedulerShutdown() {
                                shutdownWait.countDown();
                            }
                        });

                        //Shutdown, but give running jobs a chance to complete.
                        //User scheduled jobs should really implement InterruptableJob
                        s.shutdown(true);
                    } catch (Exception e) {
                        QuartzResourceAdapter.this.ex.set(e);
                    }
                }
            };

            stopThread.setDaemon(true);
            stopThread.start();

            boolean stopped = false;
            try {
                stopped = shutdownWait.await(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                //Ignore
            }

            try {
                if (!stopped || !s.isShutdown()) {

                    stopThread = new Thread("Quartz Scheduler Forced Stop") {

                        @Override
                        public void run() {
                            try {
                                //Force a shutdown without waiting for jobs to complete.
                                s.shutdown(false);
                                org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").warning("Forced Quartz stop - Jobs may be incomplete");
                            } catch (Exception e) {
                                QuartzResourceAdapter.this.ex.set(e);
                            }
                        }
                    };

                    stopThread.setDaemon(true);
                    stopThread.start();

                    try {
                        //Give the forced shutdown a chance to complete
                        stopThread.join(timeout);
                    } catch (InterruptedException e) {
                        //Ignore
                    }
                }
            } catch (Exception e) {
                ex.set(e);
            }
        }

        this.bootstrapContext.set(null);

        if (null != ex.get()) {
            org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").warning("Error stopping Quartz Scheduler", ex.get());
        } else {
            org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").info("Stopped Quartz Scheduler");
        }
    }

    @Override
    public void endpointActivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) throws ResourceException {

        final Scheduler s = scheduler.get();
        if (null == s) {
            throw new ResourceException("Quartz Scheduler is not available");
        }

        try {

            final JobSpec spec = (JobSpec) activationSpec;

            final MessageEndpoint endpoint = messageEndpointFactory.createEndpoint(null);
            spec.setEndpoint(endpoint);

            final Job job = (Job) endpoint;

            final JobDataMap jobDataMap = spec.getDetail().getJobDataMap();
            jobDataMap.put(Data.class.getName(), new Data(job));

            s.scheduleJob(spec.getDetail(), spec.getTrigger());
        } catch (SchedulerException e) {
            throw new ResourceException("Failed to schedule job", e);
        }
    }

    @Override
    public void endpointDeactivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) {

        final Scheduler s = scheduler.get();
        if (null == s) {
            throw new IllegalStateException("Quartz Scheduler is not available");
        }

        JobSpec spec = null;

        try {
            spec = (JobSpec) activationSpec;
            s.deleteJob(spec.jobKey());

        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to delete job", e);
        } finally {
            if (null != spec) {
                spec.getEndpoint().release();
            }
        }
    }

    public static class JobEndpoint implements Job {

        private static Method method = null;

        @Override
        public void execute(final JobExecutionContext execution) throws JobExecutionException {

            MessageEndpoint endpoint = null;
            JobExecutionException ex = null;

            try {

                final JobDataMap jobDataMap = execution.getJobDetail().getJobDataMap();

                final Data data = Data.class.cast(jobDataMap.get(Data.class.getName()));

                final Job job = data.job;

                if (null == method) {
                    method = job.getClass().getMethod("execute", JobExecutionContext.class);
                }

                endpoint = (MessageEndpoint) job;
                endpoint.beforeDelivery(method);

                job.execute(execution);

            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            } catch (ResourceException e) {
                ex = new JobExecutionException(e);
            } catch (Throwable t) {
                ex = new JobExecutionException(new Exception(t));
            } finally {

                if (null != endpoint) {
                    try {
                        endpoint.afterDelivery();
                    } catch (ResourceException e) {
                        ex = new JobExecutionException(e);
                    }
                }
            }

            if (null != ex) {
                throw ex;
            }
        }
    }

    /**
     * A private inner class is used so the key and value are not publicly visible.
     * This is standard OpenEJB practice for all "public storage" maps as it prevents
     * outside code from becoming dependent on or tampering with the private data.
     */
    private static class Data {

        private final Job job;

        private Data(final Job job) {
            this.job = job;
        }
    }

    @Override
    public XAResource[] getXAResources(final ActivationSpec[] activationSpecs) throws ResourceException {
        return new XAResource[0];
    }
}
