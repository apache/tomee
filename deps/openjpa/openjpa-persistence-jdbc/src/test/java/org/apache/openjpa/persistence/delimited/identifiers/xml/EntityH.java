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
package org.apache.openjpa.persistence.delimited.identifiers.xml;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class EntityH {
    private int id;
    private String name;
    
    private String secName;
    
    private Collection<EntityI> eIs = new HashSet<EntityI>();
    
    private EntityI2 eI2;
    
    Map<EntityI3,EntityI4> map = new HashMap<EntityI3,EntityI4>();
    
    Map<EntityI4,EntityI3> map2 = new HashMap<EntityI4,EntityI3>();

    public EntityH() {}
    
    public EntityH(int id) {
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
    
    public Collection<EntityI> getEntityIs() {
        return eIs;
    }
    /**
     * @param entityIs the entityIs to set
     */
    public void setEntityIs(Collection<EntityI> entityIs) {
        this.eIs = entityIs;
    }
    
    public void addEntityI(EntityI entityI) {
        eIs.add(entityI);
    }

    /**
     * @return the entityI2
     */
    public EntityI2 getEntityI2() {
        return eI2;
    }

    /**
     * @param entityI2 the entityI2 to set
     */
    public void setEntityI2(EntityI2 entityI2) {
        this.eI2 = entityI2;
    }

    /**
     * @return the map
     */
    public Map<EntityI3, EntityI4> getMap() {
        return map;
    }

    /**
     * @param map the map to set
     */
    public void setMap(Map<EntityI3, EntityI4> map) {
        this.map = map;
    }
    
    public void addMapValues(EntityI3 key, EntityI4 value) {
        map.put(key, value);
    }

    /**
     * @return the map2
     */
    public Map<EntityI4, EntityI3> getMap2() {
        return map2;
    }

    /**
     * @param map2 the map2 to set
     */
    public void setMap2(Map<EntityI4, EntityI3> map2) {
        this.map2 = map2;
    }
    
    public void addMap2Values(EntityI4 key, EntityI3 value) {
        map2.put(key, value);
    }

}
