package org.superbiz.quartz;

import java.util.Date;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import org.apache.openejb.resource.quartz.QuartzResourceAdapter;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;

@Stateless
public class JobBean implements JobScheduler {

    @Override
    public Date createJob() throws Exception {

        final QuartzResourceAdapter ra = (QuartzResourceAdapter) new InitialContext().lookup("java:openejb/Resource/QuartzResourceAdapter");
        final Scheduler s = ra.getScheduler();

        //Add a job type
        final JobDetail jd = new JobDetail("job1", "group1", JobBean.MyTestJob.class);
        jd.getJobDataMap().put("MyJobKey", "MyJobValue");
 
        //Schedule my 'test' job to run now
        final SimpleTrigger trigger = new SimpleTrigger("trigger1","group1", new Date());
        return s.scheduleJob(jd, trigger);
    }

    public static class MyTestJob implements Job{

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {            
            System.out.println("This is a simple test job to get: " + context.getJobDetail().getJobDataMap().get("MyJobKey"));
        }
    }
}
