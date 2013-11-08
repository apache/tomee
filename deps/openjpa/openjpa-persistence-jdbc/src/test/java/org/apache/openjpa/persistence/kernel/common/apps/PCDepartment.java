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
package org.apache.openjpa.persistence.kernel.common.apps;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;

/**
 * @author <A HREF="mailto:pinaki.poddar@gmail.com>Pinaki Poddar</A>
 */
@Entity
@FetchGroups({
@FetchGroup(name = "department.employees",
    attributes = @FetchAttribute(name = "employees")),
@FetchGroup(name = "department.company",
    attributes = @FetchAttribute(name = "company"))
    })
public class PCDepartment {

    private String name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private PCCompany company;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<PCEmployee> employees;

    public PCDepartment() {
        super();
    }

    public PCDepartment(String name) {
        super();
        setName(name);
    }

    public PCCompany getCompany() {
        return company;
    }

    public void setCompany(PCCompany company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set getEmployees() {
        return employees;
    }

    public void setEmployees(Set employees) {
        this.employees = employees;
    }

    public void addEmployee(PCEmployee emp) {
        if (employees == null)
            employees = new HashSet();
        employees.add(emp);
        emp.setDepartment(this);
    }

    public boolean contains(PCEmployee emp) {
        return employees != null && employees.contains(emp);
    }

    public static Object reflect(PCDepartment instance, String name) {
        if (instance == null)
            return null;
        try {
            return PCDepartment.class.getDeclaredField(name).get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
