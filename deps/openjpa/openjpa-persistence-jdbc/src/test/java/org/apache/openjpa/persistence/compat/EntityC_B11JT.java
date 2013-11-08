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
package org.apache.openjpa.persistence.compat;

import javax.persistence.*;

@Entity
public class EntityC_B11JT {

    @Id
    @GeneratedValue
    private long id;

    private String name;
    
    @OneToOne(mappedBy="entityC")
    private Bi_1To1_JT bi11jt;
    
    public long getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public void setBi11jt(Bi_1To1_JT bi11jt) {
        this.bi11jt = bi11jt;
    }
    
    public Bi_1To1_JT getBi11jt() {
        return bi11jt;
    }
    
    public int hashCode() {
        return name.hashCode() + (int)id;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof EntityC_B11JT)) return false;
        EntityC_B11JT c = (EntityC_B11JT)o;
        if (!c.name.equals(name)) return false;
        if (c.id != id) return false;
        if (c.bi11jt == null && bi11jt == null) return true;
        if (!c.bi11jt.equals(bi11jt)) return false;
        return true;
    }
}
