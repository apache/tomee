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
@Table(name="DEP1_MBI")
public class Dependent1 {
    @EmbeddedId
    DependentId1 id;
    
    @MapsId("empPK")
    @ManyToOne Employee1 emp;
    
    public Employee1 getEmp() {
        return emp;
    }
    
    public void setEmp(Employee1 emp) {
        this.emp = emp;
    }
    
    public DependentId1 getId() {
        return id;
    }
    
    public void setId(DependentId1 id) {
        this.id = id;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Dependent1)) return false;
        Dependent1 d0 = (Dependent1)o;
        DependentId1 id0 = d0.getId();
        if (!id.equals(id0)) return false;
        Employee1 e0 = d0.getEmp();
        if (emp != null && !emp.equals(e0)) return false;
        if (emp == null && e0 != null) return false;
        return true;
    }
    
    public int hashCode() {
        int ret = 0;
        ret = ret * 31 + id.hashCode();
        ret = ret * 31 + emp.hashCode();
        return ret;
    }
    
}
