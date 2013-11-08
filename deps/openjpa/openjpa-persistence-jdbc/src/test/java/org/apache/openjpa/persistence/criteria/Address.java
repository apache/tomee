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
package org.apache.openjpa.persistence.criteria;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;


/**
 * Used for testing Criteria API.
 * The fields are sometimes not declared as there is no validation yet during 
 * Query construction.
 * 
 */
@Entity
@Table(name="CR_ADDR")
@Embeddable
public class Address {
	@Id
	@GeneratedValue
	private long id;
	
	private String street;
	private String city;
	private String state;
	private String county;
    private String country;
	private String zipCode;
    @OneToOne(mappedBy="address")
    private CompUser user;
	
    public Address(){}

    public Address(String street, String city, String country, String zipcode)
    {
        this.street = street;
        this.city = city;
        this.country = country;
        this.zipCode = zipcode;
    }

    public long getId() {
        return id;
    }
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getCounty() {
        return county;
    }
    
    public void setCounty(String county) {
        this.county = county;
    }

    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getZipcode() {
        return zipCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }

    public void setZipcode(String zipCode) {
        this.zipCode = zipCode;
    }
}
