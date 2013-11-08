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
@SqlResultSetMapping(name="SQLSelectsBothEndOfTheRelation",
		entities={
            @EntityResult(entityClass=OwnerOfEntityWithCompositeId.class,
                fields={
                    @FieldResult(name="name",          column="OWNER_NAME"),
                    @FieldResult(name="relation.id",   column="REL_ID"),
                    @FieldResult(name="relation.name", column="REL_NAME")
				}
			),
			@EntityResult(entityClass=EntityWithCompositeId.class,
				fields={
					@FieldResult(name="id",  column="C_ID"),
                    @FieldResult(name="name", column="C_NAME"),
                    @FieldResult(name="value", column="C_VALUE")
				}
			)
		}
),
@SqlResultSetMapping(name="SQLSelectsOnlyOneEndOfTheRelation",
		entities={
            @EntityResult(entityClass=OwnerOfEntityWithCompositeId.class,
                fields={
                    @FieldResult(name="name",          column="OWNER_NAME"),
                    @FieldResult(name="relation.id",   column="REL_ID"),
                    @FieldResult(name="relation.name", column="REL_NAME")
				}
			)
		}
),
@SqlResultSetMapping(name="SQLSelectsUnrelatedInstances",
		entities={
            @EntityResult(entityClass=OwnerOfEntityWithCompositeId.class,
                fields={
                    @FieldResult(name="name",          column="OWNER_NAME"),
                    @FieldResult(name="relation.id",   column="REL_ID"),
                    @FieldResult(name="relation.name", column="REL_NAME")
				}
			),
			@EntityResult(entityClass=EntityWithCompositeId.class,
				fields={
                    @FieldResult(name="id",    column="C_ID"),
                    @FieldResult(name="name",  column="C_NAME"),
                    @FieldResult(name="value", column="C_VALUE")
				}
			)
		}
)

})

@Entity
@Table(name="OWNER_OF_COMPOSITE_ID")
public class OwnerOfEntityWithCompositeId {
	
	private String                 name;
	private EntityWithCompositeId  relation;
	
	public OwnerOfEntityWithCompositeId() {
		super();
	}
	
	public OwnerOfEntityWithCompositeId (String name)
	{
		setName (name);
	}
	
	@Id
	@Column(name="NAME")
	public String getName () 
	{
		return name;
	}
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumns({
		@JoinColumn(name="RELATION_ID",referencedColumnName="ID"),
		@JoinColumn(name="RELATION_NAME", referencedColumnName="NAME")
	})
	
	public EntityWithCompositeId getRelation()
	{
		return relation;
	}
	
	
	public void setName (String name)
	{
		this.name = name;
	}
	
	public void setRelation (EntityWithCompositeId relation)
	{
		this.relation = relation;
	}
}
