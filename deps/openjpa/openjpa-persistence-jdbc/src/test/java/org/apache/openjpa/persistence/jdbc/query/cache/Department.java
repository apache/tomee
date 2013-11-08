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
package org.apache.openjpa.persistence.jdbc.query.cache;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.ElementClassCriteria;

/**
 * Persistent entity with collection whose element type belongs to inheritance
 * hierarchy mapped to a SINGLE_TABLE. Hence relationship loading will require
 * 
 */
@Entity
@Table(name = "DEPT")
@IdClass(DepartmentId.class)
public class Department {

	@Id
	private String name;

	@OneToMany(mappedBy = "dept", cascade = CascadeType.PERSIST)
	@ElementClassCriteria
	private Collection<PartTimeEmployee> partTimeEmployees;

	@OneToMany(mappedBy = "dept", cascade = CascadeType.PERSIST)
	@ElementClassCriteria
	private Collection<FullTimeEmployee> fullTimeEmployees;

	public Collection<FullTimeEmployee> getFullTimeEmployees() {
		return fullTimeEmployees;
	}

	public void addEmployee(FullTimeEmployee e) {
		if (fullTimeEmployees == null)
			fullTimeEmployees = new ArrayList<FullTimeEmployee>();
		this.fullTimeEmployees.add(e);
		e.setDept(this);
	}

	public Collection<PartTimeEmployee> getPartTimeEmployees() {
		return partTimeEmployees;
	}

	public void addEmployee(PartTimeEmployee e) {
		if (partTimeEmployees == null)
			partTimeEmployees = new ArrayList<PartTimeEmployee>();
		this.partTimeEmployees.add(e);
		e.setDept(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
