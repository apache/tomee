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
package org.apache.openjpa.persistence.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

@Entity(name="oaopeEntityB")
public class EntityB {
    private static final long serialVersionUID = 0L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    private EntityA center;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn
    private Set<EntityC> linacs;

    @Column(name = "POSITION")
    private int position;


    public EntityB() {
    }

    public EntityB(EntityA center) {
        this.center = center;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityA getCenter() {
        return center;
    }

    public void setCenter(EntityA center) {
        this.center = center;
    }

    public Set<EntityC> getCs() {
        if (linacs == null)
            linacs = new HashSet<EntityC>();
        return linacs;
    }
}
