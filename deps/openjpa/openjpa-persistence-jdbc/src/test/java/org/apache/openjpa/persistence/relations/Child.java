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
package org.apache.openjpa.persistence.relations;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.ForeignKey;

@Entity
@Table(name = "JPAORPHAN_CHILD")
public class Child implements Serializable {
    private static final long serialVersionUID = 3974762966445439153L;

    @Id
    private int id;

    @ForeignKey
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private Parent parent;

    private String name;

    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Collection<GrandChild> grandChilds;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    public Collection<GrandChild> getGrandChilds() {
        return grandChilds;
    }

    public void setGrandChilds(Collection<GrandChild> grandChilds) {
        this.grandChilds = grandChilds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
