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

import javax.persistence.*;

import java.util.*;

@Entity
@DiscriminatorValue("fsched")
//@Table(name="Flight_Sched",
//		uniqueConstraints=@UniqueConstraint(columnNames={"planeName"}))
public class FlightSchedule extends Schedule
{
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int FlightNum;

	@Column(name="flight_name")
	private String planeName;

	public FlightSchedule(){}

    public FlightSchedule(String name, Calendar cad, Date start, Date end,
            String pname) {
		super(name, cad, start, end);
		this.planeName = pname;
	}

	public String getFlightName() {
		return planeName;
	}

	public void setFlightName(String flightName) {
		planeName = flightName;
	}

	public int getFlightNum() {
		return FlightNum;
	}
}
