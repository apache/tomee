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

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


//@Entity(name="entity2ExplicitName")
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
/**FIX-ME 
 * 
 * It should complain if i uncomment the above strategies...but it does
 */
@Entity
public class Entity2 implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 4723739219953167343L;

	@Id
	protected long pk;

	@Basic
	@Column(length=35)
	protected String stringField;

	@Basic
	protected int intField;

	public Entity2()
	{}


	public Entity2 (long pk, String stringField, int intField)
	{
		this.pk = pk;
		this.stringField = stringField;
		this.intField = intField;
	}


	public long getPk ()
	{
		return pk;
	}


	public void setStringField (String val)
	{
		stringField = val;
	}


	public String getStringField ()
	{
		return stringField;
	}

	public void setIntField (int val)
	{
		intField = val;
	}


	public int getIntField ()
	{
		return intField;
	}

	public String toString()
	{
        return("PK: " + pk + " StringField: " + stringField
                + " IntField: " + intField);
	}
}
