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
package org.apache.openjpa.persistence.delimited.identifiers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Entity
@Table(name="\"entity c\"", schema="\"delim id\"")
@SecondaryTable(name="\"sec join table\"", schema="\"delim id\"",
    pkJoinColumns=@PrimaryKeyJoinColumn(name="\"entity c\"",
        referencedColumnName="\"c id\""))
public class EntityC {
    @Id
    @Column(name="\"c id\"")
    private int id;
    private String name;
    
    @Column(table="\"sec join table\"")
    private String secName;
    
    @ManyToMany
    @JoinTable(name="\"c d\"", schema="\"delim id\"")
    private Collection<EntityD> entityDs = new HashSet<EntityD>();
    
    @OneToOne
    @JoinColumn(name="\"entd2 id\"", referencedColumnName="\"entityD2 id\"")
    private EntityD2 entityD2;
    
    @ManyToMany
    @JoinTable(name="\"m jtbl\"", schema="\"delim id\"")
    @MapKeyJoinColumn(name="map_ed3", referencedColumnName="\"entityD3 id\"")
    Map<EntityD3,EntityD4> map = new HashMap<EntityD3,EntityD4>();
    
    @ManyToMany
    @JoinTable(name="\"m2 jtbl\"", schema="\"delim id\"")
    @MapKeyJoinColumn(name="\"map ed4\"", 
        referencedColumnName="\"entityD4 id\"")
    Map<EntityD4,EntityD3> map2 = new HashMap<EntityD4,EntityD3>();
    
    public EntityC() {}
    
    public EntityC(int id) {
        this.id = id;
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the entityDs
     */
    public Collection<EntityD> getEntityDs() {
        return entityDs;
    }
    /**
     * @param entityDs the entityDs to set
     */
    public void setEntityDs(Collection<EntityD> entityDs) {
        this.entityDs = entityDs;
    }
    
    public void addEntityD(EntityD entityD) {
        entityDs.add(entityD);
    }

    /**
     * @return the entityD2
     */
    public EntityD2 getEntityD2() {
        return entityD2;
    }

    /**
     * @param entityD2 the entityD2 to set
     */
    public void setEntityD2(EntityD2 entityD2) {
        this.entityD2 = entityD2;
    }

    /**
     * @return the map
     */
    public Map<EntityD3, EntityD4> getMap() {
        return map;
    }

    /**
     * @param map the map to set
     */
    public void setMap(Map<EntityD3, EntityD4> map) {
        this.map = map;
    }
    
    public void addMapValues(EntityD3 key, EntityD4 value) {
        map.put(key, value);
    }

    /**
     * @return the map2
     */
    public Map<EntityD4, EntityD3> getMap2() {
        return map2;
    }

    /**
     * @param map2 the map2 to set
     */
    public void setMap2(Map<EntityD4, EntityD3> map2) {
        this.map2 = map2;
    }
    
    public void addMap2Values(EntityD4 key, EntityD3 value) {
        map2.put(key, value);
    }

    /**
     * @return the secName
     */
    public String getSecName() {
        return secName;
    }

    /**
     * @param secName the secName to set
     */
    public void setSecName(String secName) {
        this.secName = secName;
    }
}
