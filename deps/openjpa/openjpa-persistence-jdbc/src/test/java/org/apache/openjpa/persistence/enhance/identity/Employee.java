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
package org.apache.openjpa.persistence.enhance.identity;

import javax.persistence.*;


@Entity
@Table(name="EMP_MBI")
@IdClass(EmpId.class)
public class Employee {
    @Id
    int empId;
    
    @Id
    @Enumerated
    EmpType empType;

    @OneToOne
    PhoneNumber phoneNumber; 
    
    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
      
    public int getEmpId() {
        return empId;
    }
    
    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public EmpType getEmpType() {
        return empType;
    }
    
    public void setEmpType(EmpType empType) {
        this.empType = empType;
    }

    public boolean equals(Object o) {
        Employee e = (Employee) o;
        PhoneNumber p = e.getPhoneNumber();
        if (p.getPhNumber() != phoneNumber.getPhNumber())
            return false;
        
        return true;
    }
    
    public enum EmpType { A1, A2, A3, A4 };
    
    
}
