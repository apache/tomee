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
package org.apache.openjpa.persistence.access;

import java.util.Collection;

import javax.persistence.Access;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.AccessType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@Access(AccessType.FIELD)
public class FieldAccessPropStratsEntity {

    @EmbeddedId
    private EmbedId eid;

    @Transient
    private String name;

    @Transient
    private PropAccess m2one;

    @Transient
    private Collection<FieldAccess> one2m;
    
    @Transient
    private PropAccess one2one;
    
    @Transient
    private Collection<EmbedPropAccess> ecoll;
    
    @Transient
    private EmbedFieldAccess embed;
    
    @Transient
    private int ver;
    
    @Transient
    private Collection<PropAccess> m2m;

    @ElementCollection
    @Access(AccessType.PROPERTY)
    public Collection<EmbedPropAccess> getElementCollection() {
        return ecoll;
    }

    public void setElementCollection(Collection<EmbedPropAccess> coll) {
        ecoll = coll;
    }
    
    @Embedded
    @Access(AccessType.PROPERTY)
    public EmbedFieldAccess getEmbedField() {
        return embed;
    }
    
    public void setEmbedField(EmbedFieldAccess efa) {
        embed = efa;
    }
    
    @Version
    @Access(AccessType.PROPERTY)
    public int getVersion() {
        return ver;
    }
    
    public void setVersion(int version) {
        ver = version;
    }
    
    @ManyToOne(cascade=CascadeType.ALL)
    @Access(AccessType.PROPERTY)
    public PropAccess getManyToOne() {
        return m2one;
    }

    public void setManyToOne(PropAccess pa) {
        m2one = pa;
    }

    @OneToMany(cascade=CascadeType.ALL)
    @Access(AccessType.PROPERTY)
    public Collection<FieldAccess> getOneToMany() {
        return one2m;
    }

    public void setOneToMany(Collection<FieldAccess> c) {
        one2m = c;
    }

    @OneToOne(cascade=CascadeType.ALL)
    @Access(AccessType.PROPERTY)
    public PropAccess getOneToOne() {
        return one2one;
    }
    
    public void setOneToOne(PropAccess pa) {
        one2one = pa;
    }
    
    @ManyToMany(cascade=CascadeType.ALL)
    @Access(AccessType.PROPERTY)
    public Collection<PropAccess> getManyToMany() {
        return m2m;
    }

    public void setManyToMany(Collection<PropAccess> many) {
        m2m = many;
    }
    
    @Basic
    @Access(AccessType.PROPERTY)
    public String getName() {
        return name;
    }
    
    public void setName(String n) {
        name = n;
    }

    public void setEmbedId(EmbedId eid) {
        this.eid = eid;
    }

    @EmbeddedId
    @Access(AccessType.FIELD)
    public EmbedId getEmbedId() {
        return eid;
    }
}
