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
package org.apache.openjpa.persistence.nullity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

/**
 * Persistent entity used to test behavior of null constraint on basic fields.
 *  
 * @author Pinaki Poddar
 *
 */
@Entity
@Table(uniqueConstraints=@UniqueConstraint(name="UniqueNullable", columnNames={"UNS"}))
public class NullValues {
	@Id
	@GeneratedValue
	private long id;
	
	@Column(nullable=true)
	private Integer nullable;
	
	@Column(nullable=false)
	private Integer notNullable;
	
	@Basic(optional=true)
	private Integer optional;
	
	@Basic(optional=false)
	private Integer notOptional;
	
	@Column(nullable=true)
	private BlobValue nullableBlob;
	
	@Column(nullable=false)
	private BlobValue notNullableBlob;
	
	@Basic(optional=true)
	private BlobValue optionalBlob;
	
	@Basic(optional=false)
	private BlobValue notOptionalBlob;
	
	@Column(name="UNS")
	private String uniqueNullable;
	
	@Version
	private int version;
	
	
	/**
	 * Construct with all fields set to non-null values.
	 */
	public NullValues() {
		setOptional(42);
		setNotOptional(42);
		setNotNullable(42);
		setNullable(42);
		
		setNullableBlob(new BlobValue());
		setNotNullableBlob(new BlobValue());
		setOptionalBlob(new BlobValue());
		setNotOptionalBlob(new BlobValue());
		setUniqueNullable("");
	}
	
	public long getId() {
		return id;
	}
	
	public Integer getNullable() {
		return nullable;
	}
	
	public void setNullable(Integer nullable) {
		this.nullable = nullable;
	}
	
	public Integer getNotNullable() {
		return notNullable;
	}
	
	public void setNotNullable(Integer notNullable) {
		this.notNullable = notNullable;
	}
	
	public Integer getOptional() {
		return optional;
	}
	
	public void setOptional(Integer optional) {
		this.optional = optional;
	}
	
	public Integer getNotOptional() {
		return notOptional;
	}
	
	public void setNotOptional(Integer notOptional) {
		this.notOptional = notOptional;
	}

	public BlobValue getNullableBlob() {
		return nullableBlob;
	}

	public void setNullableBlob(BlobValue nullableBlob) {
		this.nullableBlob = nullableBlob;
	}

	public BlobValue getNotNullableBlob() {
		return notNullableBlob;
	}

	public void setNotNullableBlob(BlobValue notNullableBlob) {
		this.notNullableBlob = notNullableBlob;
	}

	public BlobValue getOptionalBlob() {
		return optionalBlob;
	}

	public void setOptionalBlob(BlobValue optionalBlob) {
		this.optionalBlob = optionalBlob;
	}

	public BlobValue getNotOptionalBlob() {
		return notOptionalBlob;
	}

	public void setNotOptionalBlob(BlobValue notOptionalBlob) {
		this.notOptionalBlob = notOptionalBlob;
	}
	
    public String getUniqueNullable() {
        return uniqueNullable;
    }
	
    public void setUniqueNullable(String s) {
        uniqueNullable = s;
    }
    
	public int getVersion() { 
	    return version;
	}
}
