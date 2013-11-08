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
package org.apache.openjpa.persistence.embed;

import java.util.HashSet;
import java.util.Set;

public class CustomerXml {
    protected int id;
    
    protected String name;
    
    protected Set<OrderXml> orders = new HashSet<OrderXml>();
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Set<OrderXml> getOrders() {
        return orders;
    }

    public void setOrders(Set<OrderXml> orders) {
        this.orders = orders;
    }
    
    public void addOrder(OrderXml order) {
        orders.add(order);
    }
    
    public boolean equals(Object obj) {
        CustomerXml c0 = (CustomerXml) obj;
        if (c0 == null) return false;
        if (c0.getId() != id) return false;
        if (!c0.getName().equals(name)) return false;
        Set<OrderXml> orders0 = c0.getOrders();
        if (orders0.size() != orders.size())
            return false;
        for (OrderXml o : orders) {
            if (!contains(orders0, o))
                return false;
        }
        return true;
    }
    
    private boolean contains(Set<OrderXml> orders0, OrderXml o) {
        int id = o.getId();
        for (OrderXml o0 : orders0) {
            if (o0.getId() == id)
                return true;
        }
        return false;
    }
}


