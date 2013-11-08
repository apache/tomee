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
package org.apache.openjpa.persistence.embed.attrOverrides;

import javax.persistence.*;

@Entity
@Table(name="CUS_ATTROVER")
public class Customer {
    @Id protected Integer id;
    
    protected String name;
    
    @AttributeOverrides({
        @AttributeOverride(name="state", 
        		column=@Column(name="ADDR_STATE")),
        @AttributeOverride(name="zipcode.zip", 
        		column=@Column(name="ADDR_ZIP")),
        @AttributeOverride(name="zipcode.plusFour", 
                column=@Column(name="ADDR_PLUSFOUR"))})
    @Embedded
    protected Address address;
    
    public Integer getId() {
    	return id;
    }
    
    public void setId(Integer id) {
    	this.id = id;
    }
    
    public String getName() {
    	return name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
    public Address getAddress() {
    	return address;
    }
    
    public void setAddress(Address address) {
    	this.address = address;
    }
}

