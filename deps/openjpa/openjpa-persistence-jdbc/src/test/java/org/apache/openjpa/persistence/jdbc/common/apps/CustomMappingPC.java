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
package org.apache.openjpa.persistence.jdbc.common.apps;

import javax.persistence.*;
/**
 *	<p>Persistent type used in testing.</p>
 *
 *	@author		Abe White
 */

@Entity
@Table(name="custmappc")
public class CustomMappingPC
{

	private boolean female;
	@Column(length=50)
	private String 	name;

	@Id
	private int id;

	public CustomMappingPC()
	{
	}

	public CustomMappingPC(int id)
	{
		this.id = id;
	}


	public boolean isFemale ()
	{
		return this.female;
	}


	public void setFemale (boolean female)
	{
		this.female = female;
	}


	public String getName ()
	{
		return this.name;
	}


	public void setName (String name)
	{
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
