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
package org.apache.openjpa.persistence.models.company.idclass;

import java.util.*;
import javax.persistence.*;
import org.apache.openjpa.persistence.models.company.*;

@Entity(name="IDC_Product")
public class Product implements IProduct {
    private static int ids = 1;

    @Id
    private int id = ++ids;

    @Basic
    private String name;

    @Basic
    private byte[] image;

    @Basic
    private float price;

    @ManyToMany
    private Set<Company> distributors = new HashSet<Company>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }


    public void setImage(byte[] image) {
        this.image = image;
    }

    public byte[] getImage() {
        return this.image;
    }


    public void setPrice(float price) {
        this.price = price;
    }

    public float getPrice() {
        return this.price;
    }


    public void setDistributors(Set<? extends ICompany> distributors) {
        this.distributors = (Set<Company>) distributors;
    }

    public Set<Company> getDistributors() {
        return this.distributors;
    }
}
