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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "scheduledassignments")
public class ScheduledAssignment {

    @Id
    @GeneratedValue
    @Column(name = "scheduledassignmentid")
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autoassignid")
    private ScheduledAssignment parentScheduledAssignment;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "scheduledayid", nullable = false)
    private ScheduleDay scheduleDay;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caseid")
    private Case caze;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "roleid", nullable = false)
    private Role role;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lookupId")
    private Lookup brokenRuleLookup;

    @Column(name = "brokencustomruleexplanation")
    private String brokenCustomRuleExplanation; // somehow, removing this has an effect

    public ScheduledAssignment() {
        super();
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ScheduledAssignment getParentScheduledAssignment() {
        return this.parentScheduledAssignment;
    }

    public void setParentScheduledAssignment(ScheduledAssignment scheduledassignments) {
        this.parentScheduledAssignment = scheduledassignments;
    }

    public ScheduleDay getScheduleDay() {
        return this.scheduleDay;
    }

    public void setScheduleDay(ScheduleDay scheduleDay) {
        this.scheduleDay = scheduleDay;
    }

    public Case getCase() {
        return caze;
    }

    public void setCase(Case caze) {
        this.caze = caze;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role roles) {
        this.role = roles;
    }

    public Lookup getBrokenRuleLookup() {
        return brokenRuleLookup;
    }

    public void setBrokenRuleLookup(Lookup brokenRuleLookup) {
        this.brokenRuleLookup = brokenRuleLookup;
    }

    public Case getCaze() {
        return caze;
    }

    public void setCaze(Case caze) {
        this.caze = caze;
    }

    public String getBrokenCustomRuleExplanation() {
        return brokenCustomRuleExplanation;
    }

    public void setBrokenCustomRuleExplanation(String brokenCustomRuleExplanation) {
        this.brokenCustomRuleExplanation = brokenCustomRuleExplanation;
    }
}
