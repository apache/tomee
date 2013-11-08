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
package org.apache.openjpa.persistence.jdbc.query.domain;

import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class TimeEntity {
	@Id
	@GeneratedValue
	private long id;
	
    String name;
    
    int value;
    
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Calendar cal2Timestamp;

    @Temporal(TemporalType.TIME)
    private java.util.Calendar cal2Time;

    @Temporal(TemporalType.DATE)
    private java.util.Calendar cal2Date;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date udate2Timestamp;

    @Temporal(TemporalType.TIME)
    private java.util.Date udate2Time;

    @Temporal(TemporalType.DATE)
    private java.util.Date udate2SDate;

    public TimeEntity() {
    }

    public TimeEntity(int id, String name, int value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    public Calendar getCal2Timestamp() {
        return cal2Timestamp;
    }

    public void setCal2Timestamp(Calendar cal2Timestamp) {
        this.cal2Timestamp = cal2Timestamp;
    }

    public Calendar getCal2Time() {
        return cal2Time;
    }

    public void setCal2Time(Calendar cal2Time) {
        this.cal2Time = cal2Time;
    }

    public Calendar getCal2Date() {
        return cal2Date;
    }

    public void setCal2Date(Calendar cal2Date) {
        this.cal2Date = cal2Date;
    }

    public java.util.Date getUDate2Timestamp() {
        return udate2Timestamp;
    }

    public void setUDate2Timestamp(java.util.Date udate2Timestamp) {
        this.udate2Timestamp = udate2Timestamp;
    }

    public java.util.Date getUDate2Time() {
        return udate2Time;
    }

    public void setUDate2Time(java.util.Date udate2Time) {
        this.udate2Time = udate2Time;
    }

    public java.util.Date getUDate2SDate() {
        return udate2SDate;
    }

    public void setUDate2SDate(java.util.Date udate2SDate) {
        this.udate2SDate = udate2SDate;
    }
}
