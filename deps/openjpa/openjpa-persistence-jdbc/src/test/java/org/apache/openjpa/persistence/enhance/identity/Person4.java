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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="PER4_MBI")
public class Person4 {
    @EmbeddedId
    PersonId4 id;
    
    @OneToOne(mappedBy="patient")
    MedicalHistory4 medical;
    
    public PersonId4 getId() {
        return id;
    }
    
    public void setId(PersonId4 id) {
        this.id = id;
    }
    
    public MedicalHistory4 getMedical() {
        return medical;
    }
    
    public void setMedical(MedicalHistory4 medical) {
        this.medical = medical;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Person4)) return false;
        Person4 p0 = (Person4)o;
        PersonId4 id0 = p0.getId();
        if (!id.equals(id0)) return false;
        MedicalHistory4 medical0 = p0.getMedical();
        if (medical != null && 
            !medical.patient.getId().equals(medical0.patient.getId()))
            return false; 
        if (medical == null && medical0 != null) return false;
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
