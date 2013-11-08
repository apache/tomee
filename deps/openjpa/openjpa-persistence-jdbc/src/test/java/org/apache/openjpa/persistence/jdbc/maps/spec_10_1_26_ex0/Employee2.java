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
package org.apache.openjpa.persistence.jdbc.maps.spec_10_1_26_ex0;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name="T6E2")
public class Employee2 {
    EmployeePK2 empPK;

    Department2 department;

    public Employee2() {}

    public Employee2(String name, Date bDate) {
        this.empPK = new EmployeePK2(name, bDate);
    }

    @EmbeddedId
    public EmployeePK2 getEmpPK() {
        return empPK;
    }

    public void setEmpPK(EmployeePK2 empPK) {
        this.empPK = empPK;
    }

    @ManyToOne
    @JoinColumn(name="dept_id")
    public Department2 getDepartment() {
        return department;
    }

    public void setDepartment(Department2 department) {
        this.department = department;
    }
}
