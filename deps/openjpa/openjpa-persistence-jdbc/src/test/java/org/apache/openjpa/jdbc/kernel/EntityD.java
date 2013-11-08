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
package org.apache.openjpa.jdbc.kernel;

import org.apache.openjpa.persistence.jdbc.ForeignKey;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;

@Entity
public class EntityD {

    @Id
    @Column(name = "entityd_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    @OneToOne
    @JoinColumn(name = "entitya_id", referencedColumnName = "entitya_id")
    @ForeignKey
    private EntityA entityA;
    
    @OneToOne
    @JoinColumn(name = "entityb_id", referencedColumnName = "entityb_id")
    @ForeignKey
    private EntityB entityB;
    
    @Version
    private Integer optLock;

    public EntityD() {
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public EntityA getEntityA() {
        return this.entityA;
    }

    public void setEntityA(EntityA entityA) {
        this.entityA = entityA;
    }

    public EntityB getEntityB() {
        return entityB;
    }

    public void setEntityB(EntityB entityB) {
        this.entityB = entityB;
    }
    
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

