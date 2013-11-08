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



@Entity
public class Bi_1ToM_Map_RelKey_JT {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @OneToMany(mappedBy="bi1mjt", fetch=FetchType.EAGER)
    private Map<EntityC, EntityC_B1M_Map_RelKey_JT> entityCs = null;
    
    public long getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public Map<EntityC, EntityC_B1M_Map_RelKey_JT> getEntityCs() { 
        return entityCs; 
    }

    public void setEntityCs(Map<EntityC, EntityC_B1M_Map_RelKey_JT> entityCs) { 
        this.entityCs = entityCs; 
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Bi_1ToM_Map_RelKey_JT)) return false;
        Bi_1ToM_Map_RelKey_JT b = (Bi_1ToM_Map_RelKey_JT)o;
        if (!b.name.equals(name)) return false;
        if (b.entityCs.size() != entityCs.size()) return false;
        Set<EntityC> coll = b.entityCs.keySet();
        for (EntityC cKey : coll) {
            Object val = getValue(b.entityCs, cKey); //b.entityCs.get(cKey);
            Object val1 = getValue(entityCs, cKey); //entityCs.get(cKey);
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
