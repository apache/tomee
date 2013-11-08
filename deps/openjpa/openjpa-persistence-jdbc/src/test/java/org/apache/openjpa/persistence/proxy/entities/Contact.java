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
package org.apache.openjpa.persistence.proxy.entities;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="CONTACT_ANNUITY")
@AttributeOverride(name="lastUpdateDate", column=@Column(name="LAST_UPDATE_TS"))
public class Contact extends AnnuityPersistebleObject implements IContact {
	private static final long serialVersionUID = 4015672780551057807L;
	private Address theAddress;
	private String email;
	private String phone;
	private ContactType contactType;
	
	@Transient
	public IAddress getAddress() {
		return (IAddress) this.getTheAddress();
	}
	public void setAddress(IAddress address) {
		if (address instanceof Address){
			this.setTheAddress((Address)address);
		}else if(address == null) {
			this.setTheAddress(null);
		}
		else{
			throw new ClassCastException("Invalid Implementaion of IAddress.  " +
					"Class must be instance of com.ibm.wssvt.acme.annuity.common.bean.jpa.Address");
		}
	}

	@Embedded
	private  Address getTheAddress() {
		return theAddress;
	}
	private  void setTheAddress(Address address) {
		this.theAddress = address;
	}
	
	@Column(name="EMAIL")
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Column(name="PHONE")
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	@Column(name="TYPE")
	@Enumerated(EnumType.STRING)
	public ContactType getContactType() {
		return contactType;
	}
	public void setContactType(ContactType contactType) {
		this.contactType = contactType;
	}
	
}
