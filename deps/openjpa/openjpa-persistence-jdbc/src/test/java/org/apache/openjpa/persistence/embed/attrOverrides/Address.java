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
@Table(name="ADR_AO")
public class Address {
    @Id int id;
    @Column(length = 20)
    protected String street;
    @Column(length = 20)
    protected String city;
    @Column(length = 20)
    protected String state;
    @Embedded protected Zipcode zipcode;
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public Zipcode getZipcode() {
    	return zipcode;
    }
    
    public void setZipcode(Zipcode zipcode) {
    	this.zipcode = zipcode;
    }
    
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Address)) return false;
		Address a = (Address) o;
		if (!city.equals(a.city)) return false;
		if (!state.equals(a.state)) return false;
		if (!street.equals(a.street)) return false;
		if (zipcode != null && !zipcode.equals(a.zipcode)) return false;
		return true;
	}
	
	public int hashCode() {
		int ret = 0;
		ret = ret + 31 * city.hashCode();
		ret = ret + 31 * state.hashCode();
		ret = ret + 31 * street.hashCode();
		if (zipcode != null)
		    ret = ret + 31 * zipcode.hashCode();
		return ret;
	}
}
