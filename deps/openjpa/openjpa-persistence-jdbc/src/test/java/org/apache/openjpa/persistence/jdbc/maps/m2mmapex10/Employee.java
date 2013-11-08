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
@Table(name="M10Emp")
public class Employee {
    @EmbeddedId
    EmployeePK empPK;

    @ManyToMany // Bidirectional
    Map<PhonePK, PhoneNumber> phones = new HashMap<PhonePK, PhoneNumber>();

    int salary;

    public EmployeePK getEmpPK() {
        return empPK;
    }

    public void setEmpPK(EmployeePK empPK) {
        this.empPK = empPK;
    }


    public Map<PhonePK, PhoneNumber> getPhoneNumbers() {
        return phones;
    }

    public void addPhoneNumber(PhonePK d, PhoneNumber phoneNumber) {
        phones.put(d, phoneNumber);
    }

    public void removePhoneNumber(PhonePK d) {
        phones.remove(d);
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public boolean equals(Object o) {
        Employee e = (Employee) o;
        Map<PhonePK, PhoneNumber> map = e.getPhoneNumbers();
        if (map.size() != phones.size())
            return false;
        Collection<Map.Entry<PhonePK, PhoneNumber>> entries =
            (Collection<Map.Entry<PhonePK, PhoneNumber>>) phones.entrySet();
        for (Map.Entry<PhonePK, PhoneNumber> entry : entries) {
            PhonePK key = entry.getKey();
            PhoneNumber p = entry.getValue();
            PhoneNumber p0 = map.get(key);
            if (!p.getPhonePK().equals(p0.getPhonePK()))
                return false;
        }
        return true;
    }    

    /*
     * The following change is for the comparison of Date object. In MySQL, 
     * "microseconds cannot be stored into a column of any temporal data type. 
     * Any microseconds part is discarded. " (http://dev.mysql.com/doc/refman/5.1/en/datetime.html). 
     * As a result, when the value retrieved from the database will be different from the 
     * original value in the memory for the loss of microsecond. The fix is to call toString 
     * (which will strip the microseconds0 on the Date object and compare the String values.
     */
    
    public static Employee findEmpl(Map<EmployeePK, Employee> map, EmployeePK key) {
        String name = key.getName();
        String bDateStr = key.getBDay().toString();
        Set<EmployeePK> keys = map.keySet();
        for (EmployeePK thisKey : keys) {
           if (name.equals(thisKey.getName()) && 
               bDateStr.equals(thisKey.getBDay().toString())) {
               return map.get(thisKey);
           }
        }
        return null;
    }
}
