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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntityA {
    private int id;
    private String name;
    
    private String secName;
    
    private Set<String> delimSet = new HashSet<String>();
    private Map<String, String> delimMap = 
        new HashMap<String, String>();
    
    public EntityA(){
    }
    
    public EntityA(int id, String name) {
        this.name=name;
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

    public Set<String> getDelimSet() {
        return delimSet;
    }

    public void setDelimSet(Set<String> collectionDelimSet) {
        this.delimSet = collectionDelimSet;
    }
    
    public void addDelimSet(String item) {
        this.delimSet.add(item);
    }

    /**
     * @return the delimCollectionMap
     */
    public Map<String, String> getDelimMap() {
        return delimMap;
    }

    /**
     * @param delimCollectionMap the delimCollectionMap to set
     */
    public void setDelimMap(Map<String, String> delimMap) {
        this.delimMap = delimMap;
    }
    
    public void addDelimMap(String key, String value) {
        this.delimMap.put(key, value);
    }
}
