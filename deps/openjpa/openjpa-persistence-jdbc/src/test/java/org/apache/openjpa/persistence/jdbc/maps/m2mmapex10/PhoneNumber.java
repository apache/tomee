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
package org.apache.openjpa.persistence.jdbc.maps.m2mmapex10;

import javax.persistence.*;

import java.util.*;

@Entity
@Table(name="M10Phone")
public class PhoneNumber {
    @EmbeddedId
    PhonePK phonePK;

    @ManyToMany(mappedBy="phones")
    Map<EmployeePK, Employee> emps = new HashMap<EmployeePK, Employee>();

    int room;    

    public PhonePK getPhonePK() {
        return phonePK;
    }

    public void setPhonePK(PhonePK phonePK) {
        this.phonePK = phonePK;
    }

    public Map<EmployeePK, Employee>  getEmployees() {
        return emps;
    }

    public void addEmployees(EmployeePK d, Employee employee) {
        emps.put(d, employee);
    }

    public void removeEmployee(EmployeePK d) {
        emps.remove(d);
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public boolean equals(Object o) {
        PhoneNumber p = (PhoneNumber) o;
        Map<EmployeePK, Employee> map = p.getEmployees();
        if (map.size() != emps.size())
            return false;
        Collection<Map.Entry<EmployeePK, Employee>> entries =
            (Collection<Map.Entry<EmployeePK, Employee>>) emps.entrySet();
        for (Map.Entry<EmployeePK, Employee> entry : entries) {
            EmployeePK key = entry.getKey();
            Employee e0 = Employee.findEmpl(map, key);
            Employee e = Employee.findEmpl(emps, key);
            if ((e == null && e0 != null) || (e != null && e0 == null))
                return false;
            if (!e.getEmpPK().equals(e0.getEmpPK()))
                return false;
        }
        return true;
    }    
    
    
}
