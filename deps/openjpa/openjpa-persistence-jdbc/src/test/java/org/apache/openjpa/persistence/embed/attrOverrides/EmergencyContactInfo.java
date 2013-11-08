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

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.*;


@Embeddable
public class EmergencyContactInfo {
	String fName;
	String lName;
	@OneToOne
	Address address;
	
	@OneToOne
    PhoneNumber phoneNumber; 
	
	@ElementCollection
	@OrderBy
	Collection<String> nickNames = new ArrayList<String>();
    
    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Address getAddress() {
    	return address;
    }
  
    public void setAddress(Address address) {
    	this.address = address;
    }
    
    public String getFName() {
    	return fName;
    }
    
    public void setFName(String fName) {
    	this.fName = fName;
    }

    public String getLName() {
    	return lName;
    }
    
    public void setLName(String lName) {
    	this.lName = lName;
    }
    
    public Collection<String> getNickNames() {
        return nickNames;
    }
    
    public void setNickNames(Collection<String> nickNames) {
        this.nickNames = nickNames;
    }
    
    public void addNickName(String nickName) {
        nickNames.add(nickName);
    }
}
