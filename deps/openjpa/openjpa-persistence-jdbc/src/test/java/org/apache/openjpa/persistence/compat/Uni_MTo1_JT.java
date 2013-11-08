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
public class Uni_MTo1_JT {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @ManyToOne
    @JoinTable(
        name="UniM1JT_C",
        joinColumns=
          @JoinColumn(name="U_ID", referencedColumnName="ID"),
        inverseJoinColumns=
          @JoinColumn(name="C_ID", referencedColumnName="ID")
    )
    private EntityC entityC;
    
    public long getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public EntityC getEntityC() { 
        return entityC; 
    }

    public void setEntityC(EntityC entityC) { 
        this.entityC = entityC; 
    }

    public int hashCode() {
        return name.hashCode() + (int)id;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Uni_MTo1_JT)) return false;
        Uni_MTo1_JT c = (Uni_MTo1_JT)o;
        if (!c.name.equals(name)) return false;
        if (c.id != id) return false;
        if (c.entityC == null && entityC == null) return true;
        if (!c.entityC.equals(entityC)) return false;
        return true;
    }
}
