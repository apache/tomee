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
@Access(AccessType.PROPERTY)
public class PropAccessFieldStratsEntity {

    @Transient
    private EmbedId eid;

    @Basic
    @Access(AccessType.FIELD)
    private String name;
    
    @ManyToOne(cascade=CascadeType.ALL)
    @Access(AccessType.FIELD)
    private PropAccess m2one;

    @OneToMany(cascade=CascadeType.ALL)
    @Access(AccessType.FIELD)
    private Collection<FieldAccess> one2m;
    
    @OneToOne(cascade=CascadeType.ALL)
    @Access(AccessType.FIELD)
    private PropAccess one2one;
    
    @ElementCollection
    @Access(AccessType.FIELD)
    private Collection<EmbedPropAccess> ecoll;
    
    @Embedded
    @Access(AccessType.FIELD)
    private EmbedFieldAccess embed;
    
    @Version
    @Access(AccessType.FIELD)
    private int ver;

    @ManyToMany(cascade=CascadeType.ALL)
    @Access(AccessType.FIELD)
    private Collection<PropAccess> m2m;

    @Transient
    public Collection<EmbedPropAccess> getElementCollection() {
        return ecoll;
    }

    public void setElementCollection(Collection<EmbedPropAccess> elc) {
        ecoll = elc;
    }

    @Transient
    public EmbedFieldAccess getEmbedField() {
        return embed;
    }
    
    public void setEmbedField(EmbedFieldAccess efa) {
        embed = efa;
    }
    
    @Transient
    public int getVersion() {
        return ver;
    }
    
    public void setVersion(int version) {
        ver = version;
    }
    
    @Transient
    public PropAccess getManyToOne() {
        return m2one;
    }

    public void setManyToOne(PropAccess pa) {
        m2one = pa;
    }

    @Transient
    public Collection<FieldAccess> getOneToMany() {
        return one2m;
    }

    public void setOneToMany(Collection<FieldAccess> c) {
        one2m = c;
    }

    @Transient
    public PropAccess getOneToOne() {
        return one2one;
    }

    public void setOneToOne(PropAccess pa) {
        one2one = pa;
    }

    @Transient
    public Collection<PropAccess> getManyToMany() {
        return m2m;
    }

    public void setManyToMany(Collection<PropAccess> many) {
        m2m = many;
    }
    
    @Transient
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
    @Access(AccessType.PROPERTY)
    public EmbedId getEmbedId() {
        return eid;
    }
}
