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

import java.util.Map;
import java.util.Set;

import javax.persistence.*;

//non-default mapping
//Sec 11.1.36, Example 3: 
//    Unidirectional One-to-Many association using a foreign key mapping
//    In Customer class:
//    @OneToMany(orphanRemoval=true)
//    @JoinColumn(name="CUST_ID") // join column is in table for Order
//    public Set<Order> getOrders() {return orders;}

@Entity
public class Uni_1ToM_Map_RelKey_FK {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    @JoinColumn(name="Uni1MFK_ID")
    private Map<EntityC, EntityC_U1M_Map_RelKey_FK> entityCs = null;
    
    public long getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public Map<EntityC, EntityC_U1M_Map_RelKey_FK> getEntityCs() { 
        return entityCs; 
    }

    public void setEntityCs(Map<EntityC, EntityC_U1M_Map_RelKey_FK> entityCs) { 
        this.entityCs = entityCs; 
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Uni_1ToM_Map_RelKey_FK)) return false;
        Uni_1ToM_Map_RelKey_FK b = (Uni_1ToM_Map_RelKey_FK)o;
        if (!b.name.equals(name)) return false;
        if (b.entityCs.size() != entityCs.size()) return false;
        
        Set<EntityC> coll = b.entityCs.keySet();
        for (EntityC cKey : coll) {
            EntityC_U1M_Map_RelKey_FK val = (EntityC_U1M_Map_RelKey_FK)getValue(b.entityCs, cKey);
            EntityC_U1M_Map_RelKey_FK val1 = (EntityC_U1M_Map_RelKey_FK)getValue(entityCs, cKey); 
            if (!val.equals(val1))
                return false;
        }
        return true;
    }
    
    private Object getValue(Map map, Object mkey) {
        Set<Map.Entry> entries = map.entrySet();
        for (Map.Entry entry : entries) {
            if (entry.getKey().equals(mkey))
                return entry.getValue();
        }
        return null;
    }
}
