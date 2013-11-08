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
package org.apache.openjpa.persistence.kernel.common.apps;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CAL_FLDS")
public class CalendarFields {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private Calendar unassigned;
    private Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    private Calendar pacific =
        Calendar.getInstance(TimeZone.getTimeZone("US/Pacific"));
    private Calendar newYork =
        Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
    private Calendar berlin =
        Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
    private Calendar singapore =
        Calendar.getInstance(TimeZone.getTimeZone("Asia/Singapore"));

    public CalendarFields() {
    }

    public void setGmt(Calendar gmt) {
        this.gmt = gmt;
    }

    public Calendar getGmt() {
        return this.gmt;
    }

    public void setPacific(Calendar pacific) {
        this.pacific = pacific;
    }

    public Calendar getPacific() {
        return this.pacific;
    }

    public void setNewYork(Calendar newYork) {
        this.newYork = newYork;
    }

    public Calendar getNewYork() {
        return this.newYork;
    }

    public void setBerlin(Calendar berlin) {
        this.berlin = berlin;
    }

    public Calendar getBerlin() {
        return this.berlin;
    }

    public void setSingapore(Calendar singapore) {
        this.singapore = singapore;
    }

    public Calendar getSingapore() {
        return this.singapore;
    }

    public void setUnassigned(Calendar unassigned) {
        this.unassigned = unassigned;
    }

    public Calendar getUnassigned() {
        return this.unassigned;
    }

    public List getCalendars() {
        return Arrays.asList(new Calendar[]{
            unassigned, gmt, pacific, newYork, berlin, singapore
        });
    }

    public int getId() {
        return id;
    }
}

