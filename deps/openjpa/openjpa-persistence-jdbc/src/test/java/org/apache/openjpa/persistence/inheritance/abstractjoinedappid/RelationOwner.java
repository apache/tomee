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
package org.apache.openjpa.persistence.inheritance.abstractjoinedappid;

import java.util.*;
import javax.persistence.*;

import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;

@Entity
@Table(name="TEST")
public class RelationOwner {
	
	private Integer id;
	private Collection<Superclass> supers = new ArrayList<Superclass>();
	
	@Id
	@Column(name="ID")
	public Integer getId() { return id;	}
	public void setId(Integer id) { this.id = id; }
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@ElementJoinColumn(name="TEST", referencedColumnName="ID")
	public Collection<Superclass> getSupers() {	return supers; }
    public void setSupers(Collection<Superclass> supers) {
        this.supers = supers;
    }
}
