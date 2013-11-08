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
package org.apache.openjpa.persistence.results.cls;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

@NamedNativeQueries({
@NamedNativeQuery(name = "ResultClsQueryDoubleQuotes", 
    query = "select \"1\",\"2\" FROM ResultClsAnnoEntity",
    resultSetMapping = "ResultClsRSMapping", 
    resultClass = ResultClsAnnotation.class),
@NamedNativeQuery(name = "ResultClsQueryBackTicks", 
    query = "select `1`,`2` FROM ResultClsAnnoEntity",
    resultSetMapping = "ResultClsRSMapping", 
    resultClass = ResultClsAnnotation.class),
@NamedNativeQuery(name = "ResultClsQueryBrackets", 
    query = "select [1],[2] FROM ResultClsAnnoEntity",
    resultSetMapping = "ResultClsRSMapping", 
    resultClass = ResultClsAnnotation.class),   
@NamedNativeQuery(name = "ResultClsQueryDefault", 
    query = "select * FROM ResultClsAnnoEntity",
    resultSetMapping = "ResultClsRSMapping", 
    resultClass = ResultClsAnnotation.class)
})

@SqlResultSetMapping(name = "ResultClsRSMapping", 
    entities = @EntityResult(entityClass = ResultClsAnnotation.class, fields = {
        @FieldResult(name = "id", column = "1"),
        @FieldResult(name = "description", column = "2") }))
@Entity(name = "ResultClsAnnoEntity")
public class ResultClsAnnotation {
    public ResultClsAnnotation() {
    }

    @Id
    @Column(name = "1")
    public String id;
    @Basic
    @Column(name = "2")
    public String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
