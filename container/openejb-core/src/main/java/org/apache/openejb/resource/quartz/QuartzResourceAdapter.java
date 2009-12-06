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
import org.quartz.SchedulerFactory;
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

/**
 * @version $Rev$ $Date$
*/
public class QuartzResourceAdapter implements javax.resource.spi.ResourceAdapter {

    private Scheduler scheduler;

    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        try {
            SchedulerFactory factory = new StdSchedulerFactory();
            scheduler = factory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            throw new ResourceAdapterInternalException("Failed to create Quartz Scheduler", e);
        }
    }

    public void stop() {
        try {
            scheduler.shutdown(true);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to shutdown Quartz Scheduler", e);
        }
    }

    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws ResourceException {
        JobSpec spec = (JobSpec) activationSpec;

        MessageEndpoint endpoint = messageEndpointFactory.createEndpoint(null);
        spec.setEndpoint(endpoint);

        Job job = (Job) endpoint;

        JobDataMap jobDataMap = spec.getDetail().getJobDataMap();
        jobDataMap.setAllowsTransientData(true);
        jobDataMap.put(Data.class.getName(), new Data(job));

        try {
            scheduler.scheduleJob(spec.getDetail(), spec.getTrigger());
        } catch (SchedulerException e) {
            throw new ResourceException("Failed to schedule job", e);
        }
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        JobSpec spec = (JobSpec) activationSpec;

        try {
            scheduler.deleteJob(spec.getJobName(), spec.getJobGroup());
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to delete job", e);
        }

        spec.getEndpoint().release();
    }


    public static class JobEndpoint implements Job {

        public void execute(JobExecutionContext execution) throws JobExecutionException {

            JobDataMap jobDataMap = execution.getJobDetail().getJobDataMap();

            Data data = Data.class.cast(jobDataMap.get(Data.class.getName()));

            Job job = data.job;

            MessageEndpoint endpoint = (MessageEndpoint) job;

            try {
                Method method = Job.class.getMethod("execute", JobExecutionContext.class);

                endpoint.beforeDelivery(method);

                job.execute(execution);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            } catch (ResourceException e) {
                throw new JobExecutionException(e);
            } finally {
                try {
                    endpoint.afterDelivery();
                } catch (ResourceException e) {
                    throw new JobExecutionException(e);
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
