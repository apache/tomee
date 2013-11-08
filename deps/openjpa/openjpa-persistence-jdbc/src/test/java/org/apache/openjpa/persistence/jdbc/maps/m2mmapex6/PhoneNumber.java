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
package org.apache.openjpa.persistence.jdbc.maps.m2mmapex6;

import javax.persistence.*;


import java.util.*;

@Entity
@Table(name="MEx6Phone")
public class PhoneNumber {
    @Id int number;

    @ManyToMany(mappedBy="phones")
    Map<FullName, Employee> emps = new HashMap<FullName, Employee>();

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Map<FullName, Employee>  getEmployees() {
        return emps;
    }

    public void addEmployees(FullName d, Employee employee) {
        emps.put(d, employee);
    }

    public void removeEmployee(FullName d) {
        emps.remove(d);
    }

    public boolean equals(Object o) {
        PhoneNumber p = (PhoneNumber) o;
        Map<FullName, Employee> map = p.getEmployees();
        if (map.size() != emps.size())
            return false;
        Collection<Map.Entry<FullName, Employee>> entries =
            (Collection<Map.Entry<FullName, Employee>>) emps.entrySet();
        for (Map.Entry<FullName, Employee> entry : entries) {
            FullName key = entry.getKey();
            Employee e = entry.getValue();
            Employee e0 = map.get(key);
            if (e.getEmpId() != e0.getEmpId())
                return false;
        }
        return true;
    }
}
