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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "A_EMBED")
public class A implements java.io.Serializable {

    @Id
    protected String id;
    
    String name;
    
    int value;

    @ElementCollection
    protected Set<Embed> embeds = new HashSet();

    @CollectionTable(name = "collectionTemporalOrderColumnTable", 
            joinColumns = @JoinColumn(name = "parent_id"))
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "value")
    @OrderColumn(name = "valueOrderColumn")
    @Temporal(TemporalType.DATE)
    private Collection<Date> collectionDate;

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    public Set<Embed> getEmbeds() {
        return embeds;
    }

    public void setEmbeds(Set<Embed> embeds) {
        this.embeds = embeds;
    }
    
    public Collection<Date> getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(Collection<Date> collectionDate) {
        this.collectionDate = collectionDate;
    }
    
}    
   
