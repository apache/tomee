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

import java.util.Collection;
import java.util.Map;

import javax.persistence.*;



@Entity
public class Bi_1ToM_Map_JT {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @OneToMany(mappedBy="bi1mjt", fetch=FetchType.EAGER)
    private Map<String, EntityC_B1M_Map_JT> entityCs = null;
    
    public long getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public Map<String, EntityC_B1M_Map_JT> getEntityCs() { 
        return entityCs; 
    }

    public void setEntityCs(Map<String, EntityC_B1M_Map_JT> entityCs) { 
        this.entityCs = entityCs; 
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Bi_1ToM_Map_JT)) return false;
        Bi_1ToM_Map_JT b = (Bi_1ToM_Map_JT)o;
        if (!b.name.equals(name)) return false;
        if (b.entityCs.size() != entityCs.size()) return false;
        Collection<EntityC_B1M_Map_JT> coll = b.entityCs.values();
        for (EntityC_B1M_Map_JT c : coll) {
            if (!b.entityCs.get(c.getName()).equals(entityCs.get(c.getName())))
                return false;
        }
        return true;
    }
}
