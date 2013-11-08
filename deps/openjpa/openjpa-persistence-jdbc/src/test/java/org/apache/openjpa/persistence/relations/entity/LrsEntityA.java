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
package org.apache.openjpa.persistence.relations.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.openjpa.persistence.LRS;

@Entity
@Table(name = "LrsEntityB")
public class LrsEntityA implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Integer id;

    @Column(length = 30)
    String name;
    @Basic(fetch = FetchType.LAZY)
    int age;

    @LRS
    @OneToMany(mappedBy = "entitya", cascade = CascadeType.ALL)
    public Collection<LrsEntityB> entitybs;

    public LrsEntityA() {
        this.name = "none";
        this.entitybs = new ArrayList<LrsEntityB>();
    }

    public LrsEntityA(String nam) {
        this.name = nam;
        entitybs = new ArrayList<LrsEntityB>();
    }

    public LrsEntityA(int id, String nam, int age) {
        this.id = id;
        this.name = nam;
        this.age = age;
        entitybs = new ArrayList<LrsEntityB>();
    }

    public Collection<LrsEntityB> getEntitybs() {
        return entitybs;
    }

    public void setEntitybs(Collection<LrsEntityB> entitybs) {
        this.entitybs = entitybs;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
