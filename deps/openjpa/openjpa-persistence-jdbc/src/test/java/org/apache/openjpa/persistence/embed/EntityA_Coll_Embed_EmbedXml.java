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

/**
 * CREATE TABLE EntityA_Coll_Embed_Embed (id INTEGER NOT NULL, age INTEGER,
 *     name VARCHAR(30), PRIMARY KEY (id))
 * CREATE TABLE EntityA_Coll_Embed_Embed_embeds (
 *     ENTITYA_COLL_EMBED_EMBED_ID INTEGER, intVal1 INTEGER, intVal2 INTEGER,
 *     intVal3 INTEGER, IntVal1x INTEGER, IntVal2x INTEGER, IntVal3x INTEGER)
 * @author faywang
 *
 */


public class EntityA_Coll_Embed_EmbedXml implements Serializable {
    Integer id;

    String name;
    
    int age;

    protected List<Embed_EmbedXml> embeds = new ArrayList<Embed_EmbedXml>();
    
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

    public List<Embed_EmbedXml> getEmbeds() {
        return embeds;
    }
    
    public void addEmbed(Embed_EmbedXml embed) {
        embeds.add(embed);
    }
}

