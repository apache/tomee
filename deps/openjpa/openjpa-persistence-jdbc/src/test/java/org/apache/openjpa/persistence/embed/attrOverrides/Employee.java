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
package org.apache.openjpa.persistence.embed.attrOverrides;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

@Entity
@Table(name="EMP_ATTROVER")
public class Employee {
    @Id
    int empId;
    
    @AssociationOverrides({
    	@AssociationOverride (
    		name="phoneNumbers",
    		joinColumns={},
    		joinTable=@JoinTable(
    			name="EMPPHONES",
    			joinColumns=@JoinColumn(name="EMP"),
    			inverseJoinColumns=@JoinColumn(name="PHONE"))
    	),
    	@AssociationOverride (
        	name="ecInfo.phoneNumber",
        	joinColumns=@JoinColumn(name="EMERGENCY_PHONE")
       	),
    	@AssociationOverride (
           	name="ecInfo.address",
           	joinColumns=@JoinColumn(name="EMERGENCY_ADDR")
       	),
    	@AssociationOverride (
           	name="address",
           	joinColumns=@JoinColumn(name="EMP_ADDR")
       	)
    })
    @AttributeOverrides({
        @AttributeOverride(name="ecInfo.fName", 
       		column=@Column(name="EMERGENCY_FNAME")),
        @AttributeOverride(name="ecInfo.lName", 
       		column=@Column(name="EMERGENCY_LNAME"))
    })
    @Embedded 
    ContactInfo contactInfo;
    
    @ElementCollection
    @CollectionTable(name="EMP_ATTROVER_jobInfos")
  	@AssociationOverride (
   		name="value.pm",
   		joinColumns=@JoinColumn(name="PROGRAM_MGR")
   	)
  	@AttributeOverride (
   		name="value.jobDescription",
   		column=@Column(name="JOB_DESC")
   	)
   	@MapKeyColumn(name="JOB_KEY", length=20)
    Map<String, JobInfo> jobInfos = new HashMap<String, JobInfo>();
    
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
    
    public void addJobInfo(JobInfo jobInfo) {
        jobInfos.put(jobInfo.getJobDescription(), jobInfo);
    }
    
    public Map<String, JobInfo> getJobInfos() {
        return jobInfos;
    }
}
