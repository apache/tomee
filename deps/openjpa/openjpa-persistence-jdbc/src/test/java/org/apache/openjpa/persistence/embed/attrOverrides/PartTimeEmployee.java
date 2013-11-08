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
package org.apache.openjpa.persistence.embed.attrOverrides;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;

@Entity
@AssociationOverrides({
	@AssociationOverride(
		name="address",
		joinColumns=@JoinColumn(name="ADDR_ID")),
	
	@AssociationOverride(
		name="projects",
		joinColumns={},
    	joinTable=@JoinTable(
       		name="PART_EMP_PROJECTS",
       		joinColumns=@JoinColumn(name="PART_EMP"),
       		inverseJoinColumns=@JoinColumn(name="PROJECT_ID")))
})

@Table(name="PART_EMP_ASSOC")
public class PartTimeEmployee extends AbstractEmployee {

	@Column(name="WAGE")
	protected Float wage;

    public Float getHourlyWage() {
        return wage;
    }
    
    public void setHourlyWage(Float wage) {
        this.wage = wage;
    }
}
