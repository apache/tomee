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

import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name="RIDER_ANNUITY")
@AttributeOverride(name="lastUpdateDate", column=@Column(name="LAST_UPDATE_TS"))
public class Rider extends AnnuityPersistebleObject implements IRider {
	private static final long serialVersionUID = 2088116709551706187L;

	private String rule;
	private Date effectiveDate;
	private RiderType type;
	
	@Column(name="EFFECTIVE_DATE")
	public Date getEffectiveDate() {
		return effectiveDate;
	}
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	@Column(name="RIDER_RULE")
	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
	
	@Enumerated(EnumType.STRING)	
	public RiderType getType() {
		return type;
	}
	public void setType(RiderType type) {
		this.type = type;
	}
	

}
