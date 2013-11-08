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
package org.apache.openjpa.persistence.models.company.fetchlazy;

import java.util.*;
import javax.persistence.*;
import org.apache.openjpa.persistence.models.company.*;

@Entity(name="LAZ_ProductOrder")
public class ProductOrder implements IProductOrder {
    private static long idCounter = System.currentTimeMillis();

    @Id
    private long id = idCounter++;

    @OneToMany(fetch=FetchType.LAZY)
    private List<LineItem> items = new LinkedList<LineItem>();

    @Basic(fetch=FetchType.LAZY)
    private Date orderDate;

    @Basic(fetch=FetchType.LAZY)
    private Date shippedDate;

    @OneToOne(fetch=FetchType.LAZY)
    private Customer customer;

    public void setItems(List<? extends ILineItem> items) {
        this.items = (List<LineItem>) items;
    }

    public List<LineItem> getItems() {
        return this.items;
    }


    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getOrderDate() {
        return this.orderDate;
    }


    public void setShippedDate(Date shippedDate) {
        this.shippedDate = shippedDate;
    }

    public Date getShippedDate() {
        return this.shippedDate;
    }


    public void setCustomer(ICustomer customer) {
        this.customer = (Customer) customer;
    }

    public ICustomer getCustomer() {
        return this.customer;
    }


    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

}
