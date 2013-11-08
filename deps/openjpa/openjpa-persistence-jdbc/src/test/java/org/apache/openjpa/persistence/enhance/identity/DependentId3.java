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

@Embeddable
public class DependentId3 {
    String name;
    long empPK;
    long parentPK;
    
    public DependentId3() {}
    
    public DependentId3(String name, long empPK,
    	long parentPK) {
        this.name = name;
        this.empPK = empPK;
        this.parentPK = parentPK;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setEmpPK(long empPK) {
        this.empPK = empPK;
    }
    
    public long getEmpPK() {
        return empPK;
    }
    
    public void setParentPK(long parentPK) {
        this.parentPK = parentPK;
    }
    
    public long getParentPK() {
        return parentPK;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof DependentId3)) return false;
        DependentId3 d = (DependentId3)o;
        if (empPK != d.getEmpPK()) return false;
        if (parentPK != d.getParentPK()) return false;
        if (name != null && !name.equals(d.getName())) return false;
        if (name == null && d.getName() != null) return false;
        return true;
    }
    
    public int hashCode() {
        int ret = 0;
        ret = (int) (ret * 31 + empPK);
        ret = (int) (ret * 31 + parentPK);
        ret = ret * 31 + name.hashCode();
        return ret;
    }
    
}
