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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.VersionColumn;
import org.apache.openjpa.persistence.jdbc.VersionColumns;
import org.apache.openjpa.persistence.jdbc.VersionStrategy;

/**
 * Persistent entity for testing multiple column numeric version strategy as set
 * by <code>@VersionColumns</code> annotations and where the version columns are
 * spread over primary and secondary table(s).
 * 
 * @author Pinaki Poddar
 * 
 */
@Entity
@Table(name="MCSV")
@SecondaryTables({ 
    @SecondaryTable(name = "MCSV1",
            pkJoinColumns=@PrimaryKeyJoinColumn(name="ID")),
    @SecondaryTable(name = "MCSV2",
            pkJoinColumns=@PrimaryKeyJoinColumn(name="ID")) 
})
@VersionStrategy("version-numbers")
@VersionColumns({ 
	@VersionColumn(name = "v11", table="MCSV1"), 
	@VersionColumn(name = "v12", table="MCSV1"), 
	@VersionColumn(name = "v21", table="MCSV2"),
	@VersionColumn(name = "v01") // default is the primary table
})
public class MultiColumnSecondaryVersionPC {
	@Id
	@GeneratedValue
	private long id;

	private String name;
	
	@Column(table="MCSV1")
	private String s1;
	
	@Column(table="MCSV2")
	private String s2;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getS1() {
		return s1;
	}

	public void setS1(String s1) {
		this.s1 = s1;
	}

	public String getS2() {
		return s2;
	}

	public void setS2(String s2) {
		this.s2 = s2;
	}
}
