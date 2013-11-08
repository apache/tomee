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
public class SimpleB {

    @Id
    @GeneratedValue
    protected int b_id;

    @Basic
    protected String name;

    @ManyToOne
    @JoinColumn(name="A_ID", referencedColumnName="A_ID", nullable = false,
            updatable = false)
    protected SimpleA parent;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="parent")
    protected Set<SimpleC> c_set  = new LinkedHashSet<SimpleC>();

    public int getId() {
        return b_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SimpleA getParent() {
       return parent;
    }

    public void setParent(SimpleA a) {
       this.parent = a;
    }

    public void addC (SimpleC c) {
        c_set.add (c);
        c.setParent(this);
    }

    public Set<SimpleC> getCs() {
       return c_set;
    }
}
