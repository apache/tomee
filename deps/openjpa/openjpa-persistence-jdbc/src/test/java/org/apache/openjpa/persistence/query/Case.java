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

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "cases")
public class Case {

    @Id
    @GeneratedValue
    @Column(name = "caseid")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scheduledayid", nullable = false)
    private ScheduleDay scheduleDay;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "caze")
    private List<ScheduledAssignment> scheduledAssignments;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ScheduleDay getScheduleDay() {
        return scheduleDay;
    }

    public void setScheduleDay(ScheduleDay scheduleDay) {
        this.scheduleDay = scheduleDay;
    }

    public List<ScheduledAssignment> getScheduledAssignments() {
        return scheduledAssignments;
    }

    public void setScheduledAssignments(List<ScheduledAssignment> scheduledAssignments) {
        this.scheduledAssignments = scheduledAssignments;
    }

    public void addScheduledAssignment(ScheduledAssignment scheduledAssignment) {
        scheduledAssignments.add(scheduledAssignment);
    }
}
