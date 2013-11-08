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
package org.apache.openjpa.persistence.embed;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name="EmpEmbedTest")
public class Employee {
    @Id
    int empId;
    
    @Embedded 
    ContactInfo contactInfo;
    
    @Embedded 
    JobInfo jobInfo;

    @Embedded 
    LocationDetails location;

    @ElementCollection // use default table (PERSON_NICKNAMES)
    @Column(name="name", length=50)
    protected Set<String> nickNames = new HashSet<String>();
    
    public int getEmpId() {
        return empId;
    }
    
    public void setEmpId(int empId) {
        this.empId = empId;
    }
    
    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }
    
    public ContactInfo getContactInfo() {
        return contactInfo;
    }
    
    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }
    
    public JobInfo getJobInfo() {
        return jobInfo;
    }
    
    public LocationDetails getLocationDetails() {
        return location;
    }
    
    public void setLocationDetails(LocationDetails location) {
        this.location = location;
    }
    
    public Set<String> getNickNames() {
        return nickNames;
    }
    
    public void setNickNames(Set<String> nickNames) {
        this.nickNames = nickNames;
    }
    
    public void addNickName(String nickName) {
        nickNames.add(nickName);
    }
}
