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
import javax.persistence.FetchType;
import javax.persistence.Id;

import org.apache.openjpa.persistence.PersistentCollection;

@Entity
public class PColl_EntityStringEager {

    @Id
    private int id;

    @PersistentCollection(fetch=FetchType.EAGER)
    private Set<String> collectionOfStrings = new HashSet<String>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<String> getCollectionOfStrings() {
        return collectionOfStrings;
    }

    public void setCollectionOfStrings(Set<String> collectionOfStrings) {
        this.collectionOfStrings = collectionOfStrings;
    }
}
