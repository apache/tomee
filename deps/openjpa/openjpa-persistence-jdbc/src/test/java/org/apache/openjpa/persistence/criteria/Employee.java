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
package org.apache.openjpa.persistence.criteria;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="CR_EMP")
@Inheritance
@DiscriminatorColumn(name="EMP_TYPE")  
public class Employee {
    @Id
    @GeneratedValue
    private int empId;
    
	private String name;
	@Embedded
	private Contact contactInfo;
	@ManyToOne
	private Department department;
	
	@OneToOne
	private Employee spouse;
	@ManyToOne
	private Manager manager;
	
	@OneToOne
	private FrequentFlierPlan frequentFlierPlan;

    @Basic
    private long salary;
    
    @Basic
    private int rating;
    
	
    public int getEmpId() {
        return empId;
    }
    
    public void setContactInfo(Contact contactInfo) {
        this.contactInfo = contactInfo;
    }
    
    public Contact getContactInfo() {
        return contactInfo;
    }
    
    public void setDepartment(Department department) {
        this.department = department;
    }
    
    public Department getDepartment() {
        return department;
    }

    public void setSpouse(Employee spouse) {
        this.spouse = spouse;
    }
    
    public Employee getSpouse() {
        return spouse;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
	
    public void setManager(Manager manager) {
        this.manager = manager;
    }
    
    public Manager getManager() {
        return manager;
    }
    
    public void setSalary(long salary) {
        this.salary = salary;
    }
    
    public long getSalary() {
        return salary;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    public long getRating() {
        return rating;
    }
}
