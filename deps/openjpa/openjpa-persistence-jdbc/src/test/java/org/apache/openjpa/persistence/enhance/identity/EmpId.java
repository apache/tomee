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

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class EmpId {
    int empId;
    
    @Enumerated(EnumType.ORDINAL)  
    Employee.EmpType empType;
    
    public int getEmpId() {
        return empId;
    }
    
    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public Employee.EmpType getEmpType() {
        return empType;
    }
    
    public void setEmpType(Employee.EmpType empType) {
        this.empType = empType;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof EmpId)) return false;
        if (((EmpId)o).empId == empId &&
            ((EmpId)o).empType == empType) return true;
        return false;
    }
    
    public int hashCode() {
        return empId *31 + empType.hashCode();
    }
    
}
