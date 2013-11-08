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


public class OrderXml {
    protected int id;
    
    protected String description;
    
    protected CustomerXml cust;
    
    public CustomerXml getCust() {
        return cust;
    }
    
    public void setCust(CustomerXml cust) {
        this.cust = cust;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String desc) {
        this.description = desc;
    }
    
    public boolean equals(Object obj) {
        OrderXml o0 = (OrderXml) obj;
        if (o0 == null) return false;
        if (o0.getId() != id) return false;
        if (!o0.getDescription().equals(description)) return false;
        if (!o0.getCust().equals(cust)) return false;
        return true;
    }
}
