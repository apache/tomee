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
package org.apache.openjpa.persistence.jdbc.maps.spec_10_1_27_ex0;

import javax.persistence.*;

@Entity
@Table(name="S27x0VP")
public class VicePresident {
    @Id
    int id;
    
    @Column(length=8)
    String name;
  
    @ManyToOne
    Compny1 co;
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Compny1 getCompany() {
        return co;
    }
    
    public void setCompany(Compny1 co) {
        this.co = co;
    }

    public boolean equals(Object v) {
        if (this == v)
            return true;
        if (!(v instanceof VicePresident))
            return false;
        VicePresident o = (VicePresident) v;
        if (this.id == o.getId() &&
            this.name.equals(o.getName()))
            return true;
        return false;
    }
}
