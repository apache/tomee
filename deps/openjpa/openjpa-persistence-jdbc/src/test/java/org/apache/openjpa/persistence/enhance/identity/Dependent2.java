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
@Table(name="DEP2_MBI")
public class Dependent2 {
    @EmbeddedId
    DependentId2 id;
    
    @MapsId("empPK")
    @ManyToOne Employee2 emp;
    
    public Employee2 getEmp() {
        return emp;
    }
    
    public void setEmp(Employee2 emp) {
        this.emp = emp;
    }
    
    public DependentId2 getId() {
        return id;
    }
    
    public void setId(DependentId2 id) {
        this.id = id;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Dependent2)) return false;
        Dependent2 d0 = (Dependent2)o;
        DependentId2 id0 = d0.getId();
        if (id == null && id0 != null) return false;
        if (!id.equals(id0)) return false;
        Employee2 e0 = d0.getEmp();
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
