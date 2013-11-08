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

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;

/**
 * @author <A HREF="mailto:pinaki.poddar@gmail.com>Pinaki Poddar</A>
 */
@Entity
@FetchGroups({
@FetchGroup(name = "detail+children-names", fetchGroups = "detail"),
@FetchGroup(name = "detail", fetchGroups = "default",
    attributes = @FetchAttribute(name = "address")),
@FetchGroup(name = "detail+children-list", fetchGroups = "detail",
    attributes = @FetchAttribute(name = "children")),
@FetchGroup(name = "person.address",
    attributes = @FetchAttribute(name = "address"))
    })
public class PCPerson {

    private String name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private PCAddress address;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private PCPerson parent;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<PCPerson> children;

    protected PCPerson() {
        super();
    }

    public PCPerson(String name) {
        setName(name);
    }

    public PCAddress getAddress() {
        return address;
    }

    public void setAddress(PCAddress address) {
        this.address = address;
    }

    public Set getChildren() {
        return children;
    }

    public void setChildren(Set children) {
        this.children = children;
    }

    public void addChildren(PCPerson child) {
        if (children == null)
            children = new HashSet();
        children.add(child);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PCPerson getParent() {
        return parent;
    }

    public void setParent(PCPerson parent) {
        this.parent = parent;
    }

    public static Object reflect(PCPerson instance, String name) {
        if (instance == null)
            return null;
        try {
            return PCPerson.class.getDeclaredField(name).get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
