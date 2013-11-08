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


import java.io.Serializable;

import javax.persistence.*;

/**
 *	Used to test invert one-to-ones and stuff
 *
 *	@author		skim
 */
@SuppressWarnings("serial")
@Entity
public class InvertA implements Serializable
{
	@Id
	private int id;

	@Column(length=35)
	String test;

	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	InvertB	invertB;

	public InvertA()
	{
	}

	public InvertA(int id)
	{
		this.id = id;
	}

	public InvertB getInvertB ()
	{
		return invertB;
	}

	public void setInvertB (InvertB b)
	{
		invertB = b;
	}

	public void setTest (String s)
	{
		test = s;
	}

	public String getTest ()
	{
		return test;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
