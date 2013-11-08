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
package org.apache.openjpa.persistence.relations;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="OrderTbl")
public class Order {
    @Id 
    @GeneratedValue
    int oid;

    double amount;
    boolean delivered;

    @ManyToOne (fetch=FetchType.EAGER)
    Customer customer;

    @Version
    long version;

    public Order(){}

    public Order(  double amt, boolean delivered, Customer c){
        amount=amt;
        this.delivered=delivered;
        customer=c;
        if (c!=null) c.getOrders().add(this);
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
            + " customer:" + ( customer != null ? customer.getCid() : -1 );
    }
}
