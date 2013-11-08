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

@Entity
@Table(name="conjoinpc4")
public class ConstantJoinPC4
{
	@Column(length=50)
	private String name;

	@Id
	private int id;

	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	private ConstantJoinPC5 oneToOne1;
	@ManyToMany
	private Set manyToMany = new HashSet ();

	public ConstantJoinPC4()
	{}

	public ConstantJoinPC4 (String name, int id)
	{
		this.name = name;
		this.id = id;
	}


	public String getName ()
	{
		return name;
	}


	public void setOneToOne1 (ConstantJoinPC5 val)
	{
		oneToOne1 = val;
	}


	public ConstantJoinPC5 getOneToOne1 ()
	{
		return oneToOne1;
	}


	public void setManyToMany (Set val)
	{
		manyToMany = val;
	}


	public Set getManyToMany ()
	{
		return manyToMany;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}
}
