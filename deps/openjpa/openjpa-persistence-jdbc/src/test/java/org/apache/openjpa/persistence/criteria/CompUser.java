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

import javax.persistence.*;
import org.apache.openjpa.persistence.*;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class CompUser
{
	@Basic
	@Column(length=50)
	private String name;

	@Basic
	private int age;

	@Basic
	@Column(name="compName", length=50)
	private String computerName;

	@PersistentCollection(fetch=FetchType.EAGER)
	private String[] nicknames = new String[0];

	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinColumn(name="ADD_ID")
	private Address address;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int userid;
	
    @Enumerated
    @Basic
    private CreditRating creditRating;
	
	public CompUser(){}

	public CompUser(String name, String cName, Address address, int age)
	{
		this.name = name;
		this.computerName = cName;
		this.address = address;
		this.age = age;
	}

	public String getComputerName() {
		return computerName;
	}

	public void setComputerName(String computerName) {
		this.computerName = computerName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getUserid() {
		return userid;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

    public CreditRating getRating() {
        return creditRating;
    }
    
    public void setRating(CreditRating rating) {
        this.creditRating = rating;
    }
	
    public enum CreditRating { POOR, GOOD, EXCELLENT };
}
