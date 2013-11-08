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
@Table(name="PRT3_MBI")
public class Parent3 {
    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    int pid;
    
    String name;
    
    @OneToOne(mappedBy="parent")
    Dependent3 dependent;
    
    public int getPid() {
        return pid;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Dependent3 getDependent() {
        return dependent;
    }
    
    public void setDependent(Dependent3 dependent) {
        this.dependent = dependent;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Parent3)) return false;
        Parent3 e = (Parent3)o;
        if (pid != e.getPid()) return false;
        if (name != null && !name.equals(e.getName())) return false;
        if (name == null && e.getName() != null) return false;
        Dependent3 d0 = e.getDependent();
        if (!dependent.id.equals(d0.id)) return false;
        return true;
    }
    
    public int hashCode() {
        int ret = 0;
        ret = ret * 31 + pid;
        ret = ret * 31 + name.hashCode();
        if (dependent != null)
            ret = ret * 31 + dependent.id.hashCode();
        return ret;
    }
}
