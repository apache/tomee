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
package org.apache.openjpa.persistence.xmlmapping.entities;

import org.apache.openjpa.persistence.xmlmapping.xmlbindings.myaddress.Address;

import javax.persistence.*;

import org.apache.openjpa.persistence.Persistent;
import org.apache.openjpa.persistence.jdbc.Strategy;

@Entity
@Table(name="TORDERXML")
public class Order {
    @Id 
    int oid;

    double amount;
    boolean delivered;

    @ManyToOne(fetch=FetchType.LAZY)
    Customer customer;

    @Persistent
    @Strategy("org.apache.openjpa.jdbc.meta.strats.XMLValueHandler")
    Address shipAddress;

    @Version
    long version;

    public Order(){}

    public Order(int id,  double amt, boolean delivered, Customer c) {
        oid = id;
        amount = amt;
        this.delivered = delivered;
        customer = c;
        if (c != null)
            c.getOrders().add(this);
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public int getOid() {
        return oid;
    }

    public String toString(){
        return "Order:" + oid + " amount:" + amount + " delivered:" + delivered
        + " customer:" + ((customer != null) ? customer.getCid() : -1);
    }

    public Address getShipAddress() {
        return shipAddress;
    }

    public void setShipAddress(Address shipAddress) {
        this.shipAddress = shipAddress;
    }
}
