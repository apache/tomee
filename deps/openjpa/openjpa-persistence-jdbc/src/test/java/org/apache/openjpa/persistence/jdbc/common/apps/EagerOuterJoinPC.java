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
 * <p>Class that uses eager outer joins for its collection fields.</p>
 *
 * @author Abe White
 */
@Entity
public class EagerOuterJoinPC {

    private String name = null;
    private Collection stringCollection = new HashSet();
    private List stringList = new ArrayList();
    private Collection oneManyCollection = new HashSet();
    private Collection manyManyCollection = new HashSet();
    private List manyManyList = new ArrayList();
    private HelperPC helper = null;

    public Collection getStringCollection() {
        return this.stringCollection;
    }

    public void setStringCollection(Collection stringCollection) {
        this.stringCollection = stringCollection;
    }

    public List getStringList() {
        return this.stringList;
    }

    public void setStringList(List stringList) {
        this.stringList = stringList;
    }

    public Collection getOneManyCollection() {
        return this.oneManyCollection;
    }

    public void setOneManyCollection(Collection oneManyCollection) {
        this.oneManyCollection = oneManyCollection;
    }

    public Collection getManyManyCollection() {
        return this.manyManyCollection;
    }

    public void setManyManyCollection(Collection manyManyCollection) {
        this.manyManyCollection = manyManyCollection;
    }

    public List getManyManyList() {
        return this.manyManyList;
    }

    public void setManyManyList(List manyManyList) {
        this.manyManyList = manyManyList;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return getClass().getName() + ": " + name;
    }

    public HelperPC getHelper() {
        return this.helper;
    }

    public void setHelper(HelperPC helper) {
        this.helper = helper;
    }
}
