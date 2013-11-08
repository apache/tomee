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
package org.apache.openjpa.persistence.jdbc.annotations;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.VersionColumn;
import org.apache.openjpa.persistence.jdbc.VersionColumns;
import org.apache.openjpa.persistence.jdbc.VersionStrategy;

/**
 * Persistent entity for testing multiple column numeric version strategy as 
 * set by @VersionColumns annotations.
 * 
 * The version columns can have numeric values of different types.
 * 
 * @author Pinaki Poddar
 *
 */
@Entity
@Table(name="MCV")
@VersionStrategy("version-numbers")
@VersionColumns({
	@VersionColumn(name="v1"), 
	@VersionColumn(name="v2"),
    @VersionColumn(name="v3", columnDefinition="FLOAT", scale=3, precision=10)
})
public class MultiColumnVersionPC {
	@Id
	@GeneratedValue
	private long id;
	
	private String name;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
