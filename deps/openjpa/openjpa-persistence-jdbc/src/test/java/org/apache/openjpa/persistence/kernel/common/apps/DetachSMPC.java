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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.persistence.Entity;

import org.apache.openjpa.persistence.jdbc.KeyColumn;
import org.apache.openjpa.persistence.PersistentCollection;

@Entity
public class DetachSMPC
    implements Serializable {

    private int intField;

    @PersistentCollection
    private Set relSet = new HashSet();
    @KeyColumn(name = "strngkey")
    private Map stringIntMap = new TreeMap();

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public int getIntField() {
        return this.intField;
    }

    public Set getRelSet() {
        return this.relSet;
    }

    public void setStringIntMap(Map stringIntMap) {
        this.stringIntMap = stringIntMap;
    }

    public Map getStringIntMap() {
        return this.stringIntMap;
    }
}
