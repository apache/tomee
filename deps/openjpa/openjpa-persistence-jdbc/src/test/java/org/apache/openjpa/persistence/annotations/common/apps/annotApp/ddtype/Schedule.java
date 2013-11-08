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
package org.apache.openjpa.persistence.annotations.common.apps.annotApp.ddtype;

import java.util.Date;
import java.util.Calendar;

import javax.persistence.*;

@Entity
//@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@EntityListeners({DateValidator.class, StringValidator.class})
public class Schedule
{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;

	@Temporal(TemporalType.DATE)
	@Basic
	private Calendar dob;

	@Temporal(TemporalType.TIME)
	@Basic
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Basic
	private Date endDate;

	@Basic
	private String name;

	public Schedule(){}

	public Schedule(String name, Calendar cad, Date sdate, Date endDate)
	{
		this.name = name;
		this.dob = cad;
		this.startDate = sdate;
		this.endDate = endDate;
	}

	public Calendar getDob() {
		return dob;
	}

	public void setDob(Calendar dob) {
		this.dob = dob;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString()
	{
		return "Schedule "+name+" Date "+startDate;
	}

}
