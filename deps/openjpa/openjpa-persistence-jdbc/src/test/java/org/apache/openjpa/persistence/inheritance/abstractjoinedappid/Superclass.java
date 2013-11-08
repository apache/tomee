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
package org.apache.openjpa.persistence.inheritance.abstractjoinedappid;

import javax.persistence.*;

@Entity
@IdClass(SuperID.class)
@Table(name="SUPER")
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class Superclass {
	
	private Integer id;
	private String attr1;
	
	@Id
	@Column(name="ID")
	public Integer getId() { return id; }
	public void setId(Integer id) {	this.id = id; }
	
	@Column(name="ATTR1")
	public String getAttr1() { return attr1; }
	public void setAttr1(String attr1) { this.attr1 = attr1; }
}
