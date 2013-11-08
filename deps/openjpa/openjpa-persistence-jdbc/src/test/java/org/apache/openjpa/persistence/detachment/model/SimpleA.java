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

package org.apache.openjpa.persistence.detachment.model;

import javax.persistence.*;
import java.util.Set;
import java.util.LinkedHashSet;

@Entity
public class SimpleA {

    @Id
    @GeneratedValue
    protected int a_id;

    @Basic
    protected String name;

    @OneToOne
    protected SimpleRef ref;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="parent")
    protected Set<SimpleB> b_set = new LinkedHashSet<SimpleB>();

    public int getId() {
        return a_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SimpleRef getRef() {
        return ref;
    }

    public void setRef(SimpleRef ref) {
        this.ref = ref;
    }

    public void addB(SimpleB b) {
        b_set.add(b);
        b.setParent(this);
    }

    public Set<SimpleB> getBs() {
       return b_set;
    }
}
