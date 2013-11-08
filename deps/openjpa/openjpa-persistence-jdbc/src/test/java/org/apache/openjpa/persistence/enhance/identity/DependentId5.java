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

public class DependentId5 {
    String name;
    EmployeeId5 emp;

    public DependentId5() {
    }

    public DependentId5(String name, EmployeeId5 emp) {
        this.name = name;
        this.emp = emp;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public EmployeeId5 getEmp() {
        return emp;
    }
    
    public void setEmp(EmployeeId5 emp) {
        this.emp = emp;
    }
    
    public int hashCode() {
        return name.hashCode() + emp.hashCode();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof DependentId5)) return false;
        DependentId5 d = (DependentId5) o;
        if (!emp.equals(d.emp)) return false;
        if (!name.equals(d.name)) return false;
        return true;
    }
}
