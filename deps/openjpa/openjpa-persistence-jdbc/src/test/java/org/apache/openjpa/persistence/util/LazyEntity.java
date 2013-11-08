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

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
public class LazyEntity {

    @Id
    private int id;
    
    @Basic(fetch=FetchType.LAZY)
    private String name;
    
    @Embedded    
    private LazyEmbed lazyEmbed;

    @ElementCollection(fetch=FetchType.LAZY)
    private List<LazyEmbed> lazyEmbedColl;

    @OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    private List<RelEntity> relEntities;
    
    @OneToOne(fetch=FetchType.LAZY)
    private RelEntity relEntity;
    
    @Transient
    private String transField;

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

    public void setLazyEmbed(LazyEmbed lazyEmbed) {
        this.lazyEmbed = lazyEmbed;
    }

    public LazyEmbed getLazyEmbed() {
        return lazyEmbed;
    }

    public void setTransField(String transField) {
        this.transField = transField;
    }

    public String getTransField() {
        return transField;
    }

    public void setLazyEmbedColl(List<LazyEmbed> lazyEmbedColl) {
        this.lazyEmbedColl = lazyEmbedColl;
    }

    public List<LazyEmbed> getLazyEmbedColl() {
        return lazyEmbedColl;
    }

    public void setRelEntities(List<RelEntity> relEntities) {
        this.relEntities = relEntities;
    }

    public List<RelEntity> getRelEntities() {
        return relEntities;
    }

    public void setRelEntity(RelEntity relEntity) {
        this.relEntity = relEntity;
    }

    public RelEntity getRelEntity() {
        return relEntity;
    }
}
