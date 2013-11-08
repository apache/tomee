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
package org.apache.openjpa.persistence.kernel.common.apps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.KeyColumn;
import org.apache.openjpa.persistence.Dependent;
import org.apache.openjpa.persistence.ElementDependent;
import org.apache.openjpa.persistence.PersistentMap;
import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;
import org.apache.openjpa.persistence.jdbc.ForeignKey;
import org.apache.openjpa.persistence.jdbc.KeyColumn;

/**
 * <p>Persistent type used in testing dependent field deletion through
 * {@link TestDependentFields}.</p>
 *
 * @author Abe White
 */
@Entity
@Table(name = "DEPFIELDPC")
public class DependentFieldsPC {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private long pk;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private DependentFieldsPC relation = null;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    //@ForeignKey(deleteAction=ForeignKeyAction.RESTRICT)
    @ForeignKey
    private DependentFieldsPC owner = null;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private List<DependentFieldsPC> list = new ArrayList();

    @PersistentMap(elementCascade = CascadeType.PERSIST)
    @KeyColumn(name = "depfpc")
    private Map<String, DependentFieldsPC> map = new HashMap();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Dependent
    private DependentFieldsPC dependentRelation = null;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @ElementDependent
    private List<DependentFieldsPC> dependentList = new ArrayList();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.PERSIST)
    @ElementDependent
    private List<DependentFieldsPC> dependentMappedList = new ArrayList();

    @OneToMany(cascade = CascadeType.PERSIST)
    @ElementDependent
    @ElementJoinColumn(name = "DEP_INV_KEY", referencedColumnName = "ID")
    private List<DependentFieldsPC> dependentInverseKeyList = new ArrayList();

    @PersistentMap(elementCascade = CascadeType.PERSIST)
    @ElementDependent
    @KeyColumn(name = "depmap")
    private Map<String, DependentFieldsPC> dependentMap = new HashMap();

    public DependentFieldsPC() {
    }

    public long getPK() {
        return this.pk;
    }

    public void setPK(long pk) {
        this.pk = pk;
    }

    public DependentFieldsPC getRelation() {
        return this.relation;
    }

    public void setRelation(DependentFieldsPC relation) {
        this.relation = relation;
    }

    public DependentFieldsPC getOwner() {
        return this.owner;
    }

    public void setOwner(DependentFieldsPC owner) {
        this.owner = owner;
    }

    public List getList() {
        return this.list;
    }

    public Map getMap() {
        return this.map;
    }

    public DependentFieldsPC getDependentRelation() {
        return this.dependentRelation;
    }

    public void setDependentRelation(DependentFieldsPC relation) {
        this.dependentRelation = relation;
    }

    public List getDependentList() {
        return this.dependentList;
    }

    public List getDependentMappedList() {
        return this.dependentMappedList;
    }

    public List getDependentInverseKeyList() {
        return this.dependentInverseKeyList;
    }

    public Map getDependentMap() {
        return this.dependentMap;
    }
}
