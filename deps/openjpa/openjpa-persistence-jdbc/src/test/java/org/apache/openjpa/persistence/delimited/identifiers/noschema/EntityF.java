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
package org.apache.openjpa.persistence.delimited.identifiers.noschema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="\"nsprimary entityF\"",
    uniqueConstraints=
        @UniqueConstraint(columnNames={"\"nsf name\"", "nsf_nonDelimName"}))
@SecondaryTable(name="\"nssecondary entityF\"",
    uniqueConstraints=
        @UniqueConstraint(name="\"nssec unq\"", 
            columnNames={"\"nssecondary name\""}))
public class EntityF {
    @TableGenerator(name = "f_id_gen", table = "\"nsf id gen\"", 
        pkColumnName = "\"nsgen pk\"", valueColumnName = "\"nsgen value\"")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "f_id_gen")
    @Id
    private int id;
    // Note: Delimited columnDefinition is not supported on some DBs
    // TODO: copy into a separate entity and conditionally run a different test
    @Column(name="\"nsf name\"", columnDefinition="varchar(15)")
    private String name;
    @Column(name="nsf_nonDelimName")
    private String nonDelimName;
    @Column(name="\"nssecondary name\"", table="\"nssecondary entityF\"")
    private String secName;
    
    @ElementCollection
    // CollectionTable with default name generation
    @CollectionTable
    private Set<String> nscs = new HashSet<String>();
    
    @ElementCollection
    @CollectionTable(name="\"nsc DelSet\"")
    private Set<String> nscds = new HashSet<String>();
    
    @ElementCollection
    // MapKeyColumn with default name generation
    @MapKeyColumn
    private Map<String, String> nscollMap = new HashMap<String, String>();
    
    @ElementCollection
    // Note: Delimited column definition is not supported on some DBs, so
    // it is not delimited here
    // TODO: create a separate entity and conditionally run the test on a supported DB
    @MapKeyColumn(name="\"nsmap Key\"", columnDefinition="varchar(20)", table="\"nsd c map\"")
    private Map<String, String> delimCollectionMap = 
        new HashMap<String, String>();
    
    public EntityF(String name) {
        this.name = name;
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
     * @return the nonDelimName
     */
    public String getNonDelimName() {
        return nonDelimName;
    }

    /**
     * @param nonDelimName the nonDelimName to set
     */
    public void setNonDelimName(String nonDelimName) {
        this.nonDelimName = nonDelimName;
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

    /**
     * @return the collectionSet
     */
    public Set<String> getCollectionSet() {
        return nscs;
    }

    /**
     * @param collectionSet the collectionSet to set
     */
    public void setCollectionSet(Set<String> collectionSet) {
        this.nscs = collectionSet;
    }
    
    public void addCollectionSet(String item) {
        nscs.add(item);
    }

    /**
     * @return the collectionNamedSet
     */
    public Set<String> getCollectionDelimSet() {
        return nscds;
    }

    /**
     * @param collectionNamedSet the collectionNamedSet to set
     */
    public void setCollectionDelimSet(Set<String> collectionDelimSet) {
        this.nscds = collectionDelimSet;
    } 
    
    public void addCollectionDelimSet(String item) {
        this.nscds.add(item);
    }

    /**
     * @return the collectionMap
     */
    public Map<String, String> getCollectionMap() {
        return nscollMap;
    }

    /**
     * @param collectionMap the collectionMap to set
     */
    public void setCollectionMap(Map<String, String> collectionMap) {
        this.nscollMap = collectionMap;
    }

    public void addCollectionMap(String key, String value) {
        nscollMap.put(key, value);
    }

    /**
     * @return the delimCollectionMap
     */
    public Map<String, String> getDelimCollectionMap() {
        return delimCollectionMap;
    }

    /**
     * @param delimCollectionMap the delimCollectionMap to set
     */
    public void setDelimCollectionMap(Map<String, String> delimCollectionMap) {
        this.delimCollectionMap = delimCollectionMap;
    }
    
    public void addDelimCollectionMap(String key, String value) {
        delimCollectionMap.put(key, value);
    }
}
