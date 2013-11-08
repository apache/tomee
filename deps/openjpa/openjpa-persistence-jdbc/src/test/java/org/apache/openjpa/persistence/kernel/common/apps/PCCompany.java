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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@FetchGroup(name = "company.address",
    attributes = @FetchAttribute(name = "address")),
@FetchGroup(name = "company.departments",
    attributes = @FetchAttribute(name = "departments")),
@FetchGroup(name = "default", postLoad = false,
    attributes = @FetchAttribute(name = "name"))
    })
public class PCCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private PCAddress address;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<PCDepartment> departments;

    public PCCompany() {
        super();
    }

    public PCCompany(String name) {
        super();
        setName(name);
    }

    public int getId() {
        return this.id;
    }

    public PCAddress getAddress() {
        return address;
    }

    public void setAddress(PCAddress address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set getDepartments() {
        return departments;
    }

    public void setDepartments(Set departments) {
        this.departments = departments;
    }

    public void addDepartment(PCDepartment dept) {
        if (departments == null)
            departments = new HashSet();
        departments.add(dept);
        dept.setCompany(this);
    }

    public boolean contains(PCDepartment dept) {
        return departments != null && departments.contains(dept);
    }

    public static Object reflect(PCCompany instance, String name) {
        if (instance == null)
            return null;
        try {
            return PCCompany.class.getDeclaredField(name).get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
