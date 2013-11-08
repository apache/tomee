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
@Table(name="PER2_MBI")
public class Person2 {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    long ssn;
    
    @OneToOne(mappedBy="patient")
    MedicalHistory2 medical;
    
    String name;
    
    public long getSsn() {
        return ssn;
    }
    
    public void setSsn(long ssn) {
        this.ssn = ssn;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public MedicalHistory2 getMedical() {
        return medical;
    }
    
    public void setMedical(MedicalHistory2 medical) {
        this.medical = medical;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Person2)) return false;
        Person2 p0 = (Person2)o;
        long ssn0 = p0.getSsn();
        String name0 = p0.getName();
        MedicalHistory2 medical0 = p0.getMedical();
        if (ssn != ssn0) return false;
        if (name != name0) return false;
        if (medical != null && medical0 != null && medical.id != medical0.id)
            return false; 
        if (medical == null && medical0 != null) return false;
        if (medical != null && medical0 == null) return false;
        return true;
    }
    
    public int hashCode() {
        int ret = 0;
        ret = ret * 31 + (int) ssn;
        if (medical != null)
            ret = ret * 31 + medical.hashCode();
        return ret;
    }
}
