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

import java.util.List;

import javax.persistence.*;

/**
 * This is the non-default uni-directional one-to-many mapping.
 * Foreign key strategy is used. 
 * @JoinColumn is required for this strategy (see Spec 11.1.36, Ex 3)
 * with @OneToMany. 
 * @author faywang
 */

@Entity
public class Uni_1ToM_FK {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @OneToMany(fetch=FetchType.EAGER)
    @JoinColumn(name="Uni1MFK_ID")
    private List<EntityC_U1MFK> entityCs = null;
    
    public long getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public List<EntityC_U1MFK> getEntityCs() { 
        return entityCs; 
    }

    public void setEntityCs(List<EntityC_U1MFK> entityCs) { 
        this.entityCs = entityCs; 
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Uni_1ToM_FK)) return false;
        Uni_1ToM_FK b = (Uni_1ToM_FK)o;
        if (!b.name.equals(name)) return false;
        if (b.entityCs.size() != entityCs.size()) return false;
        if (b.entityCs.size() == 1) {
            if (!b.entityCs.get(0).getName().equals(entityCs.get(0).getName()))
                return false;
        }
        return true;
    }
}
