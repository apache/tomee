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
package org.apache.openjpa.persistence.query;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "scheduledays", uniqueConstraints = @UniqueConstraint(columnNames = "scheduledate"))
public class ScheduleDay {

    @Id
    @GeneratedValue
    @Column(name = "scheduledayid")
    private Integer id;
    @Temporal(TemporalType.DATE)
    @Column(name = "scheduledate", unique = true, nullable = false, length = 4)
    private Date date;

    public ScheduleDay() {
        super();
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer scheduledayid) {
        this.id = scheduledayid;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date scheduledate) {
        this.date = scheduledate;
    }
}
