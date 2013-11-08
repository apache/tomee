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
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class HelperPC3
{
	@Column(name="strngfld", length=50)
	private String stringField;

	@Id
	private int id;

	public HelperPC3()
	{
	}

	public HelperPC3(int id)
	{
		this.id = id;
	}

	public String getStringField ()
	{
		return this.stringField;
	}

	public void setStringField (String stringField)
	{
		this.stringField = stringField;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
