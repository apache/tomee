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

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.endpoint.MessageEndpoint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;

/**
 * @version $Rev$ $Date$
*/
public final class JobSpec implements ActivationSpec {

    private MessageEndpoint endpoint;
    private ResourceAdapter resourceAdapter;
    private final CronTrigger trigger;
    private final JobDetail detail;
    private InvalidPropertyException invalidProperty;

    public JobSpec() {
        int i = hashCode();

        detail = new JobDetail();
        trigger = new CronTrigger();

        detail.setJobClass(QuartzResourceAdapter.JobEndpoint.class);
        detail.getJobDataMap().setAllowsTransientData(true);

        setVolatile(true);

        setJobGroup(Scheduler.DEFAULT_GROUP);
        setJobName("Job" + i);

        setTriggerGroup(Scheduler.DEFAULT_GROUP);
        setTriggerName("Trigger" + i);
    }

    public String getTriggerName() {
        return trigger.getName();
    }

    public void setTriggerName(String s) {
        trigger.setName(s);
    }

    public String getTriggerGroup() {
        return trigger.getGroup();
    }

    public void setTriggerGroup(String s) {
        trigger.setGroup(s);
    }

    // -- Job Name

    public String getJobName() {
        return detail.getName();
    }

    public void setJobName(String s) {
        detail.setName(s);
        trigger.setJobName(s);
    }

    // -- Job Group

    public void setJobGroup(String s) {
        detail.setGroup(s);
        trigger.setJobGroup(s);
    }

    public String getJobGroup() {
        return trigger.getJobGroup();
    }

    // -- Description

    public String getDescription() {
        return trigger.getDescription();
    }

    public void setDescription(String s) {
        detail.setDescription(s);
        trigger.setDescription(s);
    }

    // -- Volatility

    public void setVolatile(boolean b) {
        detail.setVolatility(b);
        trigger.setVolatility(b);
    }

    public boolean isVolatile() {
        return trigger.isVolatile();
    }

    // -- Recoverable

    public void setRequestsRecovery(boolean b) {
        detail.setRequestsRecovery(b);
    }

    public boolean isRequestsRecovery() {
        return detail.requestsRecovery();
    }

    // -- Durability

    public boolean isDurable() {
        return detail.isDurable();
    }

    public void setDurable(boolean b) {
        detail.setDurability(b);
    }

    // -- Calendar name

    public void setCalendarName(String s) {
        trigger.setCalendarName(s);
    }

    public String getCalendarName() {
        return trigger.getCalendarName();
    }

    // -- Expression

    public void setCronExpression(String s) {
        try {
            trigger.setCronExpression(s);
        } catch (ParseException e) {
            invalidProperty = new InvalidPropertyException("Invalid cron expression " + s, e);
        }
    }

    public String getCronExpression() {
        return trigger.getCronExpression();
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
        trigger.setTimeZone(TimeZone.getTimeZone(timeZone));
    }

    // --

    public void setStartTime(String startTime) {
        Date date = parse(startTime);
        if (date != null) {
            trigger.setStartTime(date);
        }
    }

    public void setEndTime(String endTime) {
        Date date = parse(endTime);
        if (date != null) {
            trigger.setEndTime(date);
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

        try {
            detail.validate();
            trigger.validate();
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

    CronTrigger getTrigger() {
        return trigger;
    }

    JobDetail getDetail() {
        return detail;
    }
}
