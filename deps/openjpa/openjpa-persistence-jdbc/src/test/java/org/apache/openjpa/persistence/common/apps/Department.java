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
package org.apache.openjpa.persistence.common.apps;

import javax.persistence.*;
import java.io.*;
import java.util.*;


@Entity
public class Department implements Serializable
{
	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Basic
	private String name;

	@ManyToMany(mappedBy="department")
	private List<Student> slist;

	public Department(){}

	public Department(int id)
	{
		this.id = id;
	}

	public Department(String name, List<Student> slist, int id)
	{
		this.name = name;
		this.slist = slist;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Student> getSlist() {
		return slist;
	}

	public void setSlist(List<Student> slist) {
		this.slist = slist;
	}
}
