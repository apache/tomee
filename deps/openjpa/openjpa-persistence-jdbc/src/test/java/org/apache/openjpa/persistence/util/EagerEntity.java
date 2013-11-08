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
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class EagerEntity {

    @Id
    private int id;
    
    @Basic
    private String name;
    
    @Embedded    
    private EagerEmbed eagerEmbed;

    @Embedded    
    private EagerEmbedRel eagerEmbedRel;

    @ElementCollection(fetch=FetchType.EAGER)
    private List<EagerEmbed> eagerEmbedColl;

    @OneToMany(fetch=FetchType.EAGER)
    private List<EagerEntity> eagerSelf;
    
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

    public void setEagerEmbed(EagerEmbed eagerEmbed) {
        this.eagerEmbed = eagerEmbed;
    }

    public EagerEmbed getEagerEmbed() {
        return eagerEmbed;
    }

    public void setTransField(String transField) {
        this.transField = transField;
    }

    public String getTransField() {
        return transField;
    }

    public void setEagerEmbedColl(List<EagerEmbed> eagerEmbedColl) {
        this.eagerEmbedColl = eagerEmbedColl;
    }

    public List<EagerEmbed> getEagerEmbedColl() {
        return eagerEmbedColl;
    }

    public void setEagerEmbedRel(EagerEmbedRel eagerEmbedRel) {
        this.eagerEmbedRel = eagerEmbedRel;
    }

    public EagerEmbedRel getEagerEmbedRel() {
        return eagerEmbedRel;
    }
}
