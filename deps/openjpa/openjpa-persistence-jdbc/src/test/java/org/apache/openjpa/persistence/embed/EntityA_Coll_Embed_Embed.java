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
package org.apache.openjpa.persistence.embed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.persistence.ElementCollection;

@Entity
@Table(name="TBL1A")
public class EntityA_Coll_Embed_Embed implements Serializable {
    @Id
    Integer id;

    @Column(length=30)
    String name;
    
    @Basic(fetch=FetchType.LAZY)
    int age;

    @ElementCollection
    protected List<Embed_Embed> embeds = new ArrayList<Embed_Embed>();
    
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Embed_Embed> getEmbeds() {
        return embeds;
    }
    
    public void setEmbeds(List<Embed_Embed> embeds) {
        this.embeds = embeds;
    }

    public void addEmbed(Embed_Embed embed) {
        embeds.add(embed);
    }
}

