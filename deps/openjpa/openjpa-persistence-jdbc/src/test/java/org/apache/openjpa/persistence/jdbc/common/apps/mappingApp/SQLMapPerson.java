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
package org.apache.openjpa.persistence.jdbc.common.apps.mappingApp;

import java.io.Serializable;

import javax.persistence.*;

/** 
 * @author ppoddar
 *
 */

@SqlResultSetMapping (name="MappingWithSelfJoin",
		columns={
			@ColumnResult(name="MY_NAME"),
			@ColumnResult(name="PARTNER_NAME")
		}
)
@Entity
@Table(name = "SQLMAP_PERSON")
public class SQLMapPerson implements Serializable {
	private String name;
	private SQLMapAddress address;
	private SQLMapPerson partner;


	protected SQLMapPerson() {
	}

	public SQLMapPerson(String name) {
		this.name = name;
	}

	public SQLMapPerson(String name, SQLMapAddress address) {
		this.name = name;
		setAddress(address);
	}

	@Id
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="ADDRESS_ID")
	public SQLMapAddress getAddress() {
		return address;
	}

	public void setAddress(SQLMapAddress address) {
		this.address = address;
	}


	@OneToOne
	public SQLMapPerson getPartner() {
		return partner;
	}

	public void setPartner(SQLMapPerson partner) {
		this.partner = partner;
	}
//	@PostLoad
//	protected void inform() {
//		System.out.println("Loaded" + this);
//	}
}
