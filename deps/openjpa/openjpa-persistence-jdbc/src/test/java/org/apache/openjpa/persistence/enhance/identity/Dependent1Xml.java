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


public class Dependent1Xml {
    DependentId1Xml id;
    
    Employee1Xml emp;
    
    public Employee1Xml getEmp() {
        return emp;
    }
    
    public void setEmp(Employee1Xml emp) {
        this.emp = emp;
    }
    
    public DependentId1Xml getId() {
        return id;
    }
    
    public void setId(DependentId1Xml id) {
        this.id = id;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Dependent1Xml)) return false;
        Dependent1Xml d0 = (Dependent1Xml)o;
        DependentId1Xml id0 = d0.getId();
        if (!id.equals(id0)) return false;
        Employee1Xml e0 = d0.getEmp();
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
