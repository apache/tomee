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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class BeneContactId implements Serializable {
	private static final long serialVersionUID = -837443719842439462L;
	ContactType type; 

    String beneficiaryPK; 
	
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if((obj != null) && (obj instanceof BeneContactId)) {
			BeneContactId other = (BeneContactId) obj;
			if(this.type.equals(other.type) && this.beneficiaryPK.equals(other.beneficiaryPK))
				return true;
		}
		return false;
	}
	
	public int hashCode() {
		String hash = beneficiaryPK + Integer.toString(type.ordinal());
		return hash.hashCode();
	}
	
	public String toString() {
		return type.toString() + "-" + beneficiaryPK;
	}
	
    @Column(name="ID")
	public String getBeneficiaryKey() {
		return beneficiaryPK;
	}
    
	public void setBeneficiaryKey(String id) {
		beneficiaryPK = id;
	}
	
    @Enumerated(EnumType.STRING)
    @Column(name="TYPE")
	public ContactType getContactType() {
		return type;
	}
    
	public void setContactType(ContactType type) {
		this.type = type;
	}
	
	public enum ContactType {HOME, BUSINESS, OTHER;}
}
