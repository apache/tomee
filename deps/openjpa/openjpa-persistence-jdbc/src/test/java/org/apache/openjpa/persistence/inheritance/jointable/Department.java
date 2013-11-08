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
package org.apache.openjpa.persistence.inheritance.jointable;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Inheritance(strategy=InheritanceType.JOINED)
@Entity
@Table(name="WDept")
public class Department {
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="JWTGen")
    private long OID; 

    @Basic
    private String description;

	@Column(name="DeptProp1",length=10)
    @Basic
    private String deptProp1;
  
    @OneToMany(mappedBy="dept",cascade={CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH},fetch=FetchType.LAZY)
    private java.util.Collection<Contractor> ctrs;
    
	public Department() {
	}
	
	public Department(String desc) {
        setDescription(desc);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    
    public long getOID() {
        return OID;
    }
    
    public void setOID(long oid) {
        this.OID = oid;
    }

	
	public String getDeptProp1() {
		return deptProp1;
	}

	public void setDeptProp1(String deptProp1) {
		this.deptProp1 = deptProp1;
	}

	public java.util.Collection<Contractor> getCtrs() {
		return ctrs;
	}

	public void setCtrs(java.util.Collection<Contractor> ctrs) {
		this.ctrs = ctrs;
	}
}

