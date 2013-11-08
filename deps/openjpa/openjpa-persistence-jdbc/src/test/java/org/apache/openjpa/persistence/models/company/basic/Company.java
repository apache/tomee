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
package org.apache.openjpa.persistence.models.company.basic;

import java.util.*;
import javax.persistence.*;
import org.apache.openjpa.persistence.models.company.*;

@Entity(name="BAS_Company")
public class Company implements ICompany {
    private static long idCounter = System.currentTimeMillis();

    @Id
    private long id = idCounter++;

    @Basic
    private String name;

    @OneToOne
    private Address address;

    @OneToMany(mappedBy="company")
    private Set<Employee> employees = new HashSet<Employee>();

    @ManyToMany(mappedBy="distributors")
    private Set<Product> products = new HashSet<Product>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }


    public void setAddress(IAddress address) {
        this.address = (Address) address;
    }

    public IAddress getAddress() {
        return this.address;
    }


    public void setEmployees(Set<? extends IEmployee> employees) {
        this.employees = (Set<Employee>) employees;
    }

    public Set<Employee> getEmployees() {
        return this.employees;
    }


    public void setProducts(Set<? extends IProduct> products) {
        this.products = (Set<Product>) products;
    }

    public Set<Product> getProducts() {
        return this.products;
    }


    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

}
