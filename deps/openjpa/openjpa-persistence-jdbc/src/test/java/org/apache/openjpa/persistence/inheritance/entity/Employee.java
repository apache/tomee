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
package org.apache.openjpa.persistence.inheritance.entity;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Test case and domain classes were originally part of the reported issue
 * <A href="https://issues.apache.org/jira/browse/OPENJPA-1536">OPENJPA-1536</A>
 *  
 * @author Jody Grassel
 * @author Fay Wang
 *
 */
@Entity
@Table(name="CEmployee")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="EMP_TYPEL", discriminatorType=DiscriminatorType.INTEGER)
public class Employee {
    
    @Id
    private int id;
    
    private String lastName;
    private String firstName;

    private int vacationDays;
    
    @ManyToOne
    private Manager manager;
    
    @ManyToOne
    private Department department;
    
    @Version
    private long version;
    
    private transient String str = null;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        str = null;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        str = null;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        str = null;
    }

    public int getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(int vacationDays) {
        this.vacationDays = vacationDays;
        str = null;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
        str = null;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
        str = null;
    }

    public long getVersion() {
        return version;
    }


}
