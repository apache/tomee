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
package org.apache.openjpa.persistence.util;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class OneToEntity {

    @Id
    @GeneratedValue
    private int id;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private Collection<ToManyLazy> toManyLazy;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private Collection<ToManyEager> toManyEager;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setToManyLazy(Collection<ToManyLazy> toManyEnt) {
        this.toManyLazy = toManyEnt;
    }

    public Collection<ToManyLazy> getToManyLazy() {
        return toManyLazy;
    }

    public void setToManyEager(Collection<ToManyEager> toManyEnt) {
        this.toManyEager = toManyEnt;
    }

    public Collection<ToManyEager> getToManyEager() {
        return toManyEager;
    }

}
