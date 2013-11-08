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
@Table(name="DEP3_MBI")
public class Dependent3 {
    @EmbeddedId
    DependentId3 id;
    
    @MapsId("empPK")
    @ManyToOne Employee3 emp;
    
    @MapsId("parentPK")
    @OneToOne Parent3 parent;
    
    public Employee3 getEmp() {
        return emp;
    }
    
    public void setEmp(Employee3 emp) {
        this.emp = emp;
    }
    
    public Parent3 getParent() {
        return parent;
    }
    
    public void setParent(Parent3 parent) {
        this.parent = parent;
    }
    
    public DependentId3 getId() {
        return id;
    }
    
    public void setId(DependentId3 id) {
        this.id = id;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Dependent3)) return false;
        Dependent3 d0 = (Dependent3)o;
        DependentId3 id0 = d0.getId();
        if (!id.equals(id0)) return false;
        Employee3 e0 = d0.getEmp();
        if (emp != null && !emp.equals(e0)) return false;
        if (emp == null && e0 != null) return false;
        Parent3 p0 = d0.getParent();
        if (parent != null && !parent.equals(p0)) return false;
        if (parent == null && p0 != null) return false;
        return true;
    }
    
    public int hashCode() {
        int ret = 0;
        ret = ret * 31 + id.hashCode();
        ret = ret * 31 + emp.hashCode();
        ret = ret * 31 + parent.hashCode();
        return ret;
    }
    
}
