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

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.triggers.CronTriggerImpl;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.endpoint.MessageEndpoint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @version $Rev$ $Date$
*/
public final class JobSpec implements ActivationSpec {

    private MessageEndpoint endpoint;
    private ResourceAdapter resourceAdapter;
    private Trigger trigger;
    private JobDetail detail;
    private InvalidPropertyException invalidProperty;
    private String triggerName;
    private String triggerGroup;
    private String jobName;
    private String jobGroup;
    private String description;
    private boolean recoverable;
    private boolean durable;
    private String calendarName;
    private String cronExpression;
    private String timeZone;
    private String startTime;
    private String endTime;

    public TriggerKey triggerKey() {
        return trigger.getKey();
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String s) {
        triggerName = s;
    }

    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String s) {
        triggerGroup = s;
    }

    // -- Job Name

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String s) {
        jobName = s;
    }

    // -- Job Group

    public void setJobGroup(String s) {
        jobGroup = s;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    // -- Description

    public String getDescription() {
        return description;
    }

    public void setDescription(String s) {
        description = s;
    }

    // -- Recoverable

    public void setRequestsRecovery(boolean b) {
        recoverable = b;
    }

    public boolean isRequestsRecovery() {
        return recoverable;
    }

    // -- Durability

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean b) {
        durable = b;
    }

    // -- Calendar name

    public void setCalendarName(String s) {
        calendarName = s;
    }

    public String getCalendarName() {
        return calendarName;
    }

    // -- Expression

    public void setCronExpression(String s) {
        cronExpression = s;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    /**
     * An alias for CronExpression
     *
     * @param s
     */
    public void setCronTrigger(String s) {
        setCronExpression(s);
    }

    // --

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    // --

    public void setStartTime(String startTime) {
        Date date = parse(startTime);
        if (date != null) {
            this.startTime = startTime;
        }
    }

    public void setEndTime(String endTime) {
        Date date = parse(endTime);
        if (date != null) {
            this.endTime = endTime;
        }
    }


    private Date parse(String value) {

        String[] formats = {
                "EEE MMM d HH:mm:ss z yyyy",
                "EEE, d MMM yyyy HH:mm:ss Z",
                "yyyy-MM-dd HH:mm:ss.S",
                "yyyy-MM-dd HH:mm:ss.SZ",
                "yyyy-MM-dd HH:mm:ss.S",
                "yyyy-MM-dd HH:mm:ssZ",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mmZ",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd'T'HH:mm:ss.SZ",
                "yyyy-MM-dd'T'HH:mm:ss.S",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mmZ",
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd",
                "yyyyMMdd"
        };

        for (String format : formats) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            try {
                return dateFormat.parse(value);
            } catch (ParseException e) {
                invalidProperty = new InvalidPropertyException("Invalid time format " + value, e);
            }

        }

        return null;
    }

    // -- ActivationSpec methods

    public void validate() throws InvalidPropertyException {
        if (invalidProperty != null) throw invalidProperty;

        int i = hashCode();
        detail = JobBuilder.newJob(QuartzResourceAdapter.JobEndpoint.class)
                .withIdentity("Job" + i, Scheduler.DEFAULT_GROUP)
                .withDescription(description)
                .requestRecovery(recoverable)
                .storeDurably(durable)
                .build();
        final TriggerBuilder tb = TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity("Trigger" + i, Scheduler.DEFAULT_GROUP)
                .withDescription(description);
        if (startTime != null) {
                tb.startAt(parse(startTime));
        }
        if (endTime != null) {
                tb.endAt(parse(endTime));
        }
        if (calendarName != null) {
                tb.modifiedByCalendar(calendarName);
        }
        final CronScheduleBuilder csb = CronScheduleBuilder.cronSchedule(getCronExpression());
        if (timeZone != null) {
            csb.inTimeZone(TimeZone.getTimeZone(timeZone));
        }
        tb.withSchedule(CronScheduleBuilder.cronSchedule(getCronExpression()));
        trigger = tb.build();
        

        try {
            ((CronTriggerImpl) trigger).validate();
        } catch (SchedulerException e) {
            throw new InvalidPropertyException(e);
        }
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(ResourceAdapter resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    MessageEndpoint getEndpoint() {
        return endpoint;
    }

    void setEndpoint(MessageEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    Trigger getTrigger() {
        return trigger;
    }

    JobDetail getDetail() {
        return detail;
    }

    public JobKey jobKey() {
        return detail.getKey();
    }
}
