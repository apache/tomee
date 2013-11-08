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
package org.apache.openjpa.persistence.jdbc.maps.m2mmapex2;

import javax.persistence.*;

import java.util.*;

@Entity
@Table(name="MEx2Phone")
public class PhoneNumber {
    @Id int number;

    @ManyToMany(mappedBy="phones")
    Map<String, Employee> emps = new HashMap<String, Employee>();

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Map<String, Employee>  getEmployees() {
        return emps;
    }

    public void addEmployees(String str, Employee employee) {
        emps.put(str, employee);
    }

    public void removeEmployee(String str) {
        emps.remove(str);
    }

    public boolean equals(Object o) {
        PhoneNumber p = (PhoneNumber) o;
        Map<String, Employee> map = p.getEmployees();
        if (map.size() != emps.size())
            return false;
        Collection<Map.Entry<String, Employee>> entries =
            (Collection<Map.Entry<String, Employee>>) emps.entrySet();
        for (Map.Entry<String, Employee> entry : entries) {
            String key = entry.getKey();
            Employee e0 = map.get(key);
            Employee e = emps.get(key);
            if (e.getEmpId() != e0.getEmpId())
                return false;
        }
        return true;
    }
}
