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
@Table(name="PER3_MBI")
@IdClass(PersonId3.class)
public class Person3 {
    @Id
    String firstName;
    
    @Id
    String lastName;
        
    @OneToOne(mappedBy="patient")
    MedicalHistory3 medical;
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public MedicalHistory3 getMedical() {
        return medical;
    }
    
    public void setMedical(MedicalHistory3 medical) {
        this.medical = medical;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Person3)) return false;
        Person3 p0 = (Person3)o;
        String firstName0 = p0.getFirstName();
        String lastName0 = p0.getLastName();
        MedicalHistory3 medical0 = p0.getMedical();
        if (!firstName.equals(firstName0)) return false;
        if (!lastName.equals(lastName0)) return false;
        if (medical != null && medical0 != null &&
            !medical.name.equals(medical0.name)) return false;
        if (medical == null && medical0 != null) return false;
        if (medical != null && medical0 == null) return false;
        return true;
    }
    
    public int hashCode() {
        int ret = 0;
        ret = ret * 31 + firstName.hashCode();
        ret = ret * 31 + lastName.hashCode();
        if (medical != null)
        	ret = ret * 31 + medical.hashCode();
        return ret;
    }
}
