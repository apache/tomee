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
package org.apache.openjpa.integration.validation;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Future;
import javax.validation.constraints.Past;


@Entity(name = "VDATES")
@Table(name = "DATES_ENTITY")
public class ConstraintDates implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;
    
    // current time when class loaded
    @Transient
    private static final Date CURRENT_DATE = new Date();

    // Eight hours in the past
    @Transient
    private static final Date PAST_DATE = new Date(
        CURRENT_DATE.getTime() - (8 * 3600 * 1000));

    // Eight hours in the future
    @Transient
    private static final Date FUTURE_DATE = new Date(
        CURRENT_DATE.getTime() + (8* 3600 * 1000));

    @Id
    @GeneratedValue
    private long id;

    @Basic
    @Future
    private Date futureDate;

    @Basic
    private GregorianCalendar pastCalendar; // @Past constraint is on the getter

    
    /* 
     * Some helper methods to create the entities to test with
     */
    public static ConstraintDates createInvalidFuture() {
        ConstraintDates c = new ConstraintDates();
        c.setFutureDate(PAST_DATE);
        return c;
    }

    public static ConstraintDates createInvalidPast() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.HOUR, 8);

        ConstraintDates c = new ConstraintDates();
        c.setPastCalendar(cal);
        return c;
    }

    public static ConstraintDates createInvalidFuturePast() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.HOUR, 8);

        ConstraintDates c = new ConstraintDates();
        c.setFutureDate(PAST_DATE);
        c.setPastCalendar(cal);
        return c;
    }

    public static ConstraintDates createValid() {
        ConstraintDates c = new ConstraintDates();
        return c;
    }

    
    /*
     * Main entity code
     * Create a valid entity by default
     */
    public ConstraintDates() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.HOUR, -8);
        setPastCalendar(cal);
        setFutureDate(FUTURE_DATE);
    }

    public long getId() {
        return id;
    }

    public Date getFutureDate() {
        return futureDate;
    }

    public void setFutureDate(Date d) {
        futureDate = d;            
    }

    @Past
    public GregorianCalendar getPastCalendar() {
        return pastCalendar;
    }

    public void setPastCalendar(GregorianCalendar d) {
        pastCalendar = d;
    }
}
