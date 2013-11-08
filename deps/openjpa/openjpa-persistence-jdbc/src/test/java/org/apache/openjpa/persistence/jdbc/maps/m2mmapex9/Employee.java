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
package org.apache.openjpa.persistence.jdbc.maps.m2mmapex9;

import javax.persistence.*;


import java.util.*;

@Entity
@Table(name="MEx9Emp")
public class Employee {
    @Id
    int empId;

    @ManyToMany
    //@AttributeOverrides({
    //    @AttributeOverride(name="fName", column=@Column(name="fName_Emp")),
    //    @AttributeOverride(name="lName", column=@Column(name="lName_Emp"))
    //})
    Map<FullPhoneName, PhoneNumber> phones =
        new HashMap<FullPhoneName, PhoneNumber>(); // Bidirectional

    public Map<FullPhoneName, PhoneNumber> getPhoneNumbers() {
        return phones;
    }

    public void addPhoneNumber(FullPhoneName name, PhoneNumber phoneNumber) {
        phones.put(name, phoneNumber);
    }

    public void removePhoneNumber(FullPhoneName name) {
        phones.remove(name);
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public boolean equals(Object o) {
        Employee e = (Employee) o;
        Map<FullPhoneName, PhoneNumber> map = e.getPhoneNumbers();
        if (map.size() != phones.size())
            return false;
        Collection<Map.Entry<FullPhoneName, PhoneNumber>> entries =
            (Collection<Map.Entry<FullPhoneName, PhoneNumber>>)
            phones.entrySet();
        for (Map.Entry<FullPhoneName, PhoneNumber> entry : entries) {
            FullPhoneName key = entry.getKey();
            PhoneNumber p = entry.getValue();
            PhoneNumber p0 = map.get(key);
            if (p.getNumber() != p0.getNumber())
                return false;
        }
        return true;
    }
}
