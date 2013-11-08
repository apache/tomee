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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="MRG_CAR")
public class Car {

	@Id
	@GeneratedValue
	@Column(name="CAR_ID")
	private long id;
	
	@OneToOne(cascade=CascadeType.ALL, mappedBy="car")
	private Model model;
	
    @SuppressWarnings("unused")
    @Version
    private int version;

		
	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}
}
