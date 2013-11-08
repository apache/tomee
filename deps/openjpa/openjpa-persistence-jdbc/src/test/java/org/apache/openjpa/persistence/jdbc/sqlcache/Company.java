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
package org.apache.openjpa.persistence.jdbc.sqlcache;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.*;

@Entity
@Table(name="COMPANY_PQC")

@NamedQueries({
	@NamedQuery(name="Company.PreparedQueryWithNoParameter", 
	    query="select x from Company x"),
	@NamedQuery(name="Company.PreparedQueryWithNamedParameter", 
        query="select x from Company x "
            + "where x.name=:name and x.startYear=:startYear"),
	@NamedQuery(name="Company.PreparedQueryWithPositionalParameter", 
	    query="select x from Company x where x.name=?1 and x.startYear=?2"),
	@NamedQuery(name="Company.PreparedQueryWithLiteral", 
        query="select x from Company x where x.name='IBM' and x.startYear=1900")
})
public class Company {
	@Id
	@GeneratedValue
	private long id;
	
	private String name;
	
	private int startYear;
	
	@OneToMany(mappedBy="company", cascade=CascadeType.ALL)
	private Collection<Department> departments = new HashSet<Department>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<Department> getDepartments() {
		return departments;
	}

	public void addDepartment(Department dept) {
		this.departments.add(dept);
		dept.setCompany(this);
	}

	public long getId() {
		return id;
	}

	public int getStartYear() {
		return startYear;
	}

	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}
}
