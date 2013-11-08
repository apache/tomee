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
package org.apache.openjpa.persistence.jdbc.strategy;

import javax.persistence.*;

import org.apache.openjpa.persistence.jdbc.Strategy;

@Entity
@Table(name="PERSON_STR")
public class Person {
	
	private long idPerson;
	
	private String name;

	private int age;
		
	private Address address;

	public Person() {}

	@Basic
	@Column(nullable = false)
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@Basic
	@Column(nullable = false, length = 30)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id_person")
	public long getIdPerson() {
		return idPerson;
	}

	public void setIdPerson(long idPerson) {
		this.idPerson = idPerson;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@Strategy(value = "org.apache.openjpa.persistence.jdbc.strategy.InnerJoinStrategy" )
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}
}

