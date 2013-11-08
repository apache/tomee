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
package org.apache.openjpa.jdbc.kernel;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Version;

@Entity
public class M2MBiDepartment {

	@Id	
	private String deptid;
	
	String name;
	
	@ManyToMany(mappedBy="departments", fetch=FetchType.EAGER)	
    public Collection<M2MBiEmployee> employees = new ArrayList<M2MBiEmployee>();
	
	@Version
    private int version;

    private String costCode;

    public Collection<M2MBiEmployee> getEmployees() {
        return employees;
    }

    public void setEmployees(Collection<M2MBiEmployee> employees) {
        this.employees = employees;
    }

    public String getDeptid() {
        return deptid;
    }

    public void setDeptid(String deptid) {
        this.deptid = deptid;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCostCode() {
        return costCode;
    }

    public void setCostCode(String costCode) {
        this.costCode = costCode;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
