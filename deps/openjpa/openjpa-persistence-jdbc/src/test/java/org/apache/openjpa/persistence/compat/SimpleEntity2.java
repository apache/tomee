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
package org.apache.openjpa.persistence.compat;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQuery(name="FindXTwo", query="select s from simple2 s where s.name = :fname")

@NamedQueries( {
    @NamedQuery(name="FindOne", query="select s from simple2 s where s.name = :fname"),
    @NamedQuery(name="Find2One", query="select s from simple2 s where s.name = :fname"),
    @NamedQuery(name="Find2All", query="select s from simple2 s")
})

@Entity(name = "simple2")
@Table(name = "SIMPLE_ENTITY2")
public class SimpleEntity2 {

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

    public SimpleEntity2() {
    }

    public SimpleEntity2(String name, String value) {
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

