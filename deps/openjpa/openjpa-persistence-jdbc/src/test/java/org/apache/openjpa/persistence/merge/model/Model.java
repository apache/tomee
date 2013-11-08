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
package org.apache.openjpa.persistence.merge.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.openjpa.persistence.jdbc.ForeignKey;

@Entity
@Table(name="MRG_MODEL")
public class Model {

	@Id
	@GeneratedValue
	@Column(name="MODEL_ID")
	private long id;

	@ManyToOne(cascade=CascadeType.ALL)
	@ForeignKey
	@JoinColumn(name="MAKE_ID")
	private Make make;
	
	@OneToOne(cascade=CascadeType.ALL)
	@ForeignKey
	@JoinColumn(name="CAR_ID")
	private Car car;

    @SuppressWarnings("unused")
    @Version
    private int version;

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setMake(Make make) {
		this.make = make;
	}

	public Make getMake() {
		return make;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	public Car getCar() {
		return car;
	}
}
