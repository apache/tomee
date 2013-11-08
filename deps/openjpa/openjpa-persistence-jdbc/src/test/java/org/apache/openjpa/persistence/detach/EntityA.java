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
package org.apache.openjpa.persistence.detach;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;

@Entity
@Table(name="EntityA_detach")
@FetchGroups({
    @FetchGroup(name = "loadD", attributes = {
    @FetchAttribute(name = "entityD", recursionDepth = 0)
        })
        })
public class EntityA {
    @Id
    private long id;
    
    private String name;
    
    @Basic(fetch=FetchType.LAZY)
    private String description;
    
    @OneToOne(cascade=(CascadeType.ALL))
    EntityB entityB;
    
    @OneToOne(cascade=(CascadeType.PERSIST))
    EntityC entityC;
    
    @OneToOne(cascade=(CascadeType.ALL), fetch=FetchType.LAZY)
    EntityD entityD;
    
    @OneToOne(cascade=(CascadeType.PERSIST), fetch=FetchType.LAZY)
    EntityE entityE;
    
    public EntityA() {
    }
    
    public EntityA(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the entityB
     */
    public EntityB getEntityB() {
        return entityB;
    }

    /**
     * @param entityB the entityB to set
     */
    public void setEntityB(EntityB entityB) {
        this.entityB = entityB;
    }

    /**
     * @return the entityC
     */
    public EntityC getEntityC() {
        return entityC;
    }

    /**
     * @param entityC the entityC to set
     */
    public void setEntityC(EntityC entityC) {
        this.entityC = entityC;
    }

    /**
     * @return the entityD
     */
    public EntityD getEntityD() {
        return entityD;
    }

    /**
     * @param entityD the entityD to set
     */
    public void setEntityD(EntityD entityD) {
        this.entityD = entityD;
    }

    /**
     * @return the entityE
     */
    public EntityE getEntityE() {
        return entityE;
    }

    /**
     * @param entityE the entityE to set
     */
    public void setEntityE(EntityE entityE) {
        this.entityE = entityE;
    }

}
