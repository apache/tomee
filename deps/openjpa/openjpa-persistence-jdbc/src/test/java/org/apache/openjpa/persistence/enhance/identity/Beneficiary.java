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
package org.apache.openjpa.persistence.enhance.identity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name="BENEFICIARY")
public class Beneficiary {

	private static final long serialVersionUID = -452903666159175508L;

	private String annuityHolderId;

	private String firstName;

	private String lastName;

	private String relationship;
	
    private List<BeneContact> contacts;

    private String id;  

    @javax.persistence.Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name="FK_ANNUITY_HOLDER_ID")
	public String getAnnuityHolderId() {
		return annuityHolderId;
	}
    
	public void setAnnuityHolderId(String annuityHolderId) {
		this.annuityHolderId = annuityHolderId;
	}

    @Column(name="FIRST_NAME")
	public String getFirstName() {
		return firstName;
	}
    
	public void setFirstName(String first) {
		this.firstName = first;
	}

    @Column(name="LAST_NAME")
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String last) {
		this.lastName = last;
	}

    @Column(name="RELATIONSHIP")
	public String getRelationship() {
		return relationship;
	}
    
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

    @OneToMany(targetEntity=BeneContact.class, mappedBy="beneficiary", fetch=FetchType.EAGER)
	public List<BeneContact> getContacts() {
		return contacts;
	}
    
	public void setContacts(List<BeneContact> contacts) {
		this.contacts = contacts;
	}
}
