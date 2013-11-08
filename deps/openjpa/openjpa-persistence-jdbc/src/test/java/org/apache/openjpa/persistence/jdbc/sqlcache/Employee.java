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

import javax.persistence.*;

@Entity
@Table(name="EMPLOYEE_PQC")

public class Employee {
    public enum Category {PERMANENT, CONTRACTOR, TEMP};
    
	@Id
	@GeneratedValue
	private long id;
	
	private String name;
	
	@ManyToOne
	private Department department;
	
	@OneToOne
	private Address address;
	
	private boolean isManager;
	
	private Category status;
	
	@Enumerated(EnumType.ORDINAL)
	private Category hireStatus;

	public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean isManager) {
        this.isManager = isManager;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public long getId() {
		return id;
	}
	
    public Category getHireStatus() {
        return hireStatus;
    }
    
    public Category setHireStatus(Category status) {
        return hireStatus;
    }
    
    public Category getCurrentStatus() {
        return status;
    }
    
    public Category getCurrentStatus(Category status) {
        return status;
    }

}
