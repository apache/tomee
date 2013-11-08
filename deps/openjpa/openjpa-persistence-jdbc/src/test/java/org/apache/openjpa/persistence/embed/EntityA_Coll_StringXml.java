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
package org.apache.openjpa.persistence.embed;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * CREATE TABLE EntityA_Coll_String (id INTEGER NOT NULL, age INTEGER,
 *     name VARCHAR(30), PRIMARY KEY (id))
 * CREATE TABLE NickNames_Tbl (ENTITYA_COLL_STRING_ID INTEGER,
 *     nicknames1 VARCHAR(20))
 * CREATE INDEX I_NCKNTBL_ENTITYA_ ON NickNames_Tbl (ENTITYA_COLL_STRING_ID)
 * INSERT INTO EntityA_Coll_String (id, age, name) VALUES (?, ?, ?)
 * INSERT INTO NickNames_Tbl (ENTITYA_COLL_STRING_ID, nicknames1) VALUES (?, ?)
 * SELECT t0.age, t0.name FROM EntityA_Coll_String t0 WHERE t0.id = ? 
 *     optimize for 1 row
 * SELECT t0.nicknames1 FROM NickNames_Tbl t0
 *     WHERE t0.ENTITYA_COLL_STRING_ID = ?
 * @author faywang
 */


public class EntityA_Coll_StringXml implements Serializable {
    // contains a collection of basic types
    
    Integer id;

    String name;
    
    int age;

    protected Set<String> nickNames = new HashSet<String>();

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getNickNames() {
        return nickNames;
    }
    
    public void addNickName(String nickName) {
        nickNames.add(nickName);
    }
}

