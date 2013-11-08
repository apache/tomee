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
package org.apache.openjpa.persistence.proxy.delayed.vec;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.openjpa.persistence.proxy.delayed.IDepartment;
import org.apache.openjpa.persistence.proxy.delayed.IEmployee;

@Entity
@Table(name="DC_EMPLOYEE")
public class Employee implements IEmployee, Serializable {

    private static final long serialVersionUID = 1878272252981151246L;

    @Id
    @GeneratedValue
    private int id;
    
    private String empName;
    
    @ManyToOne(targetEntity=Department.class)
    @JoinColumn(name="DEPT_ID")
    private IDepartment dept;

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getEmpName() {
        return empName;
    }


    public void setId(int id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }


    public void setDept(IDepartment dept) {
        this.dept = dept;
    }


    public IDepartment getDept() {
        return dept;
    }
    
    @Override
    public int hashCode() {
        return getId();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Employee) {
            Employee e = (Employee)obj;
            return e.getId() == getId() && e.getEmpName().equals(getEmpName());
        }
        return false;
    }
}
