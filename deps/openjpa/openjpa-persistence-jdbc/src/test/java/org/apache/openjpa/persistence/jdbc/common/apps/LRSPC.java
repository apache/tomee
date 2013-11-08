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
package org.apache.openjpa.persistence.jdbc.common.apps;

import java.util.*;
import javax.persistence.Entity;

/**
 * <p>Persistent type with LRS fields.</p>
 *
 * @author Abe White
 */
@Entity
public class LRSPC
    implements LRSPCIntf {

    private String stringField;
    private Set stringSet = new HashSet();
    private Set relSet = new HashSet();
    private Collection stringCollection = new ArrayList();
    private Collection relCollection = new ArrayList();
    private Map stringMap = new HashMap();
    private Map relMap = new HashMap();

    private LRSPC() {
    }

    public LRSPC(String str) {
        stringField = str;
    }

    public LRSPCIntf newInstance(String stringField) {
        return new LRSPC(stringField);
    }

    public Set getStringSet() {
        return this.stringSet;
    }

    public void setStringSet(Set stringSet) {
        this.stringSet = stringSet;
    }

    public Set getRelSet() {
        return this.relSet;
    }

    public void setRelSet(Set relSet) {
        this.relSet = relSet;
    }

    public Collection getStringCollection() {
        return this.stringCollection;
    }

    public void setStringCollection(Collection stringCollection) {
        this.stringCollection = stringCollection;
    }

    public Collection getRelCollection() {
        return this.relCollection;
    }

    public void setRelCollection(Collection relCollection) {
        this.relCollection = relCollection;
    }

    public Map getStringMap() {
        return this.stringMap;
    }

    public void setStringMap(Map stringMap) {
        this.stringMap = stringMap;
    }

    public Map getRelMap() {
        return this.relMap;
    }

    public void setRelMap(Map relMap) {
        this.relMap = relMap;
    }

    public String getStringField() {
        return this.stringField;
    }

    public int compareTo(Object other) {
        return stringField.compareTo(((LRSPC) other).stringField);
    }
}
