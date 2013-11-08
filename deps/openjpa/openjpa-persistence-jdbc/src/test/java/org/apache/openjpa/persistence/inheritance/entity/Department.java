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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Test case and domain classes were originally part of the reported issue
 * <A href="https://issues.apache.org/jira/browse/OPENJPA-1536">OPENJPA-1536</A>
 *  
 * @author Jody Grassel
 * @author Fay Wang
 *
 */
@Entity
@Table(name="CDepartment")
public class Department {
    @Id
    private int id;
    
    private String departmentName;
    
    @ManyToOne
    private Manager departmentManager;
    
    @OneToMany(mappedBy="department")
    private List<Employee> employeeList;

    public Department() {
        this.employeeList = new ArrayList<Employee>();
    }
    
    public Department(int id, String departmentName) {
        this.employeeList = new ArrayList<Employee>();
        this.id = id;
        this.departmentName = departmentName;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Manager getDepartmentManager() {
        return departmentManager;
    }

    public void setDepartmentManager(Manager departmentManager) {
        this.departmentManager = departmentManager;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }


}
