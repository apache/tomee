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
package org.apache.openjpa.persistence.delimited.identifiers.noschema;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name="\"nsentity d\"")
public class EntityD {
    @Id
    private int id;
    private String name;
    @ManyToMany(mappedBy="entityDs")
    private Collection<EntityC> entityCs = new HashSet<EntityC>();
    
    public EntityD() {}
    
    public EntityD(int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the entityCs
     */
    public Collection<EntityC> getEntityCs() {
        return entityCs;
    }

    /**
     * @param entityCs the entityCs to set
     */
    public void setEntityCs(Collection<EntityC> entityCs) {
        this.entityCs = entityCs;
    }
    
    public void addEntityC(EntityC entityC) {
        entityCs.add(entityC);
    }
}
