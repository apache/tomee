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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;


@Entity
@Table(name="DI_BENE_CONTACT")
public class BeneContact {

	private static final long serialVersionUID = 4571838649566012594L;

	private BeneContactId id;

	private Beneficiary beneficiary;
	
	private String email;

	private String phone;

    private Date lastUpdateDate;

    private int version;
    
    @Version
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
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
	
    @EmbeddedId
	public BeneContactId getId() {
		return id;
	}
    
	public void setId(BeneContactId id) {
		this.id = id;
	}

    @ManyToOne(targetEntity=Beneficiary.class, fetch=FetchType.EAGER)
    @JoinColumn(name="ID")
    @MapsId("beneficiaryPK")
	public Beneficiary getBeneficiary() {
		return beneficiary;
	}
    
	public void setBeneficiary(Beneficiary beneficiary) {
		this.beneficiary = beneficiary;
	}
}
