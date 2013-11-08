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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@NamedQueries( { 
    @NamedQuery(name = "Employee.findByName", 
        query = "Select e from Employee e where e.name LIKE :name") ,
    @NamedQuery(name = "Employee.findByNameEscaped", 
        query = "Select e from Employee e where e.name LIKE :name ESCAPE '\\'")
    })
@Entity
@Table(name="SUBQ_EMPLOYEE")
public class Employee {

    @Id 
    private long empId;
    private String name;
    
    private long someLong;

    private int statusId; 
    
    @Temporal(TemporalType.DATE)
    private Date hireDate;
    
    @Temporal(TemporalType.TIME)
    private Date hireTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date hireTimestamp;
    
    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public long getEmpId() {
        return empId;
    }

    public void setEmpId(long empId) {
        this.empId = empId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSomeLong() {
        return someLong;
    }

    public void setSomeLong(long someLong) {
        this.someLong = someLong;
    }

    public Date getHireDate() {
        return hireDate;
    }
    
    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public Date getHireTime() {
        return hireTime;
    }
    
    public void setHireTime(Date hireTime) {
        this.hireTime = hireTime;
    }
    
    public Date getHireTimestamp() {
        return hireTimestamp;
    }
    
    public void setHireTimestamp(Date hireTimestamp) {
        this.hireTimestamp = hireTimestamp;
    }
    
}
