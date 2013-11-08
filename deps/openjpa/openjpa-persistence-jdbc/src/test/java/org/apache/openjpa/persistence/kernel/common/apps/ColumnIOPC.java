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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "columnpc")
public class ColumnIOPC {

    @Column(length = 40)
    private String name;

    @Column(name = "ignsert")
    private int ignoreInsert;

    @Column(name = "ignpdate")
    private int ignoreUpdate;

    private int ident;

    @Id
    private int id;

    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private ColumnIOPC rel;

    public ColumnIOPC() {

    }

    public ColumnIOPC(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIgnoreInsert() {
        return this.ignoreInsert;
    }

    public void setIgnoreInsert(int ignoreInsert) {
        this.ignoreInsert = ignoreInsert;
    }

    public int getIgnoreUpdate() {
        return this.ignoreUpdate;
    }

    public void setIgnoreUpdate(int ignoreUpdate) {
        this.ignoreUpdate = ignoreUpdate;
    }

    public int getIdent() {
        return this.ident;
    }

    public void setIdent(int ident) {
        this.ident = ident;
    }

    public ColumnIOPC getRel() {
        return this.rel;
    }

    public void setRel(ColumnIOPC rel) {
        this.rel = rel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
