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
    @NamedQuery(name="PropMixedEntity.query", 
        query="SELECT fs FROM PropMixedEntity fs WHERE " + 
        "fs.idval = :id AND fs.name = :name AND " +
        "fs.ema.firstName = :firstName " +
        "AND fs.ema.lastName = :lastName AND " + 
        "fs.ema.mName = :middleName"),
    @NamedQuery(name="PropMixedEntity.badQuery", 
        query="SELECT fs FROM PropMixedEntity fs WHERE " + 
        "fs.idval = :id AND fs.name = :name AND " +
        "fs.ema.firstName = :firstName AND " +
        "fs.ema.lastName = :lastName AND " + 
        "fs.ema.middleName = :middleName") })
public class PropMixedEntity {
    
    @Id
    @GeneratedValue
    @Access(AccessType.FIELD)
    private int idval;
    
    private String myName;

    @Access(AccessType.FIELD)
    @Embedded
    private EmbedMixedAccess ema;
    
    public void setId(int id) {
        this.idval = id;
    }

    @Transient
    public int getId() {
        return idval;
    }

    public void setName(String name) {
        this.myName = name;
    }

    // Property access
    public String getName() {
        return myName;
    }

    @Transient
    public EmbedMixedAccess getEmbedProp() {
        return ema;
    }

    public void setEmbedProp(EmbedMixedAccess ef) {
        ema = ef;
    }

    public boolean equals(Object obj) {
        if (obj instanceof PropMixedEntity) {
            PropMixedEntity ps = (PropMixedEntity)obj;
            return getEmbedProp().equals(ps.getEmbedProp()) 
                && getId() == ps.getId() &&
                getName().equals(ps.getName());
        }
        return false;
    }


}
