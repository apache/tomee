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
@Table(name="MED4_MBI")
@IdClass(PersonId4.class)
public class MedicalHistory4 {
    String name;
    
    @Id
    @OneToOne Person4 patient;
    
    public Person4 getPatient() {
        return patient;
    }
    
    public void setPatient(Person4 p) {
        this.patient = p;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof MedicalHistory4)) return false;
        MedicalHistory4 m0 = (MedicalHistory4)o;
        String name0 = m0.getName();
        if (name != null && !name.equals(name0)) return false;
        if (name == null && name0 != null) return false;
        Person4 p0 = m0.getPatient();
        if (patient != null && !patient.equals(p0)) return false;
        if (patient == null && p0 != null) return false;
        return true;
    }
    
    public int hashCode() {
        int ret = 0;
        ret = ret * 31 + name.hashCode();
        ret = ret * 31 + patient.id.firstName.hashCode();
        ret = ret * 31 + patient.id.lastName.hashCode();
        return ret;
    }
    
}
