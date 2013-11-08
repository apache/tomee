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

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

@Entity
public class Student implements Serializable {

	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@Column(length=50)
	private String name;

	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinTable(name="STUD_COURSE",
		            joinColumns=@JoinColumn(name="STUD_ID"),
		            inverseJoinColumns=@JoinColumn(name="CRSE_ID"))
	private List<Course> course;

	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinTable(name="STUD_DEP",
		            joinColumns=@JoinColumn(name="STUD_ID"),
		            inverseJoinColumns=@JoinColumn(name="DEP_ID"))
	private List<Department> department;

	public Student(){}

	public Student(String name)
	{
		this.name = name;
	}

	public Student(String name, List<Course> clist, List<Department> dlist)
	{
		this.name = name;
		this.course = clist;
		this.department = dlist;
	}

	public List<Course> getCourse() {
		return course;
	}

	public void setCourse(List<Course> course) {
		this.course = course;
	}

	public List<Department> getDepartment() {
		return department;
	}

	public void setDepartment(List<Department> department) {
		this.department = department;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
