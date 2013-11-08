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
package org.apache.openjpa.persistence.entityoperation.common.apps;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.apache.openjpa.persistence.Dependent;

@Entity
public class CascadesEntity {

    private long id;
    private String name;
    private CascadesEntity none;
    private CascadesEntity all;
    private CascadesEntity dependent;
    private Collection<CascadesEntity> noneCollection = new ArrayList();
    private Collection<CascadesEntity> allCollection = new ArrayList();

    @Id
    @GeneratedValue
    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne
    public CascadesEntity getNone() {
        return this.none;
    }

    public void setNone(CascadesEntity none) {
        this.none = none;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    public CascadesEntity getAll() {
        return this.all;
    }

    public void setAll(CascadesEntity all) {
        this.all = all;
    }

    @ManyToMany
    @JoinTable(name = "CASCADES_NONE_COLL",
        joinColumns = @JoinColumn(name = "owner"))
    public Collection<CascadesEntity> getNoneCollection() {
        return this.noneCollection;
    }

    public void setNoneCollection(Collection<CascadesEntity> noneCollection) {
        this.noneCollection = noneCollection;
    }

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "CASCADES_ALL_COLL",
        joinColumns = @JoinColumn(name = "owner"))
    public Collection<CascadesEntity> getAllCollection() {
        return this.allCollection;
    }

    public void setAllCollection(Collection<CascadesEntity> allCollection) {
        this.allCollection = allCollection;
    }

    @ManyToOne
    @Dependent
    public CascadesEntity getDependent() {
        return this.dependent;
    }

    public void setDependent(CascadesEntity dependent) {
        this.dependent = dependent;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
