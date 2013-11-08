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

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

@Entity
@Access(AccessType.PROPERTY)
@NamedQueries( {
    @NamedQuery(name="MixedMultEmbedEntity.query", 
        query="SELECT fs FROM MixedMultEmbedEntity fs WHERE " + 
        "fs.mid = :id AND fs.name = :name AND " + 
        "fs.embedProp.firstName = :firstName AND " +
        "fs.embedProp.lastName = :lastName AND " +
        "fs.embedField.fName = :fName AND " +
        "fs.embedField.lName = :lName"),
    @NamedQuery(name="MixedMultEmbedEntity.badQuery1", 
        query="SELECT fs FROM MixedMultEmbedEntity fs WHERE " + 
        "fs.mid = :id AND fs.name = :name AND " + 
        "fs.epa = :epa"),
    @NamedQuery(name="MixedMultEmbedEntity.badQuery2", 
        query="SELECT fs FROM MixedMultEmbedEntity fs WHERE " + 
        "fs.mid = :id AND fs.name = :name AND " + 
        "fs.embedProp = :epa AND " +
        "fs.embedField.firstName = :firstName AND " +
        "fs.embedField.lName = :lastName") })
public class MixedMultEmbedEntity {

    @Id
    @GeneratedValue
    @Access(AccessType.FIELD)
    private int mid;
    
    private String name;

    private EmbedPropAccess epa;

    private EmbedFieldAccess efa;

    public void setId(int id) {
        this.mid = id;
    }

    @Transient
    public int getId() {
        return mid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    @Embedded
    public EmbedPropAccess getEmbedProp() {
        return epa;
    }

    public void setEmbedProp(EmbedPropAccess ep) {
        epa = ep;
    }

    @Embedded
    public EmbedFieldAccess getEmbedField() {
        return efa;
    }

    public void setEmbedField(EmbedFieldAccess ef) {
        efa = ef;
    }

    public boolean equals(Object obj) {
        if (obj instanceof MixedMultEmbedEntity) {
            MixedMultEmbedEntity ps = (MixedMultEmbedEntity)obj;
            return getEmbedProp().equals(ps.getEmbedProp()) &&
                getEmbedField().equals(ps.getEmbedField())
                && getId() == ps.getId() &&
                getName().equals(ps.getName());
        }
        return false;
    }

}
