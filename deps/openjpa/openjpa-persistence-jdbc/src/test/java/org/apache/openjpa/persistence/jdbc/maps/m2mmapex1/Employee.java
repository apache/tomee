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
package org.apache.openjpa.persistence.jdbc.maps.m2mmapex1;

import javax.persistence.*;

import java.util.*;

@Entity
@Table(name="MEx1Emp")
public class Employee {
    @Id
    int empId;

    @ManyToMany
    Map<Department, PhoneNumber> phones = 
        new HashMap<Department, PhoneNumber>();   

    public Map<Department, PhoneNumber> getPhoneNumbers() {
        return phones;
    }

    public void addPhoneNumber(Department d, PhoneNumber phoneNumber) {
        phones.put(d, phoneNumber);
    }

    public void removePhoneNumber(Department d) {
        phones.remove(d);
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public boolean equals(Object o) {
        Employee e = (Employee) o;
        Map<Department, PhoneNumber> map = e.getPhoneNumbers();
        if (map.size() != phones.size())
            return false;
        Collection<Map.Entry<Department, PhoneNumber>> entries =
            (Collection<Map.Entry<Department, PhoneNumber>>) phones.entrySet();
        for (Map.Entry<Department, PhoneNumber> entry : entries) {
            Department key = entry.getKey();
            PhoneNumber p = entry.getValue();
            PhoneNumber p0 = map.get(key);
            if (p.getNumber() != p0.getNumber())
                return false;
        }
        return true;
    }
}
