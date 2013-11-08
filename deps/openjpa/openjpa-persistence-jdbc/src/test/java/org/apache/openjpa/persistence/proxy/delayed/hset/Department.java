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
package org.apache.openjpa.persistence.proxy.delayed.hset;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.apache.openjpa.persistence.proxy.delayed.Award;
import org.apache.openjpa.persistence.proxy.delayed.Certification;
import org.apache.openjpa.persistence.proxy.delayed.IDepartment;
import org.apache.openjpa.persistence.proxy.delayed.IEmployee;
import org.apache.openjpa.persistence.proxy.delayed.Location;
import org.apache.openjpa.persistence.proxy.delayed.Product;

@Entity
@Table(name="DC_DEPARTMENT")
public class Department implements IDepartment, Serializable { 

    private static final long serialVersionUID = -6923551949033215888L;

    @Id
    @GeneratedValue
    private int id;
    
    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, targetEntity=Employee.class)
    @JoinTable(name="DC_DEP_EMP")
    private Set<IEmployee> employees;
    
    @OrderColumn
    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY)
    @JoinTable(name="DC_DEP_LOC")
    private Set<Location> locations;

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.EAGER)
    @JoinTable(name="DC_DEP_PRD")
    private Set<Product> products;
    
    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(name="DC_DEP_CERT")
    private Set<Certification> certifications;

    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="DC_DEP_AWD")
    private Set<Award> awards;
    
    @Override
    public void setEmployees(Collection<IEmployee> employees) {
        this.employees = (Set<IEmployee>)employees;
    }

    @Override
    public Collection<IEmployee> getEmployees() {
        return employees;
    }
    
    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setLocations(Collection<Location> locations) {
        this.locations =(Set<Location>)locations;
    }

    @Override
    public Collection<Location> getLocations() {
        return locations;
    }

    @Override
    public void setProducts(Collection<Product> products) {
        this.products = (Set<Product>)products;
    }

    @Override
    public Collection<Product> getProducts() {
        return products;
    }

    @Override
    public void setCertifications(Collection<Certification> certifications) {
        this.certifications = (Set<Certification>)certifications;
    }

    @Override
    public Collection<Certification> getCertifications() {
        return certifications;
    }

    @Override
    public void setAwards(Collection<Award> awards) {
        this.awards = (Set<Award>)awards;
    }

    @Override
    public Collection<Award> getAwards() {
        return awards;
    }
}
