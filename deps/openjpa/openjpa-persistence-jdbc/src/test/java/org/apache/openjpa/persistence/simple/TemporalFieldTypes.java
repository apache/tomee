/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.simple;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class TemporalFieldTypes {

    // Date mapping - Default to TIMESTAMP
    private Date dateDefaultField;

    @Temporal(TemporalType.DATE)
    private Date dateDateField;

    @Temporal(TemporalType.TIME)
    private Date dateTimeField;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTimestampField;
    
    // Calendar mapping - Default to TIMESTAMP
    private Calendar calendarDefaultField;

    @Temporal(TemporalType.DATE)
    private Calendar calendarDateField;

    @Temporal(TemporalType.TIME)
    private Calendar calendarTimeField;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar calendarTimestampField;

    private Calendar calendarTimeZoneField =
        Calendar.getInstance(TimeZone.getTimeZone("Europe/Budapest"));

    public void setDateDefaultField(Date date) {
        this.dateDefaultField = date;
    }

    public Date getDateDefaultField() {
        return this.dateDefaultField;
    }

    public void setDateDateField(Date date) {
        this.dateDateField = date;
    }

    public Date getDateDateField() {
        return this.dateDateField;
    }

    public void setDateTimeField(Date date) {
        this.dateTimeField = date;
    }

    public Date getDateTimeField() {
        return this.dateTimeField;
    }

    public void setDateTimeStampField(Date date) {
        this.dateTimestampField = date;
    }

    public Date getDateTimeStampField() {
        return this.dateTimestampField;
    }

    public void setCalendarDefaultField(Calendar calendar) {
        this.calendarDefaultField = calendar;
    }

    public Calendar getCalendarDefaultField() {
        return this.calendarDefaultField;
    }

    public void setCalendarDateField(Calendar calendar) {
        this.calendarDateField = calendar;
    }

    public Calendar getCalendarDateField() {
        return this.calendarDateField;
    }

    public void setCalendarTimeField(Calendar calendar) {
        this.calendarTimeField = calendar;
    }

    public Calendar getCalendarTimeField() {
        return this.calendarTimeField;
    }

    public void setCalendarTimeStampField(Calendar calendar) {
        this.calendarTimestampField = calendar;
    }

    public Calendar getCalendarTimeStampField() {
        return this.calendarTimestampField;
    }

    public void setCalendarTimeZoneField(Calendar calendarTimeZoneField) {
        this.calendarTimeZoneField = calendarTimeZoneField;
    }

    public Calendar getCalendarTimeZoneField() {
        return this.calendarTimeZoneField;
    }

}
