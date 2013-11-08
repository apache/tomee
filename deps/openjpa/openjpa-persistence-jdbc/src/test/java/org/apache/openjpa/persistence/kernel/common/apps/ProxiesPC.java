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
package org.apache.openjpa.persistence.kernel.common.apps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.openjpa.persistence.jdbc.KeyColumn;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.openjpa.persistence.PersistentCollection;
import org.apache.openjpa.persistence.PersistentMap;

/**
 * <p>Persistent type used in the {@link TestProxies} tests.</p>
 *
 * @author Abe White
 */
@Entity
@Table(name = "PROX_PC")
public class ProxiesPC implements Comparable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;

    private String name = null;

    @PersistentCollection
    private Set<String> stringSet = new HashSet();

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private TreeSet<ProxiesPC> proxySet = new TreeSet();

    @PersistentMap
    @KeyColumn(name = "strngkey")
    private Map<String, String> stringMap = new HashMap();

    //    @PersistentMap
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    @KeyColumn(name = "prxykey")
    private TreeMap<String, ProxiesPC> proxyMap = new TreeMap();

    @PersistentCollection
    @OrderBy
    private List<String> list = new ArrayList();

    @PersistentCollection
    private Collection<String> comp = new TreeSet(new ComparableComparator());

    @Temporal(TemporalType.DATE)
    private Date date = null;

    // sql types
    private java.sql.Date sqlDate = null;
    private java.sql.Timestamp timestamp = null;

    public ProxiesPC() {
    }

    public ProxiesPC(String name) {
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public Set getStringSet() {
        return stringSet;
    }

    public void setStringSet(Set stringSet) {
        this.stringSet = stringSet;
    }

    public TreeSet getProxySet() {
        return proxySet;
    }

    public void setProxySet(TreeSet proxySet) {
        this.proxySet = proxySet;
    }

    public Map getStringMap() {
        return stringMap;
    }

    public void setStringMap(Map stringMap) {
        this.stringMap = stringMap;
    }

    public TreeMap getProxyMap() {
        return proxyMap;
    }

    public void setProxyMap(TreeMap proxyMap) {
        this.proxyMap = proxyMap;
    }

    public int compareTo(Object other) {
        return name.compareTo(((ProxiesPC) other).getName());
    }

    public List getList() {
        return this.list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public Collection getComp() {
        return this.comp;
    }

    public void setComp(Collection comp) {
        this.comp = comp;
    }

    public java.sql.Date getSQLDate() {
        return this.sqlDate;
    }

    public void setSQLDate(java.sql.Date sqlDate) {
        this.sqlDate = sqlDate;
    }

    public java.sql.Timestamp getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(java.sql.Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
