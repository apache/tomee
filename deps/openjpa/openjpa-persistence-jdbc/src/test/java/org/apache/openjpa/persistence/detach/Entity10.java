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
package org.apache.openjpa.persistence.detach;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Entity10 {
    @Id
    private long id;
    private String name;
    @OneToMany(cascade=(CascadeType.ALL))
    private Collection<Entity8> collection;
    
    private Collection<Integer> intCollection;
    
    @ElementCollection
    private Collection<String> stringCollection;
    
    public Entity10() {
        initialize();
    }

    public Entity10(long id, String name) {
        this.id = id;
        this.name = name;
        initialize();
    }
    
    private void initialize() {
        intCollection = new ArrayList<Integer>();
        intCollection.add(new Integer(1));
        intCollection.add(new Integer(99));
        
        stringCollection = new HashSet<String>();
        stringCollection.add(new String("xxx"));
        stringCollection.add(new String("yyy"));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Entity8> getCollection() {
        return collection;
    }

    public void setCollection(Collection<Entity8> collection) {
        this.collection = collection;
    }
}
