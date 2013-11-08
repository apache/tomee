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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.apache.openjpa.persistence.jdbc.KeyColumn;
import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;
import org.apache.openjpa.persistence.PersistentMap;
import org.apache.openjpa.persistence.jdbc.KeyColumn;

@Entity
@DiscriminatorValue("ATTACH_B")
@FetchGroups({
@FetchGroup(name = "all", attributes = {
@FetchAttribute(name = "ds", recursionDepth = 0),
@FetchAttribute(name = "stringIntMap", recursionDepth = 0)
    })
    })
public class AttachB extends AttachA {

    private String bstr;
    private int bint;
    private double bdbl;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Set<AttachD> ds = new HashSet();

    @PersistentMap
    @KeyColumn(name = "strngmap")
    private Map<String, Integer> stringIntMap = new TreeMap<String, Integer>();

    public AttachB() {
    }

    public void setBstr(String bstr) {
        this.bstr = bstr;
    }

    public String getBstr() {
        return this.bstr;
    }

    public void setBint(int bint) {
        this.bint = bint;
    }

    public int getBint() {
        return this.bint;
    }

    public void setBdbl(double bdbl) {
        this.bdbl = bdbl;
    }

    public double getBdbl() {
        return this.bdbl;
    }

    public void setDs(Set ds) {
        this.ds = ds;
    }

    public Set getDs() {
        return this.ds;
    }

    public void setStringIntMap(Map stringIntMap) {
        this.stringIntMap = stringIntMap;
    }

    public Map getStringIntMap() {
        return this.stringIntMap;
    }
}
