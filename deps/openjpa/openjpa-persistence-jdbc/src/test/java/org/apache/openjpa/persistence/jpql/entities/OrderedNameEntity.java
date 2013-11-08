/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.persistence.jpql.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;

@Entity
public class OrderedNameEntity implements IColumnEntity, java.io.Serializable {

    @Id
    private int id;
    
    private String name;

    @ManyToMany
    @OrderColumn
    private List <IOrderedEntity> entities;

    
    public OrderedNameEntity() {
    }

    public OrderedNameEntity(String name) {
        this.id = name.charAt(0) - 'A' + 1;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "OrderedNameEntity[" + id + "]=" + name;
    }

    public List<IOrderedEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<IOrderedEntity> entities) {
        this.entities = entities;
    }

    public void addEntity(IOrderedEntity entity) {
        if( entities == null) {
            entities = new ArrayList<IOrderedEntity>();
        }
        entities.add(entity);
    }

    public IOrderedEntity removeEntity(IOrderedEntity entity) {
        IOrderedEntity rtnVal = null;
        if( entities != null) {
            if( entities.remove(entity) )
                rtnVal = entity;
        }
        return rtnVal;
    }
}
