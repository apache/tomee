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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.resource.quartz;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.impl.StdSchedulerFactory;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.ResourceException;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import org.apache.openejb.util.LogCategory;

/**
 * @version $Rev$ $Date$
 */
public class QuartzResourceAdapter implements javax.resource.spi.ResourceAdapter {

    private static Exception ex = null;
    private Scheduler scheduler;
    private BootstrapContext bootstrapContext;
    private Thread startThread;

    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {

        this.bootstrapContext = bootstrapContext;

        startThread = new Thread("Quartz Scheduler Start") {

            @Override
            public void run() {

                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

                synchronized (QuartzResourceAdapter.this) {

                    try {
                        scheduler = StdSchedulerFactory.getDefaultScheduler();
                    } catch (Exception e) {
                        ex = e;
                        return;
                    }
                }

                try {
                    scheduler.start();
                } catch (Exception e) {
                    ex = e;
                }
            }
        };

        startThread.setDaemon(true);
        startThread.start();

        try {
            startThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        if (null != ex) {
            throw new ResourceAdapterInternalException("Failed to create Quartz Scheduler", ex);
        }

        org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").info("Started Quartz Scheduler");
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public BootstrapContext getBootstrapContext() {
        return bootstrapContext;
    }

    public void stop() {

        synchronized (this) {

            if (null != scheduler) {

                if (startThread.isAlive()) {
                    startThread.interrupt();
                }

                Thread stopThread = new Thread("Quartz Scheduler Requested Stop") {

                    @Override
                    public void run() {
                        try {
                            scheduler.shutdown(true);
                        } catch (Exception e) {
                            ex = e;
                        }
                    }
                };

                stopThread.setDaemon(true);
                stopThread.start();

                try {
                    //Block for a maximum of 5 seconds waiting for this thread to die.
                    stopThread.join(5000);
                } catch (InterruptedException ie) {
                    //Ignore
                }

                try {
                    if (!scheduler.isShutdown()) {

                        stopThread = new Thread("Quartz Scheduler Forced Stop") {

                            @Override
                            public void run() {
                                try {
                                    //Try to force a shutdown
                                    scheduler.shutdown(false);
                                    org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").warning("Forced Quartz stop - Jobs may be incomplete");
                                } catch (Exception e) {
                                    ex = e;
                                }
                            }
                        };

                        stopThread.setDaemon(true);
                        stopThread.start();
                    }
                } catch (Exception e) {
                    ex = e;
                }
            }
        }

        this.bootstrapContext = null;

        if (null != ex) {
            org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").warning("Error stopping Quartz Scheduler", ex);
            return;
        }

        org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").info("Stopped Quartz Scheduler");
    }

    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws ResourceException {

        if (null == scheduler) {
            throw new ResourceException("Quartz Scheduler is not available");
        }

        try {

            JobSpec spec = (JobSpec) activationSpec;

            MessageEndpoint endpoint = messageEndpointFactory.createEndpoint(null);
            spec.setEndpoint(endpoint);

            Job job = (Job) endpoint;

            JobDataMap jobDataMap = spec.getDetail().getJobDataMap();
            jobDataMap.setAllowsTransientData(true);
            jobDataMap.put(Data.class.getName(), new Data(job));

            scheduler.scheduleJob(spec.getDetail(), spec.getTrigger());
        } catch (SchedulerException e) {
            throw new ResourceException("Failed to schedule job", e);
        }
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {

        if (null == scheduler) {
            throw new IllegalStateException("Quartz Scheduler is not available");
        }

        JobSpec spec = null;

        try {
            spec = (JobSpec) activationSpec;
            scheduler.deleteJob(spec.getJobName(), spec.getJobGroup());

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

        public void execute(JobExecutionContext execution) throws JobExecutionException {

            MessageEndpoint endpoint = null;

            try {

                JobDataMap jobDataMap = execution.getJobDetail().getJobDataMap();

                Data data = Data.class.cast(jobDataMap.get(Data.class.getName()));

                Job job = data.job;

                endpoint = (MessageEndpoint) job;

                if (null == method) {
                    method = Job.class.getMethod("execute", JobExecutionContext.class);
                }

                endpoint.beforeDelivery(method);

                job.execute(execution);

            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            } catch (ResourceException e) {
                throw new JobExecutionException(e);
            } catch (Throwable t) {
                throw new JobExecutionException(new Exception(t));
            } finally {

                if (null != endpoint) {
                    try {
                        endpoint.afterDelivery();
                    } catch (ResourceException e) {
                        throw new JobExecutionException(e);
                    }
                }
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

        private Data(Job job) {
            this.job = job;
        }
    }

    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        return new XAResource[0];
    }
}
