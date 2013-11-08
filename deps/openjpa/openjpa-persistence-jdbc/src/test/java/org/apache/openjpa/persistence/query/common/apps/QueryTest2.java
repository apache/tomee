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
package org.apache.openjpa.persistence.query.common.apps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.openjpa.persistence.PersistentCollection;
import org.apache.openjpa.persistence.PersistentMap;
import org.apache.openjpa.persistence.jdbc.KeyColumn;

/**
 * <p>Used in testing; should be enhanced.</p>
 *
 * @author Abe White
 */
@Entity
@DiscriminatorValue("query2")
public class QueryTest2 extends QueryTest1 {

    @OneToOne(cascade = { CascadeType.ALL })
    private QueryTest2 oneToOne = null;

    @PersistentCollection
    private List<String> stringCollection = null;

    @OneToMany(cascade = { CascadeType.ALL })
    private List<QueryTest2> oneToMany = null;

    @PersistentMap
    @KeyColumn(name = "SMAP")
    private Map<String, String> stringMap = null;

    @OneToMany(cascade = { CascadeType.ALL })
    @KeyColumn(name = "QT2")
    private Map<String, QueryTest2> stringToManyMap = null;

    public QueryTest2() {
    }

    public QueryTest2(int id) {
        super(id);
    }

    public QueryTest2 getOneToOne() {
        return oneToOne;
    }

    public void setOneToOne(QueryTest2 val) {
        oneToOne = val;
    }

    public Collection getStringCollection() {
        return stringCollection;
    }

    public void setStringCollection(List<String> val) {
        stringCollection = val;
    }

    public Collection getOneToMany() {
        return oneToMany;
    }

    public void setOneToMany(List<QueryTest2> val) {
        oneToMany = val;
    }

    public Map getStringMap() {
        return stringMap;
    }

    public void setStringMap(Map val) {
        stringMap = val;
    }

    public Map getStringToManyMap() {
        return stringToManyMap;
    }

    public void setStringToManyMap(Map val) {
        stringToManyMap = val;
    }
}
