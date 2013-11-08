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
package org.apache.openjpa.persistence.jdbc.common.apps.mappingApp;

import java.io.Serializable;

import javax.persistence.*;

/** This domain class uses a all possible nature of annotations. Nature of
 * annotation from the point of view of nature of result they produce are
 * the following
 * <pre>
 * #entity-result      #column-result       nature
 * 0                     1+                 SCALAR_ONLY
 * 1                     0                  SINGLE_CLASS
 * 1                     1+                 SINGLE_CLASS_AND_SCALAR
 * 1+                    0                  MULTI_CLASS
 * 1+                    1+                 MULTI_CLASS_AND_SCALAR
 * </pre> 
 * 
 */
@SqlResultSetMappings(value={
		/** Specifies only scalars and no entity.
		 * 
		 */		
				@SqlResultSetMapping(name="SCALAR_ONLY",
				    columns={
						@ColumnResult(name="NAME")
					}
				),
		/** Specifies one entity and no scalar.
		 * 
		 */		
				@SqlResultSetMapping(name="SINGLE_CLASS",
					entities={
                        @EntityResult(entityClass=SQLMapPerson.class)
					}
				),
				
				/** Specifies one entity and one or more scalar.
				 * 
				 */				
                @SqlResultSetMapping(name="SINGLE_CLASS_AND_SCALAR",
					entities={
                        @EntityResult(entityClass=SQLMapPerson.class)
					},
					columns={
						@ColumnResult(name="name")
					}
				),
				
                /** Specifies more than one entity and no scalar.
				 * 
				 */				
				@SqlResultSetMapping(name="MULTI_CLASS",
					entities={
                        @EntityResult(entityClass=SQLMapPerson.class),
                        @EntityResult(entityClass=SQLMapAddress.class)
					}
				),
				
                /** Specifies more than one entity and one or more scalar.
				 * 
				 */				
                @SqlResultSetMapping(name="MULTI_CLASS_AND_SCALAR",
                    entities={
                        @EntityResult(entityClass=SQLMapPerson.class),
                        @EntityResult(entityClass=SQLMapAddress.class)
					},
					columns={
						@ColumnResult(name="name"),
						@ColumnResult(name="state")
					}
				),

                @SqlResultSetMapping(name="MappingWithTraversal",
					entities={
                        @EntityResult(entityClass=SQLMapAddress.class,
                        fields={
                            @FieldResult(name="id",    column="ADDR_ID"),
                            @FieldResult(name="street",column="ADDR_STREET"),
                            @FieldResult(name="state", column="ADDR_STATE"),
                            @FieldResult(name="zip",   column="ADDR_ZIP")
                        }),
                        @EntityResult(entityClass=SQLMapPerson.class,
                        fields={
                            @FieldResult(name="name",    column="MY_NAME"),
                            @FieldResult(name="address", column="MY_ADDRESS")
                        })
					}
				)
				
				
			}
		) 


@Entity
@Table(name = "SQLMAP_ADDRESS")
public class SQLMapAddress implements Serializable {
	private int    id;
	private String street;
	private String state;
	private int    zip;

	protected SQLMapAddress() {

	}

	public SQLMapAddress(int id, String street, String state, int zip) {
		this.id = id;
		setStreet(street);
		setState(state);
		setZip(zip);
	}

	@Id
	public int getId ()
	{
		return id;
	}

	public void setId (int id)
	{
		this.id = id;
	}

	@Column(name="STATE")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Column(name="STREET")
	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public int getZip() {
		return zip;
	}

	@Column(name="ZIP")
	public void setZip(int zip) {
		this.zip = zip;
	}
	
	@PostLoad
	protected void inform() {
		System.out.println("Loaded" + this);
	}

}
