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

import java.util.*;

@Entity
@Table(name="EMP3_MBI")
public class Employee3 {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    int empId;
    
    String name;
    
    @OneToMany(mappedBy="emp")
    List<Dependent3> dependents = new ArrayList<Dependent3>();
    
    public int getEmpId() {
        return empId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<Dependent3> getDependents() {
        return dependents;
    }
    
    public void setDependents(List<Dependent3> dependents) {
        this.dependents = dependents;
    }
    
    public void addDependent(Dependent3 d) {
        dependents.add(d);
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Employee3)) return false;
        Employee3 e = (Employee3)o;
        if (empId != e.getEmpId()) return false;
        if (name != null && !name.equals(e.getName())) return false;
        if (name == null && e.getName() != null) return false;
        List<Dependent3> ds0 = e.getDependents();
        if (ds0 != null && ds0.size() != 0 && dependents == null) return false; 
        if (ds0 == null && dependents != null && dependents.size() != 0)
            return false;
        if (ds0 == null && dependents == null) return true;
        if (ds0 != null && dependents != null) { 
            if (ds0.size() != dependents.size()) return false;
        }
        return true;
    }
    
    public int hashCode() {
        int ret = 0;
        ret = ret * 31 + empId;
        ret = ret * 31 + name.hashCode();
        if (dependents != null)
            for (Dependent3 d : dependents)
                ret = ret * 31 + d.id.hashCode();
        return ret;
    }
}
