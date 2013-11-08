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

import javax.persistence.*;

@SqlResultSetMappings(value={
	@SqlResultSetMapping(name="SQLSelectsChainedRelation",
		entities={
            @EntityResult(entityClass=RecursiveEntityWithCompositeId.class,
				fields={
				
                @FieldResult(name="id",                   column="T0_ID"),
                @FieldResult(name="name",                 column="T0_NAME"),
                @FieldResult(name="relation.id",          column="T0_REL_ID"),
                @FieldResult(name="relation.name",        column="T0_REL_NAME"),
                @FieldResult(name="relation.relation.id", column="T1_REL_ID"),
                @FieldResult(name="relation.relation.name",
                        column="T1_REL_NAME"),
                @FieldResult(name="relation.relation.relation.id",
                        column="T2_REL_ID"),
                @FieldResult(name="relation.relation.relation.name",
                        column="T2_REL_NAME")
				
				}
			)
		}
	)
  }
)
 
@Entity
@IdClass(CompositeId.class)
@Table(name="RECURSIVE_ENTITY")

public class RecursiveEntityWithCompositeId {
	private Integer id;   // this must match the field in CompositeId
	private String  name; // this must match the field in CompositeId
	private int     value;// a non-primary key field 
	private RecursiveEntityWithCompositeId  relation; // self-related
	
	public RecursiveEntityWithCompositeId() {
		super();
	}
	
	@Id
	@Column(name="ID")
	public Integer getId () 
	{
		return id;
	}

	@Id
	@Column(name="NAME")
	public String getName () {
		return name;
	}
	
	@Column(name="VALUE")
	public int getValue(){
	   return value;	
	}
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumns({
		@JoinColumn(name="RELATION_ID",   referencedColumnName="ID"),
		@JoinColumn(name="RELATION_NAME", referencedColumnName="NAME")
	})
	public RecursiveEntityWithCompositeId getRelation() 
	{
		return relation;
	}
	
	public void setId (Integer id) {
		this.id = id;
	}
	
	public void setName (String name) 
	{
		this.name = name;
	}
	public void setValue (int value) 
	{
		this.value = value;
	}
	
	public void setRelation (RecursiveEntityWithCompositeId relation)
	{
		this.relation = relation;
	}
}
