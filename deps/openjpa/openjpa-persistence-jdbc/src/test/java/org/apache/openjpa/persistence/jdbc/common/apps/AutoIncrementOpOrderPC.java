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


import javax.persistence.Entity;
import javax.persistence.Id;


import java.util.*;


@Entity
public class AutoIncrementOpOrderPC {

	@Id
    private long id;
    private AutoIncrementOpOrderPC rel;
    private AutoIncrementOpOrderPC owner;
    private String secondary;
    private List relList = new ArrayList();
    private List mappedRelList = new ArrayList();
    private List inverseKeyRelList = new ArrayList();

    public long getId() {
        return id;
    }

    public OpOrder getRel() {
        return (OpOrder) rel;
    }

    public void setRel(AutoIncrementOpOrderPC rel) {
        this.rel = rel;
    }

    public OpOrder getOwner() {
        return (OpOrder) owner;
    }

    public void setOwner(AutoIncrementOpOrderPC owner) {
        this.owner = owner;
    }

    public String getSecondary() {
        return secondary;
    }

    public void setSecondary(String secondary) {
        this.secondary = secondary;
    }

    public List getRelList() {
        return relList;
    }

    public List getMappedRelList() {
        return mappedRelList;
    }

    public List getInverseKeyRelList() {
        return inverseKeyRelList;
    }
}
