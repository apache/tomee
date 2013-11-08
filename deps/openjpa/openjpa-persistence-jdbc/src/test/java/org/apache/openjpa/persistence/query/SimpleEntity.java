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
package org.apache.openjpa.persistence.query;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

@NamedQuery(name="FindXTwo", query="select s from simple s where s.name = ?1")

@NamedQueries( {
    @NamedQuery(name="FindOne",
            query="select s from simple s where s.name = ?1"),
    @NamedQuery(name="SelectWithPositionalParameter",
            query="select a from simple a where a.id=?1 and a.name=?2"),
    @NamedQuery(name="SelectWithNamedParameter",
            query="select a from simple a where a.id=:id and a.name=:name"),
    @NamedQuery(name="FindOne",
            query="select s from simple s where s.name = ?1"),
    @NamedQuery(name="FindAll", query="select s from simple s"),
    @NamedQuery(name="SelectWithPositionalParameterNonOneStart",
        query="select a from simple a where a.id=?900 and a.name=?2 and a.value=?54")
})

@NamedNativeQueries( { 
    @NamedNativeQuery(name = "findSimpleEntitites",
        query = "SELECT ID, NAME, VALUE FROM SIMPLE_ENTITY", 
        resultSetMapping = "simpleEntitiesResult") })

@SqlResultSetMapping(name = "simpleEntitiesResult",
    entities = @EntityResult(
    entityClass = org.apache.openjpa.persistence.query.SimpleEntity.class, 
    fields = {@FieldResult(name = "id", column = "ID"),
        @FieldResult(name = "name", column = "NAME"),
        @FieldResult(name = "value", column = "VALUE") }))
@Entity(name = "simple")
@Table(name = "SIMPLE_ENTITY")
public class SimpleEntity implements Serializable {
    public static final String NAMED_QUERY_WITH_POSITIONAL_PARAMS = "SelectWithPositionalParameter";
    public static final String NAMED_QUERY_WITH_NAMED_PARAMS = "SelectWithNamedParameter";
    
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;

    @Basic
    @Column(name = "NAME")
    private String name;

    @Basic
    @Column(name = "VALUE")
    private String value;

    public SimpleEntity() {
    }

    public SimpleEntity(String name, String value) {
        this();
        this.name = name;
        this.value = value;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
