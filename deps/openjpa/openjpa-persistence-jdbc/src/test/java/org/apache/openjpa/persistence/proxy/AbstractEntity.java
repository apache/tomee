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
package org.apache.openjpa.persistence.proxy;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@MappedSuperclass
public abstract class AbstractEntity {
    @Id
    @GeneratedValue
    private int id;

    @Version
    int version;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ConcreteEntity> items = new LinkedHashSet<ConcreteEntity>();

    public int getId() {
        return id;
    }

    public Set<ConcreteEntity> getItems() {
        return items;
    }

    public void addItem(ConcreteEntity ce) {
        if (items == null)
            items = new LinkedHashSet<ConcreteEntity>();
        items.add(ce);
    }
}
