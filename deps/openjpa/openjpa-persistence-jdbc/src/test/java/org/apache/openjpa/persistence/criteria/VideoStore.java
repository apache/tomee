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
package org.apache.openjpa.persistence.criteria;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Table;


@Entity
@Table(name="CR_VSTR")
public class VideoStore {
    @Id
    @GeneratedValue
    private int id;

    private String name;
    
    @Embedded
    private Address location;
    
    @ElementCollection
    @CollectionTable(name="INVENTORY",
        joinColumns=@JoinColumn(name="STORE"))
    @Column(name="COPIES_IN_STOCK")    
    @MapKeyJoinColumn(name="MOVIE", referencedColumnName="ID")
    private Map<Movie, Integer> videoInventory = new HashMap<Movie, Integer>();
    
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
    
    public Map getVideoInventory() {
        return videoInventory;
    }
    
    public void addToVideoInventory(Movie movie, Integer inventory) {
        videoInventory.put(movie, inventory);
    }
    
    public Address getLocation() {
        return location;
    }
    
    public void setLocation(Address location) {
        this.location = location;
    }
    
}
