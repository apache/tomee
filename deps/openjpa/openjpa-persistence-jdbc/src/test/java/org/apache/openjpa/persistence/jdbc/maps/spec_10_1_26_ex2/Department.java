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
package org.apache.openjpa.persistence.jdbc.maps.spec_10_1_26_ex2;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

@Entity
@Table(name="S6Dept")
public class Department {

    int deptId;
    Map<EmployeePK, Employee> empMap = new HashMap<EmployeePK, Employee>();

    @Id
    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    @OneToMany(cascade=CascadeType.ALL,mappedBy="department")
    @MapKey(name="empPK")
    public Map<EmployeePK, Employee> getEmpMap() {
        return empMap;
    }

    public void setEmpMap(Map<EmployeePK, Employee> empMap) {
        this.empMap = empMap;
    }

    public void addEmployee(Employee e) {
        empMap.put(e.getEmpPK(), e);
    }

    public void removeEmployee(EmployeePK pk) {
        empMap.remove(pk);
    }    
}
