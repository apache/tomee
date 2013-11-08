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
package org.apache.openjpa.persistence.jdbc.mapping.bidi;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Demonstrate usage of a JoinTable for a bi-directional one-to-many mapping.
 * 
 * 
 * @author Pinaki Poddar
 *
 */
@Entity
@Table(name="J_PERSON")
public class Person {
	@Id
	private long ssn;
	
	private String name;
	
	@OneToMany(cascade=CascadeType.ALL)
	@JoinTable(name="J_PERSON_ADDRESSES",
            joinColumns = @JoinColumn(name="PERSON_SSN",
                    referencedColumnName="SSN"),
            inverseJoinColumns = @JoinColumn(name="ADDRESS_PHONE",
                    referencedColumnName="PHONE"))
    private Set<Address> addresses = new HashSet<Address>();

	public long getSsn() {
		return ssn;
	}

	public Set<Address> getAddresses() {
		return addresses;
	}

	/**
	 * Keep bi-directional relation consistent.
	 */
	public void addAddress(Address address) {
		if (addresses == null)
			addresses = new HashSet<Address>();
		addresses.add(address);
		address.setPerson(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSsn(long ssn) {
		this.ssn = ssn;
	}
}
