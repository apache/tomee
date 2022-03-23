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
package org.superbiz.quartz;

import org.apache.openejb.quartz.Job;
import org.apache.openejb.quartz.JobBuilder;
import org.apache.openejb.quartz.JobDetail;
import org.apache.openejb.quartz.JobExecutionContext;
import org.apache.openejb.quartz.JobExecutionException;
import org.apache.openejb.quartz.Scheduler;
import org.apache.openejb.quartz.SimpleScheduleBuilder;
import org.apache.openejb.quartz.SimpleTrigger;
import org.apache.openejb.quartz.TriggerBuilder;
import org.apache.openejb.resource.quartz.QuartzResourceAdapter;

import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import java.util.Date;

@Stateless
public class JobBean implements JobScheduler {

    @Override
    public Date createJob() throws Exception {

        final QuartzResourceAdapter ra = (QuartzResourceAdapter) new InitialContext().lookup("java:openejb/Resource/QuartzResourceAdapter");
        final Scheduler s = ra.getScheduler();

        //Add a job type
        final JobDetail jd = JobBuilder.newJob(MyTestJob.class).withIdentity("job1", "group1").build();
        jd.getJobDataMap().put("MyJobKey", "MyJobValue");

        //Schedule my 'test' job to run now
        final SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1", "group1")
                .forJob(jd)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withRepeatCount(0)
                        .withIntervalInSeconds(0))
                .build();
        return s.scheduleJob(jd, trigger);
    }

    public static class MyTestJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("This is a simple test job to get: " + context.getJobDetail().getJobDataMap().get("MyJobKey"));
        }
    }
}
