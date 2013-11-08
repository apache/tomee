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
package org.apache.openjpa.meta;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Person implements Serializable {

    @Id
    private String name;

    @OneToMany
    private Collection<Painter> paitersForPortrait;

    /**
     * default constructor required by enhancement.
     */
    protected Person() {

    }

    /**
     * The public constructor constructs with a name.
     *
     * @param name the name of the person.
     */

    public Person(String name) {
        this.name = name;
    }

    /**
     * Gets the name of this person. This is the unique identifier.
     *
     * @return return the name of this person.
     */
    public String getName() {

        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().length() == 0)
            throw new IllegalArgumentException(
                "null or empty name not allowed");
        this.name = name;
    }

    public Collection<Painter> getPainters() {

        return paitersForPortrait;
    }

    public void setPainters(Collection<Painter> p) {
        this.paitersForPortrait = p;
    }
}
