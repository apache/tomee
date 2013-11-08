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
package org.apache.openjpa.persistence.relations;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="E4_REL")
public class E {

	@Id
	private String eId;

	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "e")
	private Set<VCS> vcss = new HashSet<VCS>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "e")
	private Set<CM> cms = new HashSet<CM>();

	public E() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<CM> getCms() {
		return cms;
	}

	public void setCms(Set<CM> cms) {
		this.cms = cms;
	}

	public String getEId() {
		return eId;
	}

	public void setEId(String id) {
		eId = id;
	}

	public Set<VCS> getVcss() {
		return vcss;
	}

	public void setVcss(Set<VCS> vcss) {
		this.vcss = vcss;
	}
}
