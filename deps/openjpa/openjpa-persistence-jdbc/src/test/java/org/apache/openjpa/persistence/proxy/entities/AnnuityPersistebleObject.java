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

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.persistence.Version;

@MappedSuperclass
public class AnnuityPersistebleObject implements JPAPersisteble {
	private static final long serialVersionUID = -1752164352355128830L;
	private String id;
	private Date lastUpdateDate;
	private Parameterizable<String, String> config = new StringParameterizable();
	private int version;
	
	@Version
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@javax.persistence.Id
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
		
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	@Transient
	public Parameterizable<String, String> getConfiguration() {
		return this.config;
	}
	@Transient
	public void setConfiguration(Parameterizable<String, String> config) {
		this.config = config;
	}
	
	@SuppressWarnings("unused")
	@PrePersist
	@PreUpdate
	private void fixLastUpdateDate(){
		setLastUpdateDate(new Date());
	}
}
