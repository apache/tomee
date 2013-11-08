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
package org.apache.openjpa.persistence.jdbc.common.apps.mappingApp;

import javax.persistence.*;

@Entity
@IdClass(CompositeId.class)
@Table(name="COMPOSITE_ID")
public class EntityWithCompositeId {
	private Integer id;   // this must match the field in CompositeId
	private String  name; // this must match the field in CompositeId
	private String  value;
	
	public EntityWithCompositeId() {
		super();
	}
	
	@Id
	@Column(name="ID")
	public Integer getId () 
	{
		return id;
	}

	@Id
	@Column(name="NAME")
	public String getName () {
		return name;
	}
	
	
	@Column(name="VALUE")
	public String getValue () 
	{
		return value;
	}
	
	public void setId (Integer id) {
		this.id = id;
	}
	
	public void setName (String name) 
	{
		this.name = name;
	}
	
	public void setValue (String value)
	{
		this.value = value;
	}
}
