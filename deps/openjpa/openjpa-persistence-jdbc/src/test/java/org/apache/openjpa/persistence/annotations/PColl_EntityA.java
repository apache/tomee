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
package org.apache.openjpa.persistence.annotations;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Version;

import org.apache.openjpa.persistence.PersistentCollection;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class PColl_EntityA {

    @Id
    private int id;

    @PersistentCollection(elementEmbedded = true)
    private Set<PColl_EmbedB> embedCollection = new HashSet<PColl_EmbedB>();

    @Version
    private int version;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<PColl_EmbedB> getEmbedCollection() {
        return embedCollection;
    }

    public void setEmbedCollection(Set<PColl_EmbedB> embedCollection) {
        this.embedCollection = embedCollection;
    }

    public void addEmbedElement(PColl_EmbedB element) {
        this.embedCollection.add(element);
    }

    public String toString() {
        return "PColl_EntityD<id=" + id + ",ver=" + version
            + ",embedBCollection#=" + embedCollection.size() + ">";
    }
}
