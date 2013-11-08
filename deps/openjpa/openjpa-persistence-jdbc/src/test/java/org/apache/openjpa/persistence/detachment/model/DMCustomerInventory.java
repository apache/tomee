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
package org.apache.openjpa.persistence.detachment.model;

import static javax.persistence.CascadeType.*;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class DMCustomerInventory  {
    private static long idCounter = System.currentTimeMillis();
    @Id private long id = idCounter++;

    @ManyToOne(cascade={PERSIST, MERGE, REFRESH})
    @JoinColumn(name = "CI_ITEMID")
    private DMItem  item;    
    private int quantity;

    @ManyToOne(cascade=MERGE)
    @JoinColumn(name="CI_CUSTOMERID")
    private DMCustomer customer;
    
    @Version int version;

    public DMCustomerInventory() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DMItem getItem() {
        return item;
    }

    public void setItem(DMItem item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public DMCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(DMCustomer customer) {
        this.customer = customer;
    }    
}
