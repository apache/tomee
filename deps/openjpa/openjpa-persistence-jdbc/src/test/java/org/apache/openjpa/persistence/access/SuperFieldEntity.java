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
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@Inheritance
@Access(AccessType.FIELD)
@NamedQueries( {
    @NamedQuery(name="SuperFieldEntity.query", 
        query="SELECT sfe FROM SuperFieldEntity sfe WHERE " + 
        "sfe.id = :id AND sfe.name = :name"),
    @NamedQuery(name="SuperFieldEntity.badQuery", 
        query="SELECT sfe FROM SuperFieldEntity sfe WHERE " + 
        "sfe.id = :id AND sfe.name = :name AND sfe.crtDate = :crtDate") } )
public class SuperFieldEntity {

    @Id
    @GeneratedValue
    @Access(AccessType.FIELD)
    private int id;
    
    @Basic
    private String name;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
        
    public boolean equals(Object obj) {
        if (obj instanceof SuperFieldEntity) {
            SuperFieldEntity sfe = (SuperFieldEntity)obj;
            return id == sfe.getId() &&
                   name.equals(sfe.getName());
        }
        return false;
    }
}
