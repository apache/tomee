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
package org.apache.openjpa.persistence.util;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="MAPENT")
public class MapEntity {

    @Id
    @GeneratedValue
    private int id;
    
    @OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    private MapValEntity mapValEntity;

    @OneToMany(fetch=FetchType.EAGER)
    private Map<MapKeyEmbed, MapValEntity> mapEntities;
    
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setMapValEntity(MapValEntity mapValEntity) {
        this.mapValEntity = mapValEntity;
    }

    public MapValEntity getMapValEntity() {
        return mapValEntity;
    }

    public void setMapEntities(Map<MapKeyEmbed, MapValEntity> mapEntities) {
        this.mapEntities = mapEntities;
    }

    public Map<MapKeyEmbed, MapValEntity> getMapEntities() {
        return mapEntities;
    }
}
