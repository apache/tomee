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
@Table(name="PER1_MBI")
public class Person1 {
    @EmbeddedId
    PersonId1 id;
         
    @OneToOne(mappedBy="patient")
    MedicalHistory1 medical;
    
    public PersonId1 getId() {
        return id;
    }
    
    public void setId(PersonId1 id) {
        this.id = id;
    }
    
    public MedicalHistory1 getMedical() {
        return medical;
    }
    
    public void setMedical(MedicalHistory1 medical) {
        this.medical = medical;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Person1)) return false;
        Person1 p0 = (Person1)o;
        PersonId1 id0 = p0.getId();
        MedicalHistory1 medical0 = p0.getMedical();
        if (id != null && !id.equals(id0)) return false;
        if (medical != null && medical0 != null && 
            !medical.getId().equals(medical0.getId())) return false; 
        if (medical == null && medical0 != null) return false;
        if (medical != null && medical0 == null) return false;
        return true;
    }
    
    public int hashCode() {
        int ret = 0;
        ret = ret * 31 + id.hashCode();
        if (medical != null)
            ret = ret * 31 + medical.hashCode();
        return ret;
    }
}
