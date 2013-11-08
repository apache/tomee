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


import java.util.*;
import javax.persistence.*;


/**
 *	<p>Persistent type used in testing.</p>
 *
 *	@author		Abe White
 */
@Entity
@Table(name="autoincpc1")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class AutoIncrementPC1
{

    private Set setField = new HashSet ();

	@Id
	private int id;

	@Column(name="strngfld", length=50)
	private String				stringField	= null;

	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	private AutoIncrementPC1	oneOne		= null;

	public AutoIncrementPC1()
	{
	}

	public AutoIncrementPC1(int key)
	{
		this.id = key;
	}

	public Set getSetField ()
	{
		return this.setField;
	}

	public void setSetField (Set setField)
	{
		this.setField = setField;
	}


	public String getStringField ()
	{
		return this.stringField;
	}


	public void setStringField (String stringField)
	{
		this.stringField = stringField;
	}


	public AutoIncrementPC1 getOneOne ()
	{
		return this.oneOne;
	}


	public void setOneOne (AutoIncrementPC1 oneOne)
	{
		this.oneOne = oneOne;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
